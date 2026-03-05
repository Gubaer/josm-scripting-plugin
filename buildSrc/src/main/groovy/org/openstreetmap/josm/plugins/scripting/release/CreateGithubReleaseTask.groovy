package org.openstreetmap.josm.plugins.scripting.release

import groovy.json.JsonOutput
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Creates a GitHub release for the scripting plugin.
 *
 * Usage in build.gradle:
 * <pre>
 * tasks.register("createGithubRelease", CreateGithubReleaseTask) {
 *     releaseTag = "v1.2.3"
 *     releaseDescription = "a description"
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
abstract class CreateGithubReleaseTask extends DefaultTask {

    static final String GITHUB_API_BASE_URL = "https://api.github.com"
    static final MediaType JSON = MediaType.get("application/json; charset=utf-8")

    /**
     * The release tag, e.g. "v1.2.3"
     */
    @Input
    abstract Property<String> getReleaseTag()

    /**
     * The release description (body text)
     */
    @Input
    abstract Property<String> getReleaseDescription()

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

    @TaskAction
    def createRelease() {
        final githubUser  = requireProjectProperty("github.user")
        final githubRepo  = requireProjectProperty("github.repository")
        final githubToken = requireEnvVar("GITHUB_ACCESS_TOKEN")

        final tag         = releaseTag.get()
        final body        = releaseDescription.get()

        final url = "${GITHUB_API_BASE_URL}/repos/${githubUser}/${githubRepo}/releases"
        logger.info("Creating GitHub release '${tag}' at ${url}")

        final payload = JsonOutput.toJson([
            tag_name: tag,
            name    : tag,
            body    : body,
            draft   : false,
            prerelease: false
        ])

        final client = new OkHttpClient()
        final request = new Request.Builder()
            .url(url)
            .header("Authorization", "token ${githubToken}")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .post(RequestBody.create(payload, JSON))
            .build()

        client.newCall(request).execute().withCloseable { response ->
            if (!response.successful) {
                final responseBody = response.body()?.string() ?: "(no body)"
                throw new GradleException(
                    "Failed to create GitHub release '${tag}'. " +
                    "HTTP ${response.code()}: ${responseBody}"
                )
            }
            logger.lifecycle(
                "Successfully created GitHub release '${tag}' at " +
                "https://github.com/${githubUser}/${githubRepo}/releases/tag/${tag}"
            )
        }
    }
}
