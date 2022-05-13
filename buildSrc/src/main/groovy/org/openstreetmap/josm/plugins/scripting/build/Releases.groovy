package org.openstreetmap.josm.plugins.scripting.build

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
}

/**
 *
 */
@SuppressWarnings('unused')
class Releases {
    static final String RELEASES_FILE = "releases.conf"

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

    private ConfigObject config

    Releases(final String configuration) {
        config = new ConfigSlurper().parse(configuration)
    }

    /**
     * Replies the highest JOSM version for which a release is available.
     * This is the JOSM version for which the current release is published.
     *
     * @return the highest JOSM version for which a release is available
     */
    String highestJosmVersion() {
        config.releases.collect {it.josmVersion}.max().toString()
    }

    static private int comparePluginVersions(v1, v2) {
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
            return new SemanticVersion(v1) <=> new SemanticVersion(v2)
        }
    }
    /**
     * Replies the current plugin version
     *
     * @return the current plugin version
     */
    String currentPluginVersion() {
        return config.releases.collect {it.pluginVersion}
            .sort(this.&comparePluginVersions)
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
    String highestPluginVersionForJosmVersion(josmVersion) {
        josmVersion = josmVersion.toString()
        config.releases
            .findAll {it.josmVersion.toString() == josmVersion}
            .collect {it.pluginVersion}
            .sort(this.&comparePluginVersions)
            .reverse()
            .first()
    }

    /**
     * Replies a list of all JOSM versions for which a release is
     * available (in descending order)
     *
     * @return a list of the JOSM versions
     */
    int[] getJosmVersions() {
        return config.releases
            .collect {it.josmVersion.toInteger()}
            .unique()
            .reverse() as int[]
    }
}
