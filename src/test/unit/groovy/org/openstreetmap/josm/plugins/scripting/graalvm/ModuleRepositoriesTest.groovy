package org.openstreetmap.josm.plugins.scripting.graalvm


import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.fail


class ModuleRepositoriesTest {

    @Before
    void clearRepositories() {
        ModuleRepositories.instance.clear()
    }

    @Test
    void "singleton instance should be available"() {
        def repos = ModuleRepositories.getInstance()
        assertNotNull(repos)
    }

    @Test
    void "add: can add a repository"() {
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar"))
        def repos = ModuleRepositories.getInstance()
        repos.add(repo)
        assertNotNull(repos.repositories.find {
            r -> r.baseURI == repo.baseURI
        })
    }

    @Test
    void "add: don't add a repository twice"() {
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar"))
        def repos = ModuleRepositories.getInstance()
        repos.add(repo)
        repos.add(repo)
        assertEquals(1,
                repos.repositories.findAll {
                    r -> r.baseURI == repo.baseURI
                }.size()
        )
    }

    @Test(expected = NullPointerException)
    void "add: don't add a null repository"() {
        ModuleRepositories.instance.add(null)
    }

    @Test
    void "remove: can remove a repo"() {
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar"))
        def repos = ModuleRepositories.getInstance()
        repos.add(repo)
        repos.remove(repo)
        assertTrue(repos.repositories.isEmpty())
    }

    @Test
    void "remove: can remove a repo given a base URI"() {
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar"))
        def repos = ModuleRepositories.getInstance()
        repos.add(repo)
        repos.remove(repo.baseURI)
        assertTrue(repos.repositories.isEmpty())
    }

    @Test(expected = NullPointerException)
    void "remove: reject null repo"() {
        ModuleRepositories.instance.remove(null as ICommonJSModuleRepository)
    }

    @Test(expected = NullPointerException)
    void "remove: reject null base URI"() {
        ModuleRepositories.instance.remove(null as URI)
    }

    @Test
    void "clear: can clear the registry"() {
        def repos = ModuleRepositories.getInstance()
        repos.add(new FileSystemJSModuleRepository(new File("/foo/bar1")))
        repos.add(new FileSystemJSModuleRepository(new File("/foo/bar2")))
        repos.clear()
        assertTrue(repos.repositories.isEmpty())
    }

    @Test
    void "getRepositories: can retrieve the repositories"() {
        def repos = ModuleRepositories.getInstance()
        repos.add(new FileSystemJSModuleRepository(new File("/foo/bar1")))
        repos.add(new FileSystemJSModuleRepository(new File("/foo/bar2")))
        def registeredRepos = repos.repositories
        assertEquals(2, registeredRepos.size())
    }

    @Test
    void "getRepositoryForModule: does correctly lookup a module"() {
        def repos = ModuleRepositories.getInstance()
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar1"))
        repos.add(repo)
        def moduleUri = new File(new File(repo.baseURI), "my/module.js").toURI()

        def lookedUpRepo = repos.getRepositoryForModule(moduleUri)
        assertTrue(lookedUpRepo.isPresent())
        assertEquals(repo.baseURI, lookedUpRepo.get().baseURI)
    }

    @Test
    void "getRepositoryForModule: doesn't find an arbitrary module URI"() {
        def repos = ModuleRepositories.getInstance()
        def repo = new FileSystemJSModuleRepository(new File("/foo/bar1"))
        repos.add(repo)
        def moduleUri = new File("/yet/another/location/my/module.js").toURI()

        def lookedUpRepo = repos.getRepositoryForModule(moduleUri)
        assertFalse(lookedUpRepo.isPresent())
    }

    @Test(expected = NullPointerException)
    void "getRepositoryForModule: reject null module URI"() {
        ModuleRepositories.instance.getRepositoryForModule(null)
    }
}

class ModuleRepositoriesResolveTest {

    static def String moduleRepoPath
    static def ICommonJSModuleRepository moduleRepo

    @BeforeClass
    static void readEnvironmentVariables() {
        moduleRepoPath = System.getenv("TEST_COMMONJS_MODULE_REPO")
        if (moduleRepoPath == null) {
            fail("environment variable TEST_COMMONJS_MODULE_REPO not set.")
        }

        moduleRepo = new FileSystemJSModuleRepository(moduleRepoPath)
    }

    @BeforeClass
    static void enableLogging() {
        Logger.getLogger(FileSystemJSModuleRepository.class.getName())
                .setLevel(Level.FINE);

        Logger.getLogger("")
                .getHandlers().findAll {
            it instanceof ConsoleHandler
        }.each {
            it.level = Level.FINE
        }
    }

    @Before
    void resetRepositoryRegistry() {
        ModuleRepositories.instance.clear()
        ModuleRepositories.instance.add(moduleRepo)
    }

    @Test
    void "can resolve an existing module"() {
        def moduleId = "module1"
        def moduleUri = ModuleRepositories.instance.resolve(moduleId)
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
        def moduleUri = ModuleRepositories.instance.resolve(moduleId, contextUri)
        assertTrue(moduleUri.isPresent())
        moduleRepo.isBaseOf(moduleUri.get())
    }

}

