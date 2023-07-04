package org.openstreetmap.josm.plugins.scripting.graalvm

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertEquals
import static groovy.test.GroovyAssert.shouldFail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ModuleJarURITest {

    static def projectHome

    @BeforeAll
    static void readEnvironmentVariables() {
        projectHome = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        if (projectHome == null) {
            throw new Exception(
               "environment variable JOSM_SCRIPTING_PLUGIN_HOME missing")
        }
    }

    static String jarReposBaseDir() {
        return "${projectHome}/test/data/jar-repos"
    }

    static File testJarFile(String name){
        return new File(jarReposBaseDir(), name)
    }

    @Test
    void "constructor - accept valid URI"() {
        def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}!/foo/bar.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertEquals("/foo/bar.js", uri.getJarEntryPathAsString())
        assertEquals(
            testJarFile('jar-repo-1.jar').toString(),
            uri.getJarFilePath())
    }

    @Test
    void "constructor - reject http URI"() {
        shouldFail(IllegalArgumentException.class) {
            def urispec = "http://www.foo.bar"
            new ModuleJarURI(new URI(urispec))
        }
    }

    @Test
    void "constructor - reject embedded http URI"() {
        shouldFail(IllegalArgumentException.class) {
            def urispec = "jar:http://www.foo.bar!/foo/bar.js"
            new ModuleJarURI(new URI(urispec))
        }
    }

    @Test
    void "constructor - reject when jar entry path missing"() {
        shouldFail(IllegalArgumentException.class) {
            def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}"
            new ModuleJarURI(new URI(urispec))
        }
    }

    @Test
    void "constructor - reject when jar entry path not absolute"() {
        shouldFail(IllegalArgumentException.class) {
            def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}!../foo.js"
            new ModuleJarURI(new URI(urispec))
        }
    }

    @Test
    void "refersToReadableFile - true for existing readable file"() {
        def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertTrue(uri.refersToReadableFile())
    }

    @Test
    void "refersToReadableFile - false for non-existing file"() {
        def urispec = "jar:file://${testJarFile('no-such-jar.jar')}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToReadableFile())
    }

    @Test
    void "refersToReadableFile - false for directory"() {
        def urispec = "jar:file://${jarReposBaseDir()}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToReadableFile())
    }

    @Test
    void "refersToJarFile - true for existing jar file"() {
        def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertTrue(uri.refersToJarFile())
    }

    @Test
    void "refersToJarFile - false for non-existing file"() {
        def urispec = "jar:file://${testJarFile('no-such-jar.jar')}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToJarFile())
    }

    @Test
    void "refersToJarFile - false for directory"() {
        def urispec = "jar:file://${jarReposBaseDir()}!/foo.js"
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToJarFile())
    }

    @Test
    void "normalized - should normalize file path and jar entry path"() {
        def filepath = "${jarReposBaseDir()}/test/data/jar-repos/foo/.././../jar-repos/jar-repo-1.jar"
        def jarEntryPath = "/foo/../bar/./../foo.js"
        def urispec = "jar:file://${filepath}!${jarEntryPath}".toString()
        def uri = new ModuleJarURI(new URI(urispec))
        def normalizedUri = uri.normalized()
        assertEquals(
            "${jarReposBaseDir()}/test/data/jar-repos/jar-repo-1.jar".toString(),
            normalizedUri.jarFilePath)
        assertEquals(
            "/foo.js",
            normalizedUri.jarEntryPathAsString)
    }

    @Test
    void "toUri - should build jar URI"() {
        def urispec = "jar:file://${testJarFile('jar-repo-1.jar')}!/foo/bar.js".toString()
        def uri = new ModuleJarURI(new URI(urispec))
        def converted = uri.toURI().toString()
        assertEquals(urispec, converted)
    }

    @Test
    void "refersToDirectoryJarEntry - true, if directory exists"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo"
                .toString()
        def uri = new ModuleJarURI(new URI(urispec))
        assertTrue(uri.refersToDirectoryJarEntry())
    }

    @Test
    void "refersToDirectoryJarEntry - false, if entry does not exist"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/no-such-entry"
                .toString()
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToDirectoryJarEntry())
    }

    @Test
    void "refersToDirectoryJarEntry - false, if entry is a file"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo/bar.js"
                .toString()
        def uri = new ModuleJarURI(new URI(urispec))
        assertFalse(uri.refersToDirectoryJarEntry())
    }

    @Test
    void "isBaseOf - true, if other is a 'child' URI"() {

        def base = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo"
                .toString()
        def other = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo/bar.js"
                .toString()
        def baseUri = new ModuleJarURI(new URI(base))
        def otherUri = new ModuleJarURI(new URI(other))
        assertTrue(baseUri.isBaseOf(otherUri))
    }

    @Test
    void "isBaseOf - false, if other refers to different file"() {

        def base = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo"
                .toString()
        // not the same jar file
        def other = "jar:file://${testJarFile('jar-repo-1.jar')}!/foo/bar.js"
                .toString()
        def baseUri = new ModuleJarURI(new URI(base))
        def otherUri = new ModuleJarURI(new URI(other))
        assertFalse(baseUri.isBaseOf(otherUri))
    }

    @Test
    void "isBaseOf - false, if other jar entry path isn't a prefix"() {

        def base = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo"
                .toString()
        // /foo isn't a prefix of /bar.js
        def other = "jar:file://${testJarFile('jar-repo-2.jar')}!/bar.js"
                .toString()
        def baseUri = new ModuleJarURI(new URI(base))
        def otherUri = new ModuleJarURI(new URI(other))
        assertFalse(baseUri.isBaseOf(otherUri))
    }

    @Test
    void "toResolutionContextUri - identical, if jar entry path is a forward slash"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/".toString()
        def uri = new ModuleJarURI(new URI(urispec))
        def resolutionContextUri = uri.toResolutionContextUri()
        assertEquals(urispec, resolutionContextUri.toString())
    }

    @Test
    void "toResolutionContextUri - identical, if jar entry path is a dir"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo".toString()
        def uri = new ModuleJarURI(new URI(urispec))
        def resolutionContextUri = uri.toResolutionContextUri()
        assertEquals(urispec, resolutionContextUri.toString())
    }

    @Test
    void "toResolutionContextUri - parent, if jar entry path is a file"() {
        def urispec = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo/baz.js".toString()
        def expectd = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo".toString()
        def uri = new ModuleJarURI(new URI(urispec))
        def resolutionContextUri = uri.toResolutionContextUri()
        assertEquals(expectd, resolutionContextUri.toString())
    }
}

