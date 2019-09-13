package org.openstreetmap.josm.plugins.scripting.graalvm


import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.Assert.fail

class BuiltInCommonJSModuleCompilationTest {

    static String moduleRepoPath
    static ICommonJSModuleRepository moduleRepo

    @BeforeClass
    static void readEnvironmentVariables() {
        moduleRepoPath = System.getenv("BUILTIN_COMMONJS_MODULE_REPO")
        if (moduleRepoPath == null) {
            fail("environment variable BUILTIN_COMMONJS_MODULE_REPO not set.")
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

    static def IGraalVMFacade facade
    @BeforeClass
    static void initGraalVMFacade() {
        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
    }

    @Before
    void resetRepositoryRegistry() {
        ModuleRepositories.instance.clear()
        ModuleRepositories.instance.add(moduleRepo)
    }

    @Before
    void resetContext() {
        facade.resetContext()
    }

    @Test
    void "can evaluate module 'josm/util'"() {
        def require = new RequireFunction(null)
        require.apply("josm/util")
    }

    @Test
    void "can evaluate module 'josm'"() {
        def require = new RequireFunction(null)
        require.apply("josm")
    }
}
