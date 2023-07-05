package org.openstreetmap.josm.plugins.scripting.graalvm

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertEquals
import static groovy.test.GroovyAssert.shouldFail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.BaseJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

class FileSystemJSModuleRepositoryTest {

    private static String moduleRepo

    @BeforeAll
    static void readEnvironmentVariables() {
        moduleRepo = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        if (moduleRepo == null) {
            fail("environment variable JOSM_SCRIPTING_PLUGIN_HOME not set.")
        }
        moduleRepo += "/test/data/require/modules"
    }

    @BeforeAll
    static void enableLogging() {
        Logger.getLogger(BaseJSModuleRepository.class.getName())
             .setLevel(Level.FINE)

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

    @SuppressWarnings('GroovyResultOfObjectAllocationIgnored')
    @Test
    void "should reject null as base dir"() {
        shouldFail(NullPointerException.class) {
            new FileSystemJSModuleRepository(null as String)
            new FileSystemJSModuleRepository(null as File)
        }
    }

    @SuppressWarnings('GroovyUnusedAssignment')
    @Test
    void "resolve - should fail if repo base doesn't exist"() {
        shouldFail(IllegalStateException.class) {
            def base = "/no/such/dir"
            def repo = new FileSystemJSModuleRepository(base)
            def moduleUri = repo.resolve("./my-module")
        }
    }

    @Test
    void "resolve - should succeed for top level module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should succeed for top level module id with suffix js"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module1")
        def expected = new File(baseDir, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should reject id with leading forward slash"() {
        shouldFail(IllegalArgumentException.class) {
            def baseDir = new File(moduleRepo)
            def repo = new FileSystemJSModuleRepository(baseDir)
            repo.resolve("/module2")
        }
    }

    @Test
    void "resolve - should reject a module which refers to a directory"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("sub")
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve - should accept relative segments _dot__dot_ in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("sub/../module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should accept relative segments _dot_ in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("./sub/.././module2")
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should ignore too many _dot__dot_ segments in a module URI "() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("../../module1")
        assertTrue(moduleUri.isPresent())
    }

    @Test
    void "isBase - should work for context URIs starting with file_colon__slash__slash__slash_"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = new URI("file://" + baseDir.absolutePath + "/module3.js")
        assertTrue(repo.isBaseOf(moduleUri))
    }


    // ----------------------------------------------------------------------
    // resolution against a context path
    @Test
    void "resolve with context - should succeed for top level module id"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module2", contextUri)
        def expected = new File(baseDir, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should succeed for top level module id with suffix _dot_js"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("module1", contextUri)
        def expected = new File(baseDir, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should reject id with leading _slash_"() {
        shouldFail(IllegalArgumentException.class) {
            def baseDir = new File(moduleRepo)
            def contextUri = new File(baseDir, "sub").toURI()
            def repo = new FileSystemJSModuleRepository(baseDir)
            repo.resolve("/module2", contextUri)
        }
    }

    @Test
    void "resolve with context - should reject a module which refers to a directory"() {
        def baseDir = new File(moduleRepo)
        def contextUri = new File(baseDir, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(baseDir)
        def moduleUri = repo.resolve("./foo", contextUri)
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve with context - should accept and resolve relative segments _dot__dot_ in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub").toURI()
        def moduleUri = repo.resolve("./foo/../module4", contextUri)
        def expected = new File(baseDir, "sub/module4").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept and resolve relative segments _dot_ in a module id"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub/module4").toURI()
        def moduleUri = repo.resolve("./module3", contextUri)
        def expected = new File(baseDir, "sub/module3.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept relative ids leaving with too many _dot__dot_ segments"() {
        def baseDir = new File(moduleRepo)
        def repo = new FileSystemJSModuleRepository(baseDir)
        def contextUri = new File(baseDir, "sub/module4").toURI()
        def moduleUri = repo.resolve("../../../../module1", contextUri)
        assertTrue(moduleUri.isPresent())
    }
}
