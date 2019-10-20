package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Value;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class JSAction extends JosmAction {
    private final static AtomicInteger counter = new AtomicInteger();

    static private String propertyAsString(Value object,
            String property, String defaultValue) {
        final Value prop = object.getMember(property);
        if (prop == null) {
            return defaultValue;
        }
        return prop.asString();
    }

    static private Value propertyAsFunction(Value object,
            String property, Value defaultValue) {
        final Value value = object.getMember(property);
        if (value == null || !value.canExecute()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Creates the JS action with the properties in <code>properties</code>
     *
     * @param properties the properties
     */
    public JSAction(Value properties) {
        final String name = propertyAsString(properties, "name", "JSAction"
                + counter.incrementAndGet());
        final String iconName = propertyAsString(properties, "iconName", null);
        final String tooltip = propertyAsString(properties, "tooltip", null);
        final String toolbarId = propertyAsString(properties, "toolbarId",
                "toolbar" + counter.incrementAndGet());
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
        // just remember the id. It will be used later, when the action is added
        // to the toolbar
        this.putValue("toolbarId", toolbarId);

        // FIXME should accept shortcut as parameter
        this.sc = Shortcut.registerShortcut(name, name, KeyEvent.VK_0,
                Shortcut.NONE);
        MainApplication.registerActionShortcut(this, sc);
        initEnabledState();
    }

    private Value onExecute;
    private Value onInitEnabled;
    private Value onUpdateEnabled;

    public @Null Value getOnExecute() {
        return onExecute;
    }

    /**
     * Sets the JavaScript function to be invoked when the action is
     * triggered.
     *
     * @param onExecute the JavaScript function to be invoked
     * @throws IllegalArgumentException if <code>onExecute</code> isn't
     * executable
     */
    public void setOnExecute(@Null Value onExecute) {
        if (onExecute == null || onExecute.isNull()) {
            this.onExecute = null;
        } else {
            if (!onExecute.canExecute()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "executable value expected, got ''{0}''", onExecute
                ));
            }

            this.onExecute = onExecute;
        }
    }

    /**
     * Sets the JavaScript function which is invoked to initialize
     * the state (enabled/disabled) of this action
     *
     * @param onInitEnabled the JavaScript function
     * @throws IllegalArgumentException if <code>onInitEnabled</code> isn't
     * executable
     */
    public void setOnInitEnabled(@Null Value onInitEnabled) {
        if (onInitEnabled == null || onInitEnabled.isNull()) {
            this.onInitEnabled = null;
        } else {
            if (!onInitEnabled.canExecute()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "executable value expected, got ''{0}''", onInitEnabled
                ));
            }
            this.onInitEnabled = onInitEnabled;
        }
    }

    public @Null Value getOnInitEnabled() {
        return onInitEnabled;
    }

    /**
     * Sets the JavaScript function which is invoked to update
     * the state (enabled/disabled) of this action
     *
     * @param onUpdateEnabled the JavaScript function
     * @throws IllegalArgumentException if <code>onUpdateEnabled</code> isn't
     * executable
     */
    public void setOnUpdateEnabled(Value onUpdateEnabled) {
        if (onUpdateEnabled == null || onUpdateEnabled.isNull()) {
            this.onUpdateEnabled = null;
        } else {
            if (!onUpdateEnabled.canExecute()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "executable value expected, got ''{0}''", onUpdateEnabled
                ));
            }
            this.onUpdateEnabled = onUpdateEnabled;
        }
    }

    public @Null Value getOnUpdateEnabled() {
        return onUpdateEnabled;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (onExecute == null || ! onExecute.canExecute()) {
            return;
        }
        onExecute.execute(evt);
    }

    @Override
    protected void initEnabledState() {
        if (onInitEnabled == null || !onInitEnabled.canExecute()) {
            return;
        }
        onInitEnabled.execute();
    }

    @Override
    protected void updateEnabledState() {
        if (onUpdateEnabled == null || !onUpdateEnabled.canExecute()) {
            return;
        }
        onUpdateEnabled.execute();
    }

    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        if (onUpdateEnabled == null || !onUpdateEnabled.canExecute()) {
            return;
        }
        onUpdateEnabled.execute(selection);
    }

    /**
     * Replies the name of the JSAction
     * @return the name
     */
    public Value getName() {
        return Value.asValue(getValue(Action.NAME));
    }

    /**
     * Set the name of the JSAction
     *
     * @param name the name. Ignored, if nullish; otherwise,
     *   converted to a string with {@link #toString()}
     */
    public void setName(@Null Value name) {
        if (name == null || name.isNull()) {
            return;
        }
        putValue(Action.NAME, name.toString());
    }

    /**
     * Replies the tooltip
     * @return the tooltip
     */
    public Value getTooltip() {
        return Value.asValue(getValue(Action.SHORT_DESCRIPTION));
    }

    /**
     * Set the tooltip of the JSAction
     *
     * @param tooltip the tooltip. Ignored, if nullish; otherwise,
     *   converted to a string with {@link #toString()}
     */
    public void setTooltip(@Null Value tooltip) {
        if (tooltip == null || tooltip.isNull()) {
            return;
        }
        setTooltip(tooltip.toString());
    }
}
