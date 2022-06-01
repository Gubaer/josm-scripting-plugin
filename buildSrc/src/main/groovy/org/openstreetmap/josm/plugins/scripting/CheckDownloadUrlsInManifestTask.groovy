package org.openstreetmap.josm.plugins.scripting

import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.regex.Matcher
import java.util.stream.Collectors

@SuppressWarnings('unused')
class CheckDownloadUrlsInManifestTask extends DefaultTask {

    def lookupJarFile() {
        final jarFile = new File(project.buildDir, "dist/scripting.jar")
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            logger.error("No scripting.jar file found. Path: '$jarFile.absolutePath'")
        }
        return new JarFile(jarFile)
    }

    def loadManifest(JarFile jar) {
        logger.debug("Loading manifest from jar file '${jar.name}'")
        //NOTE: jar.getManifest() doesn't work?
        //var manifest = jar.getManifest()
        var entry = jar.getEntry("META-INF/MANIFEST.MF")
        if (entry == null) {
            logger.error("No manifest found in jar file '${jar.name}'")
            return null
        }
        var manifest = new Manifest(jar.getInputStream(entry))
        return manifest
    }

    static def extractDownloadUrls(Manifest manifest) {
        final keyPattern = ~/^\d+_Plugin-Url$/
        final valuePattern = ~/^\s*[^;]+;(.*)/

        return manifest.mainAttributes.entrySet().stream()
            .filter {entry ->
                var matcher = entry.key.toString() =~ keyPattern
                return matcher.matches()
            }.map {entry ->
                Matcher matcher = entry.value.toString() =~ valuePattern
                matcher.matches() ? matcher.group(1) : null
            }
            .filter {it != null}
            .map(URL::new)
            .collect(Collectors.toList())
    }

    def checkDownloadUrls(List<URL> urls) {
        var client = new OkHttpClient()
        var failedUrls = []
        urls.forEach {url ->
            logger.debug("Checking availability of download jar '$url'")
            var request = new Request.Builder()
                .url(url)
                .build()
            var response = client.newCall(request).execute()
            if (!response.successful) {
                logger.error("Failed to download jar from URL '$url'. " +
                "status code=${response.code()}")
                failedUrls.push(url)
            } else {
                logger.debug("Successfully validated download URL '$url'")
            }
            response.body().close()
        }
        return failedUrls
    }

    @TaskAction
    def check() {
        var jarFile = lookupJarFile()
        if (jarFile == null) {
            throw new GradleException("jar file for scripting plugin not found")
        }
        var manifest = loadManifest(jarFile)
        if (manifest == null) {
            throw new GradleException("manifest not found in jar file '$jarFile'")
        }
        var downloadUrls = extractDownloadUrls(manifest)
        var failedUrls = checkDownloadUrls(downloadUrls)
        if (!failedUrls.isEmpty()) {
            logger.error("The following download URLs could not be verified:")
            failedUrls.forEach {url ->
                logger.error("Failed download URL: ${url}")
            }
            throw new GradleException("${failedUrls.size()} download URLs not successfully verified")
        }
    }
}
