package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CommonJSModuleRepositoryFactoryTest extends GroovyTestCase{

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
    void "should build a repo with a valid file uri"() {
        def factory = CommonJSModuleRepositoryFactory.instance
        def uri = "file:///foo/bar".toURI()
        def repo = factory.build(uri)
        assertTrue(repo instanceof FileSystemJSModuleRepository)
    }

    @Test
    void "should build a repo with a valid jar uri"() {
        def factory = CommonJSModuleRepositoryFactory.instance
        def uri = "jar:file://${testJarFile('jar-repo-2.jar')}!/foo"
        def repo = factory.build(uri)
        assertTrue(repo instanceof JarJSModuleRepository)
    }
}
