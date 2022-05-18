package org.openstreetmap.josm.plugins.scripting.convert

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


/**
 * Converts a releases file in the legacy format into a releases format into
 * the new YAML format.
 */
abstract class ReleaseFileConverter extends DefaultTask{

    @Input
    abstract Property<File> getInputFile()

    @Input
    abstract Property<File> getOutputFile()

    @TaskAction
    def convert() {
        var file = inputFile.get()
        logger.info("Reading release config file '${inputFile.get().absolutePath}' ...")
        var slurper = new ConfigSlurper().parse(file.toURI().toURL())
        var yamlFactory = YAMLFactory.builder()
                .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();
        var mapper = new ObjectMapper(yamlFactory)
        var root = [
            "releases": slurper.releases.collect {release ->
                var ret = [:]
                if (release.pluginVersion != null) {
                    ret["label"] = release.pluginVersion
                }
                if (release.josmVersion != null) {
                    ret["minJosmVersion"] = release.josmVersion
                }
                if (release.description != null) {
                    ret["description"] = release.description
                }
                return ret
            }
        ]

        outputFile.get().withWriter {writer ->
            logger.info("Writing to releases YML file '${outputFile.get().absolutePath}' ...")
            mapper.writeValue(writer, root)
        }
    }
}