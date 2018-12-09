package org.openstreetmap.josm.plugins.scripting.js;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Icon;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;


public class JSAction extends JosmAction {
    private final static AtomicInteger counter = new AtomicInteger();

    static protected boolean isNothing(Object value) {
        return value == Scriptable.NOT_FOUND || value == null
                || value == Undefined.instance;
    }

    static protected String propertyAsString(Scriptable object,
            String property, String defaultValue) {
        final Object value = object.get(property, object);
        if (isNothing(value)) return defaultValue;
        return ScriptRuntime.toString(value);
    }

    static protected boolean propertyAsBoolean(Scriptable object,
            String property, boolean defaultValue) {
        final Object value = object.get(property, object);
        if (value == Scriptable.NOT_FOUND) return defaultValue;
        return ScriptRuntime.toBoolean(value);
    }

    static protected Function propertyAsFunction(Scriptable object,
            String property, Function defaultValue) {
        final Object value = object.get(property, object);
        if (isNothing(value)) return defaultValue;
        if (! (value instanceof Function)) return defaultValue;
        return (Function)value;
    }

    public JSAction(Scriptable properties) {
        final String name = propertyAsString(properties, "name", "JSAction"
                + counter.incrementAndGet());
        final String iconName = propertyAsString(properties, "iconName", null);
        final String tooltip = propertyAsString(properties, "tooltip", null);
        String toolbarId = propertyAsString(properties, "toolbarId", null);
        onExecute = propertyAsFunction(properties, "onExecute", null);
        onInitEnabled = propertyAsFunction(properties, "onInitEnabled", null);
        onUpdateEnabled = propertyAsFunction(properties, "onUpdateEnabled",
                null);
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        if (iconName != null) {
            Icon icon = ImageProvider.getIfAvailable(iconName);
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
        }
        if (toolbarId == null) {
            // automatically generate a toolbar id, if missing
            toolbarId = "toolbar" + counter.incrementAndGet();
        }
        // just remember the id. It will be used later, when the action is added
        // to the toolbar
        this.putValue("toolbarId", toolbarId);

        // FIXME should accept shortcut as parameter
        this.sc = Shortcut.registerShortcut(name, name, KeyEvent.VK_0,
                Shortcut.NONE);
        MainApplication.registerActionShortcut(this, sc);
        initEnabledState();
    }

    private Function onExecute;
    private Function onInitEnabled;
    private Function onUpdateEnabled;

    public Function getOnExecute() {
        return onExecute;
    }

    /**
     * Sets the JavaScript function to be invoked when the action is
     * triggered.
     *
     * @param onExecute the JavaScript function to be invoked
     */
    public void setOnExecute(Function onExecute) {
        this.onExecute = onExecute;
    }

    /**
     * Sets the JavaScript function which is invoked to initialize
     * the state (enabled/disabled) of this action
     *
     * @param onInitEnabled the JavaScript function
     */
    public void setOnInitEnabled(Function onInitEnabled) {
        this.onInitEnabled = onInitEnabled;
    }

    public Function getOnInitEnabled() {
        return onInitEnabled;
    }

    /**
     * Sets the JavaScript function which is invoked to update
     * the state (enabled/disabled) of this action
     *
     * @param onUpdateEnabled the JavaScript function
     */
    public void setOnUpdateEnabled(Function onUpdateEnabled) {
        this.onUpdateEnabled = onUpdateEnabled;
    }

    public Function getOnUpdateEnabled() {
        return onUpdateEnabled;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (onExecute != null) {
            final Scriptable scope = RhinoEngine.getInstance().getScope();
            onExecute.call(Context.getCurrentContext(),
                    scope,
                    (Scriptable)Context.javaToJS(this, scope),
                    new Object[]{Context.javaToJS(evt, scope)}
           );
        }
    }

    @Override
    protected void initEnabledState() {
        if (onInitEnabled != null) {
            final Scriptable scope = RhinoEngine.getInstance().getScope();
            onInitEnabled.call(Context.getCurrentContext(),
                    scope,
                    (Scriptable)Context.javaToJS(this, scope),
                    new Object[]{}
            );
        }
    }

    @Override
    protected void updateEnabledState() {
        if (onUpdateEnabled != null) {
            final Scriptable scope = RhinoEngine.getInstance().getScope();
            onUpdateEnabled.call(Context.getCurrentContext(),
                    scope,
                    (Scriptable)Context.javaToJS(this, scope),
                    new Object[]{}
            );
        }
    }

    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        if (onUpdateEnabled != null) {
            final Scriptable scope = RhinoEngine.getInstance().getScope();
            onUpdateEnabled.call(Context.getCurrentContext(),
                    scope,
                    (Scriptable)Context.javaToJS(this, scope),
                    new Object[]{Context.javaToJS(selection, scope)
            });
        }
    }
}
