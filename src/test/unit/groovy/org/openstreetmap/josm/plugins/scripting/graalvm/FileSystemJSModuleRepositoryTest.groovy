package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.BaseJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

import static groovy.test.GroovyAssert.fail
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class FileSystemJSModuleRepositoryTest {

    private static File moduleRepo

    @BeforeAll
    static void readEnvironmentVariables() {
        def repoHome = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME")
        if (repoHome == null) {
            fail("environment variable JOSM_SCRIPTING_PLUGIN_HOME not set")
        }
        moduleRepo = new File(new File(repoHome), "/src/test/resources/require/modules")
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
        def repo = new FileSystemJSModuleRepository(moduleRepo.toString())
        assertEquals(moduleRepo.toURI(), repo.baseURI)
    }

    @Test
    void "should create with valid base dir as file"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        assertEquals(moduleRepo.toURI(), repo.baseURI)
    }

    @SuppressWarnings('GroovyResultOfObjectAllocationIgnored')
    @Test
    void "should reject null as base dir"() {
        shouldFail(NullPointerException.class) {
            new FileSystemJSModuleRepository(null as String)
        }
        shouldFail(NullPointerException.class) {
            new FileSystemJSModuleRepository(null as File)
        }
    }

    @Test
    void "should reject relative directory as base dir"() {
        shouldFail(IllegalArgumentException.class) {
            new FileSystemJSModuleRepository("foo/bar")
        }
        shouldFail(IllegalArgumentException.class) {
            new FileSystemJSModuleRepository("..\\foo\\bar")
        }
    }

    @SuppressWarnings('GroovyUnusedAssignment')
    @Test
    void "resolve - should fail if repo base doesn't exist"() {
        def base= new File(moduleRepo, "no-such-directory")
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("./my-module")
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve - should succeed for top level module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("module2")
        def expected = new File(moduleRepo, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should succeed for top level module id with suffix js"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("module1")
        def expected = new File(moduleRepo, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should reject id with leading forward slash"() {
        shouldFail(IllegalArgumentException.class) {
            def repo = new FileSystemJSModuleRepository(moduleRepo)
            repo.resolve("/module2")
        }
    }

    @Test
    void "resolve - should reject a module which refers to a directory"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("emptyDir")
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve - should accept relative segments _dot__dot_ in a module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("sub/../module2")
        def expected = new File(moduleRepo, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should accept relative segments _dot_ in a module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("./sub/.././module2")
        def expected = new File(moduleRepo, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve - should check for too many _dot__dot_ segments in a module URI and fail to resolve "() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("../../module1")
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "isBase - should work for context URIs starting with file_colon__slash__slash__slash_"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = new File(moduleRepo, "module3.js").toURI()
        assertTrue(repo.isBaseOf(moduleUri))
    }


    // ----------------------------------------------------------------------
    // resolution against a context path
    @Test
    void "resolve with context - should succeed for top level module id"() {
        def contextUri = moduleRepo.toURI()
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("module2", contextUri)
        def expected = new File(moduleRepo, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should succeed for top level module id with suffix _dot_js"() {
        def contextUri = moduleRepo.toURI()
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("module1", contextUri)
        def expected = new File(moduleRepo, "module1.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should reject id with leading _slash_"() {
        shouldFail(IllegalArgumentException.class) {
            def contextUri = new File(moduleRepo, "sub").toURI()
            def repo = new FileSystemJSModuleRepository(moduleRepo)
            repo.resolve("/module2", contextUri)
        }
    }

    @Test
    void "resolve with context - should reject a module which refers to a directory"() {
        def contextUri = new File(moduleRepo, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("./foo", contextUri)
        assertFalse(moduleUri.isPresent())
    }

    @Test
    void "resolve with context - should accept and resolve relative segments _dot__dot_ in a module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        // sub/module4 is a source file!
        def contextUri = new File(moduleRepo, "sub/module4").toURI()
        def moduleUri = repo.resolve("./foo/../module4", contextUri)
        def expected = new File(moduleRepo, "sub/module4").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept and resolve relative segments _dot_ in a module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        // sub/module4 is a source file!
        def contextUri = new File(moduleRepo, "sub/module4").toURI()
        def moduleUri = repo.resolve("./module3", contextUri)
        def expected = new File(moduleRepo, "sub/module3.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept relative ids leading with too many _dot__dot_ segments"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def contextUri = new File(moduleRepo, "sub/module4").toURI()
        def moduleUri = repo.resolve("../../../../module1", contextUri)
        assertFalse(moduleUri.isPresent())
    }
}
