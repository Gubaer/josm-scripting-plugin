package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.BeforeClass
import org.junit.Test

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.Assert.*

class FileSystemJSModuleRepositoryTest {

    static def moduleRepo

    @BeforeClass
    static void readEnvironmentVariables() {
        moduleRepo = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        if (moduleRepo == null) {
            fail("environment variable JOSM_SCRIPTING_PLUGIN_HOME not set.")
        }
        moduleRepo += "/test/data/require/modules"
    }

    @BeforeClass
    static void enableLogging() {
        Logger.getLogger(BaseJSModuleRepository.class.getName())
             .setLevel(Level.FINE);

        Logger.getLogger("")
            .getHandlers().findAll {
                it instanceof ConsoleHandler
            }.each {
                it.level = Level.FINE
            }
    }

    @Test
    void "should create with valid base dir"() {
        def root = "/foo/bar"
        def repo = new FileSystemJSModuleRepository(root)
        assertEquals("file:/foo/bar", repo.baseURI.toString())
    }

    @Test
    void "should create with valid base dir as file"() {
        def root = "/foo/bar"
        def repo = new FileSystemJSModuleRepository(new File(root))
        assertEquals("file:/foo/bar", repo.baseURI.toString())
    }

    @Test(expected = NullPointerException)
    void "should reject null as base dir"() {
        new FileSystemJSModuleRepository(null as String)
        new FileSystemJSModuleRepository(null as File)
    }

    @Test(expected = IllegalStateException)
    void "resolve: should fail if repo base doesn't exist"() {
        def base = "/no/such/dir"
        def repo = new FileSystemJSModuleRepository(base)
        def moduleUri = repo.resolve("./my-module")
    }

    @Test
    void "resolve: should succeed for toplevel module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve: should succeed for toplevel module id with suffix .js"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module1")
        def expected = new File(baseDir, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test(expected = IllegalArgumentException)
    void "resolve: should reject id with leading '/'"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        repo.resolve("/module2")
    }

    @Test
    void "resolve: should reject a module which refers to a directory"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("sub")
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve: should accept relative segments '..' in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("sub/../module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve: should accept relative segments '.' in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("./sub/.././module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve: should ignore too many .. segments in a module URI "() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("../../module1")
        assertTrue(moduleUri.isPresent())
    }

    @Test
    void "isBase: should work for context URIs starting with file:///"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = new URI("file://" + baseDir.absolutePath + "/module3.js")
        assertTrue(repo.isBaseOf(moduleUri))
    }


    // ----------------------------------------------------------------------
    // resolution against a context path

    void "resolve with context: should succeed for toplevel module id"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module2", contextUri)
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context: should succeed for toplevel module id with suffix .js"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module1", contextUri)
        def expected = new File(baseDir, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test(expected = IllegalArgumentException)
    void "resolve with context: should reject id with leading '/'"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        repo.resolve("/module2", contextUri)
    }

    @Test
    void "resolve with context: should reject a module which refers to a directory"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("./foo", contextUri)
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve with context: should accept and resolve relative segments '..' in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub").toURI()
        def moduleUri = repo.resolve("./foo/../module4", contextUri)
        def expected = new File(baseDir, "sub/module4").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context: should accept and resolve relative segments '.' in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub/module4").toURI()
        def moduleUri = repo.resolve("./module3", contextUri)
        def expected = new File(baseDir, "sub/module3.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context: should accept relative ids leaving with too many .. segments"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub/module4").toURI()
        def moduleUri = repo.resolve("../../../../module1", contextUri)
        assertTrue(moduleUri.isPresent())
    }


}
