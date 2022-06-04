package org.openstreetmap.josm.plugins.scripting.graalvm.with_graalvm


import org.graalvm.polyglot.Context
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.*

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

class RequireFunctionTest extends AbstractGraalVMBasedTest {

    @BeforeAll
    static void enableLogging() {
        Logger.getLogger(FileSystemJSModuleRepository.class.getName())
                .setLevel(Level.FINE)
        Logger.getLogger(RequireFunction.class.getName())
                .setLevel(Level.FINE)
        Logger.getLogger("").getHandlers().findAll {
            it instanceof ConsoleHandler
        }.each {
            it.level = Level.FINE
        }
    }

    Context context

    @BeforeEach
    void initContext() {
        context = Context.newBuilder("js")
            .allowAllAccess(true)
            .build()
        GraalVMFacade.populateContext(context)
        context.enter()
    }

    @AfterEach
    void clearContext() {
        context?.leave()
        context?.close()
    }

    @Test
    void "create - can create with a non-null URI"() {
        def contextURI = new File(new File(moduleRepo.getBaseURI()),
                "foo/bar.js").toURI()
        def require = new RequireFunction(contextURI)
        assertEquals(contextURI, require.getContextURI())
    }

    @Test
    void "create - can create with a null URI"() {
        def require = new RequireFunction(null)
        assertNull(require.getContextURI())
    }

    @Test
    void "apply - can require an existing absolute module ID"() {
        def require = new RequireFunction(null)
        def value = require.apply("module2")
        assertNotNull(value)
        assertEquals("module 2", value?.getMember("message")?.asString())
    }

    @Test
    void "apply - can require an existing relative URI from a context"() {
        def contextURI = new File(new File(moduleRepo.getBaseURI()),
                "module2").toURI()
        def require = new RequireFunction(contextURI)
        def value = require.apply("./sub/module4")
        assertNotNull(value)
        assertEquals("module 4", value?.getMember("message")?.asString())
    }

    @Test
    void "apply - should throw if module is not available"() {
        shouldFail(RequireFunctionException.class) {
            def require = new RequireFunction(null)
            require.apply("no-such-module")
        }
    }

    @SuppressWarnings('GroovyUnusedAssignment')
    @Test
    void "apply - should throw if the module source is invalid"() {
        shouldFail(RequireFunctionException.class) {
            def require = new RequireFunction(null)
            def value = require.apply("moduleWithIllegalSyntax")
        }
    }

    @Test
    void "apply - can require a set of nested modules"() {
        def require = new RequireFunction(null)
        def value = require.apply("module3")
        assertNotNull(value)
        assertEquals("module 3",
            value?.getMember("messages")?.getMember("module3")?.asString())
        assertEquals("module 5",
            value?.getMember("messages")?.getMember("module5")?.asString())
        assertEquals("module 6",
            value?.getMember("messages")?.getMember("module6")?.asString())
    }
}
