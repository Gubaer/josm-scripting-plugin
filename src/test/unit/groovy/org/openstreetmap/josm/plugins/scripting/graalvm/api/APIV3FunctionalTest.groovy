package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.FileSystemESModuleRepository

class APIV3FunctionalTest extends AbstractGraalVMBasedTest{

    static private ESModuleResolver resolver

    @BeforeAll
    static void setupResolverAndContext() {
        resolver = ESModuleResolver.instance
        final repo = new FileSystemESModuleRepository(new File(
            getProjectHome(),
            "src/test/functional/javascript/v3"
        ))
        resolver.setUserDefinedRepositories(List.of(repo))
    }

    final def testModules = [
        "command-add-test",
        "command-change-test",
        "command-delete-test",
        "command-undo-redo-test",
        "ds-test",
        "layer-test",
        "menu-bar-test",
        "menu-test",
        "open-files-test"
    ]

    @Test
    void "can execute functional tests"() {
        testModules.forEach(module -> {
            facade.resetContext()
            getLogger().info("Running tests in module $module ...")
            final source = """
                import {run} from '$module'
                run()
                """
            facade.eval(graalJSDescriptor, source)
        })
    }
}
