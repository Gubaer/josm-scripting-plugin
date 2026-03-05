package org.openstreetmap.josm.plugins.scripting.release

import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * Publishes a JAR file as an asset to the latest GitHub release.
 *
 * Usage in build.gradle:
 * <pre>
 * tasks.register("publishToGithubRelease", PublishToGithubReleaseTask) {
 *     localJarName = "scripting.jar"   // optional, default: "scripting.jar"
 *     remoteJarName = "scripting.jar"  // optional, default: "scripting.jar"
 * }
 * </pre>
 *
 * Requires:
 * <ul>
 *   <li>Project property <code>github.user</code> — GitHub repository owner</li>
 *   <li>Project property <code>github.repository</code> — GitHub repository name</li>
 *   <li>Environment variable <code>GITHUB_ACCESS_TOKEN</code> — GitHub personal access token</li>
 * </ul>
 */
@SuppressWarnings('unused')
abstract class PublishToGithubReleaseTask extends DefaultTask {

    static final String GITHUB_API_BASE_URL = "https://api.github.com"
    static final String DEFAULT_JAR_NAME = "scripting.jar"
    static final MediaType OCTET_STREAM = MediaType.get("application/octet-stream")

    /**
     * The file name of the local JAR in <code>build/libs/</code> to upload.
     * Defaults to "scripting.jar".
     */
    @Input
    @Optional
    abstract Property<String> getLocalJarName()

    /**
     * The asset name to use in the GitHub release.
     * Defaults to "scripting.jar".
     */
    @Input
    @Optional
    abstract Property<String> getRemoteJarName()

    PublishToGithubReleaseTask() {
        localJarName.convention(DEFAULT_JAR_NAME)
        remoteJarName.convention(DEFAULT_JAR_NAME)
    }

    private String requireProjectProperty(String name) {
        if (!project.hasProperty(name)) {
            throw new GradleException(
                "Missing mandatory project property '${name}'. " +
                "Set it in gradle.properties or pass it via -P${name}=<value>."
            )
        }
        return project.property(name) as String
    }

    static private String requireEnvVar(String name) {
        final value = System.getenv(name)
        if (!value) {
            throw new GradleException(
                "Missing mandatory environment variable '${name}'. " +
                "Export it before running this task."
            )
        }
        return value
    }

    private String fetchLatestReleaseUploadUrl(OkHttpClient client, String githubUser,
                                               String githubRepo, String githubToken) {
        final url = "${GITHUB_API_BASE_URL}/repos/${githubUser}/${githubRepo}/releases/latest"
        logger.info("Fetching latest release from ${url}")

        final request = new Request.Builder()
            .url(url)
            .header("Authorization", "token ${githubToken}")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .get()
            .build()

        client.newCall(request).execute().withCloseable { response ->
            if (!response.successful) {
                final body = response.body()?.string() ?: "(no body)"
                throw new GradleException(
                    "Failed to fetch latest GitHub release. " +
                    "HTTP ${response.code()}: ${body}"
                )
            }
            final json = new JsonSlurper().parseText(response.body().string())
            final uploadUrl = json.upload_url?.toString()
            if (!uploadUrl) {
                throw new GradleException("No 'upload_url' found in latest release response.")
            }
            // upload_url contains a URI template suffix like "{?name,label}" — strip it
            return uploadUrl.replaceFirst(/\{[^}]+\}$/, "")
        }
    }

    @TaskAction
    def publish() {
        final githubUser  = requireProjectProperty("github.user")
        final githubRepo  = requireProjectProperty("github.repository")
        final githubToken = requireEnvVar("GITHUB_ACCESS_TOKEN")

        final jarFile = new File(
            project.layout.buildDirectory.get().asFile, "libs/${localJarName.get()}"
        )
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            throw new GradleException(
                "JAR file to upload not found or not readable: '${jarFile.absolutePath}'"
            )
        }

        final client = new OkHttpClient()
        final uploadBaseUrl = fetchLatestReleaseUploadUrl(client, githubUser, githubRepo, githubToken)
        final uploadUrl = "${uploadBaseUrl}?name=${remoteJarName.get()}"
        logger.info("Uploading '${jarFile.name}' to ${uploadUrl}")

        final request = new Request.Builder()
            .url(uploadUrl)
            .header("Authorization", "token ${githubToken}")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .post(RequestBody.create(jarFile, OCTET_STREAM))
            .build()

        client.newCall(request).execute().withCloseable { response ->
            if (!response.successful) {
                final body = response.body()?.string() ?: "(no body)"
                throw new GradleException(
                    "Failed to upload '${jarFile.name}' to GitHub release. " +
                    "HTTP ${response.code()}: ${body}"
                )
            }
            logger.lifecycle(
                "Successfully uploaded '${jarFile.name}' as '${remoteJarName.get()}' " +
                "to the latest GitHub release of ${githubUser}/${githubRepo}."
            )
        }
    }
}
