package org.openstreetmap.josm.plugins.scripting.rhino;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * <p>An extension for {@link NativeJavaClass}. In addition to the properties
 * derived from the static fields and methods of a java class it "mixes in"
 * a set of properties defined in an external javascript mixin module.</p>
 *
 */
public class NativeJavaClassWithJSMixin extends NativeJavaClass{
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(NativeJavaClassWithJSMixin.class.getName());

    private static final long serialVersionUID = 1L;

    public NativeJavaClassWithJSMixin(Scriptable scope, Class<?> cls) {
         super(scope, cls);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        if (name.startsWith("$")) {
            return super.has(name.substring(1), start);
        }
        final Scriptable mixin = JSMixinRegistry.get((Class<?>)javaObject);
        if (mixin != null) {
            Object property = JSMixinUtil.getStaticProperty(mixin, name);
            if (property != NOT_FOUND) return true;
        }
        return super.has(name, start);
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (name.startsWith("$")) {
            return super.get(name.substring(1), start);
        }
        final Scriptable mixin = JSMixinRegistry.get((Class<?>)javaObject);
        if (mixin == null) {
            return super.get(name, start);
        }
        final Object o = mixin.get(name, mixin);
        if (o == NOT_FOUND) {
            return super.get(name, start);
        } else if (o instanceof Function) {
            final Function f = (Function)o;
            Object isStatic = f.get("static", f);
            if (isStatic != NOT_FOUND && isStatic instanceof Boolean
                    && ((Boolean)isStatic)) {
                return f;
            }
        } else if (o instanceof Scriptable) {
            final Scriptable p = (Scriptable)o;
            final Object isStatic = p.get("static", p);
            if (isStatic != NOT_FOUND && isStatic instanceof Boolean
                    && ((Boolean)isStatic)) {
                final Object value = ((Scriptable)o).get("value",(Scriptable)o);
                if (value != NOT_FOUND) return value;
                final Object getter = ((Scriptable)o).get("get",(Scriptable)o);
                if (getter instanceof Function) {
                    return ((Function) getter).call(
                            Context.getCurrentContext(),
                            parent,
                            this,
                            new Object[]{}
                    );
                }
            }
        }
        return super.get(name, start);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        if (name.startsWith("$")) {
            super.put(name, start,value);
            return;
        }
        Scriptable mixin = JSMixinRegistry.get((Class<?>)javaObject);
        if (mixin == null) {
            super.put(name, start,value);
            return;
        }
        Object o = mixin.get(name, mixin);
        if (o instanceof Scriptable) {
            Object setter = ((Scriptable)o).get("set",(Scriptable)o);
            if (setter == NOT_FOUND) {
                throw ScriptRuntime.throwError(Context.getCurrentContext(), parent,
                    MessageFormat.format(
                            "Can''t set property ''{0}''. "
                          + "Javascript wrapper for class ''{1}'' doesn''t "
                          + "include a setter function.",
                          name, javaObject.getClass())
                );
            } else if (setter instanceof Function) {
                ((Function) setter).call(Context.getCurrentContext(),
                        parent,
                        this,
                        new Object[]{value}
                );
                return;
            } else {
                throw ScriptRuntime.throwError(
                    Context.getCurrentContext(),
                    parent,
                    MessageFormat.format(
                        "Can''t set property ''{0}''. "
                      + "Expected a setter function as value of property ''set'', "
                      + "got {1}",
                      name, setter)
                );
            }
        }
        super.put(name, start, value);
    }
}
