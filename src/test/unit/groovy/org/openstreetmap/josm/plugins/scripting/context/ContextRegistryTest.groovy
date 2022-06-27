package org.openstreetmap.josm.plugins.scripting.context

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ContextRegistryTest extends AbstractGraalVMBasedTest{

    @Test
    void "can lookup non-existing default context"() {
        final registry = new ContextRegistry()
        final context = registry.lookupDefaultContext(graalJSDescriptor)
        assertNull(context)
    }

    @Test
    void "can create default context, look it up, and remove it"() {
        final registry = new ContextRegistry()

        def listener = new PropertyChangeListener() {
            public PropertyChangeEvent event
            @Override
            void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                this.event = propertyChangeEvent
            }
        }
        registry.addPropertyChangeListener(listener)
        // create a default context
        final context01 = registry.getOrCreateDefaultContext(graalJSDescriptor)
        assertNotNull(context01)
        assertEquals(graalJSDescriptor, context01.getScriptEngine())
        def event = (HostedContextsChangeEvent)listener.event
        assertNull(event.getOldHostedContextState().getDefaultContext())
        assertNotNull(event.getNewHostedContextState().getDefaultContext())
        assertTrue(event.isDefaultContextChanged())
        assertEquals(context01, event.getNewHostedContextState().getDefaultContext())
        registry.removePropertyChangeListener(listener)

        // can lookup the new context
        final context02 = registry.lookupDefaultContext(graalJSDescriptor)
        assertNotNull(context02)
        assertEquals(context01.getId(), context02.getId())

        listener = new PropertyChangeListener() {
            public PropertyChangeEvent event
            @Override
            void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                this.event = propertyChangeEvent
            }
        }

        registry.addPropertyChangeListener(listener)
        // can remove the context
        registry.removeContext(context01)
        assertNotNull(listener.event)
        assertTrue(listener.event instanceof HostedContextsChangeEvent)
        event = (HostedContextsChangeEvent)listener.event
        assertNotNull(event.getOldHostedContextState().getDefaultContext())
        assertNull(event.getNewHostedContextState().getDefaultContext())
        assertTrue(event.isDefaultContextChanged())
        registry.removePropertyChangeListener(listener)

        // context isn't available anymore
        final context03 = registry.lookupDefaultContext(graalJSDescriptor)
        assertNull(context03)
    }

    @Test
    void "lookup non-existing user defined context should return null"() {
        final registry = new ContextRegistry()
        final context = registry.lookupUserDefinedContext("no-such-id", graalJSDescriptor)
        assertNull(context)
    }

    @Test
    void "can create user defined context, look it up, and remove it"() {
        final registry = new ContextRegistry()

        def listener = new PropertyChangeListener() {
            public PropertyChangeEvent event
            @Override
            void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                this.event = propertyChangeEvent
            }
        }
        registry.addPropertyChangeListener(listener)

        // create a default context
        final context01 = registry.createUserDefinedContext("display name", graalJSDescriptor)
        assertNotNull(context01)
        assertEquals(graalJSDescriptor, context01.getScriptEngine())
        assertEquals("display name", context01.getDisplayName())

        // should have fired a property change event with the correct data
        def event = (HostedContextsChangeEvent)listener.event
        assertEquals(0, event.getOldHostedContextState().getUserDefinedContexts().size())
        assertEquals(1, event.getNewHostedContextState().getUserDefinedContexts().size())
        def ctx = event.getNewHostedContextState().getUserDefinedContexts().get(0)
        assertEquals(context01, ctx)
        assertFalse(event.isDefaultContextChanged())
        assertTrue(event.isUserDefinedContextsChanged())
        registry.removePropertyChangeListener(listener)

        listener = new PropertyChangeListener() {
            public PropertyChangeEvent event
            @Override
            void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                this.event = propertyChangeEvent
            }
        }
        registry.addPropertyChangeListener(listener)
        // can remove the context
        registry.removeContext(context01)
        assertNotNull(listener.event)
        assertTrue(listener.event instanceof HostedContextsChangeEvent)
        event = (HostedContextsChangeEvent)listener.event
        assertEquals(1, event.getOldHostedContextState().getUserDefinedContexts().size())
        assertEquals(0, event.getNewHostedContextState().getUserDefinedContexts().size())
        assertFalse(event.isDefaultContextChanged())
        assertTrue(event.isUserDefinedContextsChanged())
        registry.removePropertyChangeListener(listener)

        // context isn't available anymore
        final context03 = registry.lookupUserDefinedContext(context01.getId(), graalJSDescriptor)
        assertNull(context03)
    }
}
