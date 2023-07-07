package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryRegistry
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.ICommonJSModuleRepository

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

import static groovy.test.GroovyAssert.fail
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.*

class ModuleRepositoriesTest extends JOSMFixtureBasedTest {

    @BeforeEach
    void clearRepositories() {
        CommonJSModuleRepositoryRegistry.instance.clear()
    }

    @Test
    void "singleton instance should be available"() {
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        assertNotNull(repos)
    }

    @Test
    void "add - can add a user defined repository"() {
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(), "foo/bar"))
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(repo)
        assertNotNull(repos.userDefinedRepositories.find {
            r -> r.baseURI == repo.baseURI
        })
    }

    @Test
    void "add - don't add a user defined repository twice"() {
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(),"foo/bar"))
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(repo)
        repos.addUserDefinedRepository(repo)
        assertEquals(1,
                repos.userDefinedRepositories.findAll {
                    r -> r.baseURI == repo.baseURI
                }.size()
        )
    }

    @Test
    void "add - don't add a user defined null repository"() {
        shouldFail(NullPointerException.class) {
            CommonJSModuleRepositoryRegistry.instance.addUserDefinedRepository(null)
        }
    }

    @Test
    void "remove - can remove a user defined repo"() {
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(),"foo/bar"))
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(repo)
        repos.removeUserDefinedRepository(repo)
        assertTrue(repos.userDefinedRepositories.isEmpty())
    }

    @Test
    void "remove - can remove a user defined repo given a base URI"() {
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(),"foo/bar"))
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(repo)
        repos.removeUserDefinedRepository(repo.baseURI)
        assertTrue(repos.userDefinedRepositories.isEmpty())
    }

    @Test
    void "remove - reject null repo"() {
        shouldFail(NullPointerException.class) {
            CommonJSModuleRepositoryRegistry.instance.removeUserDefinedRepository(null as ICommonJSModuleRepository)
        }
    }

    @Test
    void "remove - reject null base URI when removing a user defined repo"() {
        shouldFail(NullPointerException.class) {
            CommonJSModuleRepositoryRegistry.instance.removeUserDefinedRepository(null as URI)
        }
    }

    @Test
    void "clear - can clear the registry"() {
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            new File(getProjectHome(),"foo/bar1")))
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            new File(getProjectHome(),"foo/bar2")))
        repos.clear()
        assertTrue(repos.userDefinedRepositories.isEmpty())
    }

    @Test
    void "getRepositories - can retrieve the repositories"() {
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            new File(getProjectHome(),"foo/bar1")))
        repos.addUserDefinedRepository(new FileSystemJSModuleRepository(
            new File(getProjectHome(), "foo/bar2")))
        def registeredRepos = repos.userDefinedRepositories
        assertEquals(2, registeredRepos.size())
    }

    @Test
    void "getRepositoryForModule - does correctly lookup a module"() {
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(),"foo/bar1"))
        repos.addUserDefinedRepository(repo)
        def moduleUri = new File(new File(repo.baseURI), "my/module.js").toURI()

        def lookedUpRepo = repos.getRepositoryForModule(moduleUri)
        assertTrue(lookedUpRepo.isPresent())
        assertEquals(repo.baseURI, lookedUpRepo.get().baseURI)
    }

    @Test
    void "getRepositoryForModule - doesn't find an arbitrary module URI"() {
        def repos = CommonJSModuleRepositoryRegistry.getInstance()
        def repo = new FileSystemJSModuleRepository(new File(getProjectHome(),"foo/bar1"))
        repos.addUserDefinedRepository(repo)
        def moduleUri = new File("/yet/another/location/my/module.js").toURI()

        def lookedUpRepo = repos.getRepositoryForModule(moduleUri)
        assertFalse(lookedUpRepo.isPresent())
    }

    @Test
    void "getRepositoryForModule - reject null module URI"() {
        shouldFail(NullPointerException.class) {
            CommonJSModuleRepositoryRegistry.instance.getRepositoryForModule(null)
        }
    }
}

class ModuleRepositoriesResolveTest extends JOSMFixtureBasedTest{

    static String moduleRepoPath
    static ICommonJSModuleRepository moduleRepo

    @BeforeAll
    static void readEnvironmentVariables() {
        moduleRepoPath = System.getenv("TEST_COMMONJS_MODULE_REPO")
        if (moduleRepoPath == null) {
            fail("environment variable TEST_COMMONJS_MODULE_REPO not set.")
        }

        moduleRepo = new FileSystemJSModuleRepository(moduleRepoPath)
    }

    @BeforeAll
    static void enableLogging() {
        Logger.getLogger(FileSystemJSModuleRepository.class.getName())
                .setLevel(Level.FINE)

        Logger.getLogger("")
                .getHandlers().findAll {
            it instanceof ConsoleHandler
        }.each {
            it.level = Level.FINE
        }
    }

    @BeforeEach
    void resetRepositoryRegistry() {
        CommonJSModuleRepositoryRegistry.instance.clear()
        CommonJSModuleRepositoryRegistry.instance.addUserDefinedRepository(moduleRepo)
    }

    @Test
    void "can resolve an existing module"() {
        def moduleId = "module1"
        def moduleUri = CommonJSModuleRepositoryRegistry.instance.resolve(moduleId)
        assertTrue(moduleUri.isPresent())
        moduleRepo.isBaseOf(moduleUri.get())
    }

    @Test
    void "can resolve an existing module with a context"() {
        def moduleId = "../module2"
        def contextUri = new File(
            new File(moduleRepo.baseURI),
            "sub/module3.js"
        ).toURI()
        def moduleUri = CommonJSModuleRepositoryRegistry.instance.resolve(moduleId, contextUri)
        assertTrue(moduleUri.isPresent())
        moduleRepo.isBaseOf(moduleUri.get())
    }
}

