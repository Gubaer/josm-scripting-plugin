package org.openstreetmap.josm.plugins.scripting.js;

import org.mozilla.javascript.Scriptable;

public class JSMixinUtil {

    /**
     * <p>Replies true if the mixin property <code>property</code> is a
     * "static" property.</p>
     *
     * @param property  the property as defined in the mixin definition
     * @return true, if this is a static property
     */
    static public boolean isStaticProperty(Scriptable property) {
        final Object isStatic = ((Scriptable)property).get("static",
                (Scriptable)property);
        return isStatic != Scriptable.NOT_FOUND
                && isStatic instanceof Boolean && ((Boolean)isStatic);
    }

    /**
     * <p>Replies the static property with name <code>name</code> or
     * {@link Scriptable#NOT_FOUND}, if no such property exists.</p>
     *
     * @param mixin  the parsed JavaScript mixin
     * @param name  the property name
     * @return the property or {@link Scriptable#NOT_FOUND}
     */
    static public Object getStaticProperty(Scriptable mixin, String name) {
        final Object property = mixin.get(name, mixin);
        if (property != Scriptable.NOT_FOUND &&
                property instanceof Scriptable) {
            if (isStaticProperty((Scriptable)property)) {
                return (Scriptable)property;
            }
        }
        return Scriptable.NOT_FOUND;
    }

    /**
     * <p>Replies the instance property with name <code>name</code> or
     * {@link Scriptable#NOT_FOUND}, if no such property exists.</p>
     *
     * @param mixin  the parsed JavaScript mixin
     * @param name  the property name
     * @return the property or {@link Scriptable#NOT_FOUND}
     */
    static public Object getInstanceProperty(Scriptable mixin, String name) {
        final Object property = mixin.get(name, mixin);
        if (property != Scriptable.NOT_FOUND
                && property instanceof Scriptable) {
            if (!isStaticProperty((Scriptable)property)) {
                return (Scriptable)property;
            }
        }
        return Scriptable.NOT_FOUND;
    }
}
