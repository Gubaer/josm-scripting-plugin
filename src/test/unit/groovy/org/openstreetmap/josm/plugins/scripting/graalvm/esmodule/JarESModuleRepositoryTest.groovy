package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase
import org.openstreetmap.josm.plugins.scripting.model.RelativePath

import java.nio.channels.Channels

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class JarESModuleRepositoryTest extends BaseTestCase {

    @Test
    void "can create a repo"() {
        final file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        final repo = new JarESModuleRepository(file)
        assertNotNull(repo)
    }

    @Test
    void "can create a repo with an existing zip entry as root"() {
        def file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        def root = RelativePath.of("josm")
        def repo = new JarESModuleRepository(file, root)
        assertNotNull(repo)

        // should work with an absolute path too
        root = RelativePath.of("josm")
        repo = new JarESModuleRepository(file, root)
        assertNotNull(repo)
    }

    @Test
    void "reject creating a repo when jar file doesn't exist"()  {
        final file = new File("no-such-jar.jar")
        shouldFail(IOException) {
            new JarESModuleRepository(file)
        }
    }

    @Test
    void "reject creating a repo with nulls as parameters"() {
        shouldFail(NullPointerException) {
            new JarESModuleRepository(null, RelativePath.of("foo"))
        }
        shouldFail(NullPointerException) {
            new JarESModuleRepository(new File("foo"), null)
        }
        shouldFail(NullPointerException) {
            new JarESModuleRepository(null, null)
        }
    }

    @Test
    void "reject creating a repo with non existing zip entry for the root"() {
        final file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        final root = RelativePath.parse("no/such/root")
        shouldFail(IllegalArgumentException) {
            new JarESModuleRepository(file, root)
        }
    }

    @Test
    void "can create a repo with a valid jar URI"() {
        final jarUri = "jar:${getProjectHome().toURI()}/src/test/resources/es-modules/es-modules.jar!/"
        final repo = new JarESModuleRepository(new URI(jarUri))
        assertNotNull(repo)
    }

    @Test
    void "reject invalid jar URIs to create a repo"() {
        shouldFail(NullPointerException) {
            new JarESModuleRepository((URI) null)
        }

        shouldFail(IllegalESModuleBaseUri) {
            // non-existing jar
            final jarUri = "jar:${getProjectHome().toURI()}/no-such-jar.jar!/"
            new JarESModuleRepository(new URI(jarUri))
        }

        shouldFail (IllegalESModuleBaseUri){
            // non-existing root entry
            final jarUri = "jar:${getProjectHome().toURI()}/src/test/resources/es-modules/es-modules.jar!/no-such-entry"
            new JarESModuleRepository(new URI(jarUri))
        }
    }

    private JarESModuleRepository repo

    @BeforeEach
    void initRepo() {
        final file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        repo = new JarESModuleRepository(file)
    }

    @Test
    void "can resolve path to existing module in a jar file"() {

        def modulePath = RelativePath.of("foo")
        def resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertTrue(resolvedPath.startsWith(repo.getUniquePathPrefix()))

        modulePath = RelativePath.parse("./foo")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append("foo"), resolvedPath)

        modulePath = RelativePath.parse("foo.mjs")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append("foo.mjs"), resolvedPath)

        modulePath = RelativePath.parse("sub/../foo")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append("foo"), resolvedPath)

        modulePath = repo.getUniquePathPrefix().append("foo")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append("foo.mjs"), resolvedPath)

        modulePath = RelativePath.parse("sub/bar")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append(RelativePath.parse("sub/bar")), resolvedPath)

        modulePath = RelativePath.parse("sub/bar.mjs")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append(RelativePath.parse("sub/bar.mjs")), resolvedPath)

        modulePath = RelativePath.parse("sub/././bar.mjs")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append(RelativePath.parse("sub/bar.mjs")), resolvedPath)

        modulePath = RelativePath.parse("sub/baz/.././bar")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append(RelativePath.parse("sub/bar")), resolvedPath)

        // resolves against a .js file
        modulePath = RelativePath.parse("sub/baz")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertEquals(repo.getUniquePathPrefix().append(RelativePath.parse("sub/baz")), resolvedPath)
    }

    @Test
    void "rejects resolution to non-existing modules"() {
        def modulePath = RelativePath.parse("no-such-module")
        def resolvedPath = repo.resolveModulePath(modulePath)
        assertNull(resolvedPath)

        // sub is a directory entry, not a file entry
        modulePath = RelativePath.parse("sub")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNull(resolvedPath)

        // no sub/baz, sub/baz.js, or sub/baz.mjs
        modulePath = RelativePath.parse("sub/no-such-module")
        resolvedPath = repo.resolveModulePath(modulePath)
        assertNull(resolvedPath)
    }

    @Test
    void "can read existing module from channel"() {

        def modulePath = RelativePath.parse("foo")
        def resolvedPath = repo.resolveModulePath(modulePath)
        assertNotNull(resolvedPath)
        assertTrue(resolvedPath.startsWith(repo.getUniquePathPrefix()))

        def channel = repo.newByteChannel(resolvedPath)
        assertNotNull(channel)
        def content = Channels.newReader(channel, "utf-8").text
        assertTrue(content.indexOf("foo") >= 0)
    }

    @Test
    void "fails read of non-existing module with IllegalArgumentException"() {
        def resolvedPath = repo.getUniquePathPrefix().append("no-such-module")
        shouldFail(IllegalArgumentException) {
            repo.newByteChannel(resolvedPath)
        }
    }
}
