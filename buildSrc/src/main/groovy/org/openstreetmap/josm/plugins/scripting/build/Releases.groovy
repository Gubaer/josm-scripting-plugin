package org.openstreetmap.josm.plugins.scripting.build

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class IllegalSemanticVersion extends Exception {
    IllegalSemanticVersion(String message) {
        super(message)
    }
}

/**
 * Represents a semantic version with three numeric components, i.e.
 * <code>1.2.3</code>.
 */
class SemanticVersion implements Comparable<SemanticVersion> {
    static SemanticVersion fromLabel(String label) {
        if (label.toLowerCase().startsWith("v")) {
            return new SemanticVersion(label.substring(1))
        } else {
            return new SemanticVersion(label)
        }
    }

    private versionComponents = []

    /**
     * Creates a new semantic version from a string representation of the
     * version.
     *
     * @param version the string representation
     * @throws IllegalSemanticVersion thrown, if <code>version</code> is illegal
     */
    SemanticVersion(final String version) throws IllegalSemanticVersion{
        final components = version.split("\\.")
        if (components.length != 3) {
            throw new IllegalSemanticVersion("expected semantic version 'number.number.number', got '$version'")
        }
        components.each {
            if (! it.isInteger()) {
                throw new IllegalSemanticVersion("expected semantic version 'number.number.number', got '$version'")
            }
            versionComponents.add(it as int)
        }
    }

    @Override
    int compareTo(SemanticVersion other) {
        var result = versionComponents[0] <=> other.versionComponents[0]
        if (result != 0) {
            return result
        }
        result = versionComponents[1] <=> other.versionComponents[1]
        if (result != 0) {
            return result
        }
        return versionComponents[2] <=> other.versionComponents[2]
    }


    @Override
    String toString() {
        return versionComponents.collect {it.toString()}.join(".")
    }
}

class Release {
    public String label
    public String minJosmVersion
    public String description
}

class ReleaseList {
    public List<Release> releases
}

/**
 *
 */
@SuppressWarnings('unused')
class Releases {
    static final String RELEASES_FILE = "releases.yml"

    /**
     * Builds a releases configuration object from the content of a file given
     * by <code>path</code>
     *
     * @param path the path to the file
     * @return the releases object
     */
    static Releases fromFileWithPath(final String path=RELEASES_FILE) throws IOException {
        return fromFile(new File(path))
    }

    static Releases fromFile(final File file=new File(RELEASES_FILE)) throws IOException {
        return new Releases(file.text)
    }

    /**
     * Builds a releases configuration object from a resource.
     *
     * @param resourceName the name of the resource
     * @return the releases object
     */
    static Releases fromResource(final String resourceName) throws IOException{
        return new Releases(
            new File(getClass().getResource(resourceName).toURI()).text
        )
    }

    private ReleaseList config

    Releases(final String configuration) {
        var mapper = new ObjectMapper(new YAMLFactory())
        config = mapper.readValue(configuration, ReleaseList.class)
    }

    /**
     * Replies the highest JOSM version for which a release is available.
     * This is the JOSM version for which the current release is published.
     *
     * @return the highest JOSM version for which a release is available
     */
    String getHighestJosmVersion() {
        config.releases.collect {it.minJosmVersion as int}
            .max().toString()
    }

    static private int comparePluginLabels(v1, v2) {
        // plugin versions until Q2/2022 just consist of a number.
        // Starting with the version "0.2.0" in Q2/2022 a plugin version
        // is a semantic version "a.b.c".
        v1 = v1.toString()
        v2 = v2.toString()
        if (v1.isNumber() && v2.isNumber()) {
            return v1 <=> v2
        } else if (v1.isNumber() && !v2.isNumber()) {
            return -1
        } else if (!v1.isNumber() && v2.isNumber()) {
            return 1
        } else {
            return SemanticVersion.fromLabel(v1) <=> SemanticVersion.fromLabel(v2)
        }
    }
    /**
     * Replies the current plugin version
     *
     * @return the current plugin version
     */
    String getCurrentPluginLabel() {
        return config.releases.collect {it.label}
            .sort(this.&comparePluginLabels)
            .reverse()
            .first()
    }

    /**
     * Replies the highest available plugin version for a given
     * JOSM version
     *
     * @param josmVersion the JOSM version
     * @return the highest available plugin version
     */
    String highestPluginLabelForJosmVersion(josmVersion) {
        josmVersion = josmVersion.toString()
        config.releases
            .findAll {it.minJosmVersion.toString() == josmVersion}
            .collect {it.label}
            .sort(this.&comparePluginLabels)
            .reverse()
            .first()
    }

    /**
     * Replies a list of all JOSM versions for which a release is
     * available (in descending order)
     *
     * @return a list of the JOSM versions
     */
    List<Integer> getJosmVersions() {
        return config.releases
            .collect {it.minJosmVersion.toInteger()}
            .unique()
            .sort()
            .reverse() as int[]
    }

    int getLastCompatibleJosmVersion() {
        final versions = getJosmVersions()
        if (versions.isEmpty()) {
            return 0
        } else {
            return versions[0]
        }
    }
}
