package org.openstreetmap.josm.plugins.scripting.release

import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.util.stream.Collectors

/**
 * Retrieves current information about the scripting plugin from JOSMS plugin
 * inventory. Use this task to check what plugin version is distributed to
 * JOSM users through JOSMs plugin system.
 */
@SuppressWarnings('unused')
class ShowPublishedPluginTask extends DefaultTask {
    /**
     * The URL where a dump of JOSMs plugin inventory is available
     */
    static public final String PLUGIN_INVENTORY_URL = "https://josm.openstreetmap.de/plugin"


    static def extractPluginNameAndDownloadUrl(List<String> lines) {
        final pattern3 = ~/^([^;]+);(.*)/
        var matcher = lines[0] =~ pattern3
        if (!matcher.matches()) {
            throw new GradleException(
                "Unexpected format of leading line with jar name and download " +
                "URL. Got: '${lines[0]}'")
        }
        return [
            "pluginName": matcher.group(1),
            "downloadUrl": matcher.group(2)
        ]
    }

    static def extractManifestAttributes(List<String> lines) {
        final pattern4 = ~/^\s*([^:]+):\s*(.*)/
        return lines.stream().skip(1).map {line ->
            final m = line =~ pattern4
            if (! m.matches()) {
                throw new GradleException(
                    "Unexpected format of manifest line. Got: '$line'"
                )
            }
            return [m.group(1), m.group(2)]
        }
        .collect(Collectors.toMap({it[0]}, {it[1]}))
    }

    @TaskAction
    def showInformationAboutPublishedPlugin() {
        final client = new OkHttpClient()
        final request = new Request.Builder()
            .url(PLUGIN_INVENTORY_URL)
            .build()

        final response = client.newCall(request).execute()
        if (! response.isSuccessful()) {
            throw new GradleException(
                "Failed to retrieve plugin information from '$PLUGIN_INVENTORY_URL'. " +
                "Response code: ${response.code()}")
        }

        final body = response.body().string()
        // the leading line: jar-name;download-url
        final pattern1 = ~/^scripting.jar;.*/
        // a manifest line: starts with leading whitespace
        final pattern2 = ~/^\s+.*/
        final lines = body.lines()
            .dropWhile {!(it =~ pattern1).matches()}
            .takeWhile {(it =~ pattern1).matches() || (it =~ pattern2).matches()}
            .map {it.stripIndent()}
            .collect(Collectors.toList())
        if (lines.isEmpty()) {
            throw new GradleException(
                "Didn't find information about scripting.jar in the plugin " +
                "inventory '$PLUGIN_INVENTORY_URL'")
        }

        final pluginCoreInfo = extractPluginNameAndDownloadUrl(lines)
        final pluginManifest = extractManifestAttributes(lines)

        logger.info("JOSMs plugin inventory accessible on '$PLUGIN_INVENTORY_URL' currently \n" +
            "includes the following information about the scripting plugin:")
        logger.info("--------------- Core information ---------------------")
        logger.info("Jar File: ${pluginCoreInfo["pluginName"]}")
        logger.info("Description: ${pluginManifest["Plugin-Description"]}")
        logger.info("Download url: ${pluginCoreInfo["downloadUrl"]}")
        logger.info("Version: ${pluginManifest["Plugin-Version"]}")
        logger.info("Release date: ${pluginManifest["Plugin-Date"]}")
        logger.info("--------------- All manifest attributes ---------------")
        logger.info(
            pluginManifest.entrySet()
                .collect {"$it.key: $it.value"}
                .join("\n")
        )
    }
}
