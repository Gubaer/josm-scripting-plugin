package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.BaseJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository

import java.nio.file.Path
import java.nio.file.Paths
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
        moduleRepo = new File(new File(repoHome), "/test/data/require/modules")
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

    // testing Path.of(), Paths.get(), Path.relativize(), and Path.normalize() using relative paths.
    // This should work on the windows and the linux platform.
    @Test
    void "Path - should normalize and resolve"() {
        // can we normalize a relative path?
        def rootPath = moduleRepo.toPath()
        def repoPath = Path.of("foo/bar/../baz/./").normalize()
        def expectedRepoPath = Path.of("foo/baz")
        assertEquals(expectedRepoPath, repoPath)

        // can we concatenate an absolute root path with a module path relative to the root path?
        def fullPath = Paths.get(rootPath.toString(), repoPath.toString())
        def expectedFullPath = Paths.get(rootPath.toString(), "foo/baz")
        assertEquals(expectedFullPath.toUri(), fullPath.toUri())

        // can we relativize() to paths on the windows platform?
        def relativePath = rootPath.relativize(fullPath)
        assertEquals(Path.of("foo/baz"), relativePath)

        // on windows, if we compare paths directly, the path delimiter doesn't matter
        def p1 = Path.of("foo/bar")
        def p2 = Path.of("foo\\bar")
        assertEquals(p2, p1)

        // can we resolve two relative paths?
        p1 = Path.of("foo/bar")
        p2 = Path.of("baz.js")
        def p3 = p1.resolve(p2)
        def expectedP3 = Path.of("foo/bar/baz.js")
        assertEquals(expectedP3, p3)
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
        def moduleUri = repo.resolve("sub")
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
    void "resolve - should ignore too many _dot__dot_ segments in a module URI "() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("../../module1")
        assertTrue(moduleUri.isPresent())
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
        def contextUri = new File(moduleRepo, "sub").toURI()
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def moduleUri = repo.resolve("module2", contextUri)
        def expected = new File(moduleRepo, "module2").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should succeed for top level module id with suffix _dot_js"() {
        def contextUri = new File(moduleRepo, "sub").toURI()
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
        def contextUri = new File(moduleRepo, "sub").toURI()
        def moduleUri = repo.resolve("./foo/../module4", contextUri)
        def expected = new File(moduleRepo, "sub/module4").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept and resolve relative segments _dot_ in a module id"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def contextUri = new File(moduleRepo, "sub/module4").toURI()
        def moduleUri = repo.resolve("./module3", contextUri)
        def expected = new File(moduleRepo, "sub/module3.js").toURI()
        assertEquals(expected, moduleUri.get())
    }

    @Test
    void "resolve with context - should accept relative ids leaving with too many _dot__dot_ segments"() {
        def repo = new FileSystemJSModuleRepository(moduleRepo)
        def contextUri = new File(moduleRepo, "sub/module4").toURI()
        def moduleUri = repo.resolve("../../../../module1", contextUri)
        assertTrue(moduleUri.isPresent())
    }
}
