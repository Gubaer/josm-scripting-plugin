package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleCache

class CommonJSModuleCacheTest extends GroovyTestCase {

    @Test
    void "singleton should be available"() {
        def instance = CommonJSModuleCache.getInstance()
        assertNotNull(instance)
    }

    @Test
    void "can remember a module which is then available"() {
        def moduleUri = new File("/repo/base/dir/my-module.js").toURI()
        def module = Value.asValue("my message")
        def context = null

        try {
            context = Context.create("js")
            def cache = CommonJSModuleCache.getInstance()
            cache.remember(moduleUri, module, context)
            def lookedUpModule = cache.lookup(moduleUri, context)
            assertTrue(lookedUpModule.isPresent())
            assertEquals(module.asString(), lookedUpModule.get().asString())
        } finally {
            context?.close()
        }
    }

    @Test
    void "module which is not registered should not be available"() {
        def moduleUri = new File("/repo/base/dir/module3").toURI()
        def context = null

        try {
            context = Context.create("js")
            def cache = CommonJSModuleCache.getInstance()
            def lookedUpModule = cache.lookup(moduleUri, context)
            assertFalse(lookedUpModule.isPresent())
        } finally {
            context?.close()
        }
    }

    @Test
    void "remembered module should not be available in other context"() {
        def moduleUri = new File("/repo/base/dir/module2.js").toURI()
        def module = Value.asValue("module2 message")
        def context1 = null
        def context2 = null

        try {
            context1 = Context.create("js")
            context2 = Context.create("js")
            def cache = CommonJSModuleCache.getInstance()
            cache.remember(moduleUri, module, context1)
            def lookedUpModule = cache.lookup(moduleUri, context2)
            assertFalse(lookedUpModule.isPresent())
        } finally {
            context1?.close()
            context2?.close()
        }
    }

    @Test
    void "a module which is removed from the cache can't be looked up"() {
        def moduleUri = new File("/repo/base/dir/my-module.js").toURI()
        def module = Value.asValue("my message")
        def context = null

        try {
            context = Context.create("js")
            def cache = CommonJSModuleCache.getInstance()
            cache.remember(moduleUri, module, context)
            def lookedUpModule = cache.lookup(moduleUri, context)
            assertTrue(lookedUpModule.isPresent())
            assertEquals(module.asString(), lookedUpModule.get().asString())
            cache.clear(moduleUri, context)
            lookedUpModule = cache.lookup(moduleUri, context)
            assertFalse(lookedUpModule.isPresent())
        } finally {
            context?.close()
        }
    }

    @Test
    void "a module which is removed from the cache can't be looked up (2)"() {
        def moduleUri = new File("/repo/base/dir/my-module.js").toURI()
        def module = Value.asValue("my message")
        def context = null

        try {
            context = Context.create("js")
            def cache = CommonJSModuleCache.getInstance()
            cache.remember(moduleUri, module, context)
            def lookedUpModule = cache.lookup(moduleUri, context)
            assertTrue(lookedUpModule.isPresent())
            assertEquals(module.asString(), lookedUpModule.get().asString())
            cache.clear(context)
            lookedUpModule = cache.lookup(moduleUri, context)
            assertFalse(lookedUpModule.isPresent())
        } finally {
            context?.close()
        }
    }
}
