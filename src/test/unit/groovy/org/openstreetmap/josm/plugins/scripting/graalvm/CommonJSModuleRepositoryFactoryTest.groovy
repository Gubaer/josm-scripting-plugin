package org.openstreetmap.josm.plugins.scripting.graalvm

import static org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryFactory
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.FileSystemJSModuleRepository
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.JarJSModuleRepository

class CommonJSModuleRepositoryFactoryTest {

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
        return "${projectHome}/src/test/resources/jar-repos"
    }

    static File testJarFile(String name){
        return new File(jarReposBaseDir(), name)
    }

    @Test
    void "should build a repo with a valid file uri"() {
        def factory = CommonJSModuleRepositoryFactory.instance
        def uri = new File("/foo/bar").toURI()
        def repo = factory.build(uri)
        assertTrue(repo instanceof FileSystemJSModuleRepository)
    }

    @Test
    void "should build a repo with a valid jar uri"() {
        def factory = CommonJSModuleRepositoryFactory.instance
        def uri = "jar:${testJarFile('jar-repo-2.jar').toURI().toString()}!/foo"
        def repo = factory.build(uri)
        assertTrue(repo instanceof JarJSModuleRepository)
    }
}
