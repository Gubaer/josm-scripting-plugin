package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * <p>Represents a native java object with an optional mixin implemented in JavaScript.</p>
 * 
 * <p>Mixins are implemented as CommonJS modules. A mixin has to export two properties:</p>
 * <ul>
 *   <li><strong>forClass</strong> -  the value must be java class for which the mixin is defined</li>
 *   <li><strong>mixin</strong> - an object with object descriptors. The value for each property is 
 *   either a function (this defines a mixed in method) or a property descriptor with eithe a value or
 *   a getter and an (optional) setter function (this defines a mixed in property).</li> 
 * </ul>
 * <strong>Example</strong>
 * <pre>
 * exports.forClass = org.openstreetmap.josm.data.osm.Node;

 * // a mixin defining two new properties and an additional method
 * //
 * exports.mixin = {
 *     id: {
 *       get: function() {return this.getUniqueId();}
 *     },
 *     lat: {
 *       get: function() {
 *          return this.getCoor().lat();
 *       },
 *       set: function(lat) {
 *         var coor = this.getCoor(); 
 *         coor=new LatLon(lat, coor.lon()); 
 *         this.setCoor(coor);
 *       } 
 *     }
 *     
 *     echo: function() {
 *         java.lang.System.out.println("node: " + this.toString());
 *     }
 * };
 * </pre>
 * 
 *
 */
public class NativeJavaObjectWithJSMixin extends NativeJavaObject {
	private static final long serialVersionUID = 1L;
	static private final Logger logger = Logger.getLogger(NativeJavaObjectWithJSMixin.class.getName());
	
	static public Map<Class<?>, Scriptable> MIXINS = new HashMap<Class<?>, Scriptable>();
	static public void registerMixin(Class<?> clazz, Scriptable mixin) {
		MIXINS.put(clazz, mixin);
	}

	/**
	 * <p>Register a mixin for a java class, implemented in a CommonJS module with name <code>moduleName</code></p>.
	 * 
	 * @param parentScope the parent scope to be used when loading the module 
	 * @param moduleName the module name 
	 * @throws WrappingException thrown, if an exception occurs
	 */
	static public void loadJSPrototype(Scriptable parentScope, String moduleName) throws WrappingException{
		Context ctx = Context.getCurrentContext();
		Scriptable scope = new NativeObject();
		scope.setParentScope(parentScope);
		String script = MessageFormat.format("require(''{0}'').forClass;", moduleName);
		Object o = null;
		try {
			o = ctx.evaluateString(scope, script, "inline", 0, null);
		} catch(RhinoException e){
			throw new WrappingException(
					MessageFormat.format("Failed to get property ''{0}'' in module ''{1}''.", "forClass",moduleName),
					e
			);
		} 
		if (o instanceof Wrapper) {
			o = ((Wrapper)o).unwrap();
		}
		if (! (o instanceof Class<?>)) {
			throw WrappingException.we("Unexpected value for property ''{0}'' in module ''{1}''. Expected a java class object, got {2}", "forClass", moduleName, o);
		}
		Class<?> clazz = (Class<?>)o;
		script = MessageFormat.format("require(''{0}'').mixin", moduleName);
		try {
			o = ctx.evaluateString(scope, script, "inline", 0, null);
		} catch(RhinoException e){
			throw new WrappingException(
					MessageFormat.format("Failed to get property ''{0}'' in module ''{1}''.", "mixin",moduleName),
					e
			);
		} 
		if (! (o instanceof Scriptable)) {
			throw WrappingException.we("Unexpected value for mixin ''mixin'' in module ''{0}''. Expected a Scriptable, got {1}", "forClass", moduleName, o);
		}
		Scriptable mixin = (Scriptable)o;
		registerMixin(clazz, mixin);
	}
	
	public NativeJavaObjectWithJSMixin(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType, false);		
	}
		
	@Override
	public boolean has(String name, Scriptable start) {
		Scriptable mixin = MIXINS.get(javaObject.getClass());
		if (mixin != null) {
			boolean ret = mixin.has(name, mixin);
			if (ret) return ret;
		}
		return super.has(name, start);
	}

	@Override
	public Object get(String name, Scriptable start) {
		Scriptable mixin = MIXINS.get(javaObject.getClass());
		if (mixin == null) {
			return super.get(name, start);
		}
		Object o = mixin.get(name, mixin);
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
		Scriptable mixin = MIXINS.get(javaObject.getClass());
		if (mixin == null) {
			super.put(name, start,value);
			return;
		}
		Object o = mixin.get(name, mixin);
		if (o instanceof Scriptable) {
			Object setter = ((Scriptable)o).get("set",(Scriptable)o);
			if (setter == NOT_FOUND) {
				WrappingUtil.assertApi(false, "Can''t set property ''{0}''. Javascript wrapper for class ''{1}'' doesn''t include a setter function.", name, javaObject.getClass());				
			} else if (setter instanceof Function) {
				((Function) setter).call(Context.getCurrentContext(), parent, this, new Object[]{value});
				return;
			} else {
				WrappingUtil.assertApi(false, "Can''t set property ''{0}''. Expected a setter function as value of property ''set'', got {1}",name, setter);
			}
		} 
		super.put(name, start, value);
	}

	@Override
	public Object get(int index, Scriptable start) {
		Scriptable mixin = MIXINS.get(javaObject.getClass());
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
		Scriptable mixin = MIXINS.get(javaObject.getClass());
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
