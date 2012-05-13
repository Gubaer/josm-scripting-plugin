package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * <p>Custom WrapFactory for the embedded JOSM scripting engine.</p>
 * 
 * <p>Wraps a subset of the native JOSM objects with custom wrappers, mostly to extend them with a
 * JavaScript-like API.</p>
 * 
 */
public class JOSMWrapFactory extends WrapFactory{
	
	/**
	 * the map of available custom wrappers
	 */
	private static Map<Class<?>, Class<? extends Scriptable>> WRAPPERS = new HashMap<Class<?>, Class<? extends Scriptable>>();
	static {
		WRAPPERS.put(Node.class, NodeWrapper.class);
		WRAPPERS.put(Way.class, WayWrapper.class);
	}
	
	@Override
	public Object wrap(Context cx, Scriptable scope, Object obj,
			Class<?> staticType) {

		// copy/paste from Rhinos default implementation
		//
		if (obj == null || obj == Undefined.instance
				|| obj instanceof Scriptable) {
			return obj;
		}
		if (staticType != null && staticType.isPrimitive()) {
			if (staticType == Void.TYPE)
				return Undefined.instance;
			if (staticType == Character.TYPE)
				return Integer.valueOf(((Character) obj).charValue());
			return obj;
		}
		if (!isJavaPrimitiveWrap()) {
			if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
				return obj;
			} else if (obj instanceof Character) {
				return String.valueOf(((Character) obj).charValue());
			}
		}
		Class<?> cls = obj.getClass();
		if (cls.isArray()) {
			return NativeJavaArray.wrap(scope, obj);
		}
		
		// wrap native JOSM objects
		//
		Class<? extends Scriptable> wrapperClazz = WRAPPERS.get(obj.getClass());
		if (wrapperClazz != null) {
			try {
				Constructor<?> constructor = wrapperClazz.getConstructor(
						Scriptable.class, Object.class, Class.class);
				return constructor.newInstance(scope, obj, null);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return wrapAsJavaObject(cx, scope, obj, staticType);
	}

	/**
	 * Wrap an object newly created by a constructor call.
	 * 
	 * @param cx
	 *            the current Context for this thread
	 * @param scope
	 *            the scope of the executing script
	 * @param obj
	 *            the object to be wrapped
	 * @return the wrapped value.
	 */
	public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
		
		// copy/paste from Rhinos default implementation 
		//
		if (obj instanceof Scriptable) {
            return (Scriptable)obj;
        }
        Class<?> cls = obj.getClass();
        if (cls.isArray()) {
            return NativeJavaArray.wrap(scope, obj);
        }
        
		// custom wrapping for selected native JOSM objects  
		//
		Class<? extends Scriptable> wrapperClazz = WRAPPERS.get(obj.getClass());
		if (wrapperClazz != null) {			
			try {
				Constructor<? extends Scriptable> constructor = wrapperClazz.getConstructor(Scriptable.class, Object.class, Class.class);
				return constructor.newInstance(scope, obj, null);
			} catch(NoSuchMethodException e) {
				e.printStackTrace();
			} catch(InstantiationException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch(InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return wrapAsJavaObject(cx, scope, obj, null);
	}
}
