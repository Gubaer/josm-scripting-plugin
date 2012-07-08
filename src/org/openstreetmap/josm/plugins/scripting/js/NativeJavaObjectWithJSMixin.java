package org.openstreetmap.josm.plugins.scripting.js;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * <p>An extension for {@link NativeJavaObject}. In addition to the properties derived from
 * the fields and methods of a java class it "mixes in" a set of properties defined
 * in an external javascript mixin module.</p>
 * 
 */public class NativeJavaObjectWithJSMixin extends NativeJavaObject {
	private static final long serialVersionUID = 1L;
	static private final Logger logger = Logger.getLogger(NativeJavaObjectWithJSMixin.class.getName());

	public NativeJavaObjectWithJSMixin(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType, false);		
	}
		
	@Override
	public boolean has(String name, Scriptable start) {
		if (name.startsWith("$")) {			
			return super.has(name.substring(1), start);
		}
		Scriptable mixin = JSMixinRegistry.get(javaObject.getClass());
		if (mixin != null) {
			boolean ret = mixin.has(name, mixin);
			if (ret) return ret;
		}
		return super.has(name, start);
	}

	@Override
	public Object get(String name, Scriptable start) {
		if (name.startsWith("$")) {			
			return super.get(name.substring(1), start);
		}
		Scriptable mixin = JSMixinRegistry.get(javaObject.getClass());
		if (mixin == null) {
			return super.get(name, start);
		}
		Object o = JSMixinUtil.getInstanceProperty(mixin, name);
		if (o == NOT_FOUND) {
			return super.get(name, start);
		} else if (o instanceof Function) {
			return o;
		} else if (o instanceof Scriptable) {
			Object value = ((Scriptable)o).get("value",(Scriptable)o);
			if (value != NOT_FOUND) return value;
			Object getter = ((Scriptable)o).get("get",(Scriptable)o);
			if (getter instanceof Function) {
				Object ret = ((Function) getter).call(Context.getCurrentContext(), parent, this, new Object[]{});
				return ret;
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
		Scriptable mixin = JSMixinRegistry.get(javaObject.getClass());
		if (mixin == null) {
			super.put(name, start,value);
			return;
		}
		Object o = JSMixinUtil.getInstanceProperty(mixin, name);
		if (o instanceof Scriptable) {
			Object setter = ((Scriptable)o).get("set",(Scriptable)o);
			if (setter == NOT_FOUND) {
				ScriptRuntime.throwError(Context.getCurrentContext(), parent, 
					MessageFormat.format("Can''t set property ''{0}''. Javascript wrapper for class ''{1}'' doesn''t include a setter function.", name, javaObject.getClass())
				);
			} else if (setter instanceof Function) {
				((Function) setter).call(Context.getCurrentContext(), parent, this, new Object[]{value});
				return;
			} else {
				ScriptRuntime.throwError(Context.getCurrentContext(), parent, 
					MessageFormat.format("Can''t set property ''{0}''. Expected a setter function as value of property ''set'', got {1}",name, setter)
				);
			}
		} 
		super.put(name, start, value);
	}

	@Override
	public Object get(int index, Scriptable start) {
		Scriptable mixin = JSMixinRegistry.get(javaObject.getClass());
		if (mixin == null) {
			return super.get(index, start);
		}	
		Object f = mixin.get("__getByIndex", mixin);
		if (f instanceof Function) {
			return ((Function) f).call(Context.getCurrentContext(), parent, this, new Object[]{index});
		}
		return super.get(index, start);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		Scriptable mixin = JSMixinRegistry.get(javaObject.getClass());
		if (mixin == null) {
			super.put(index, start, value);
			return;
		}	
		Object f = mixin.get("__putByIndex", mixin);
		if (f instanceof Function) {
			((Function) f).call(Context.getCurrentContext(), parent, this, new Object[]{index, value});
		} else {
			super.put(index, start, value);
		}
	}
}
