package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;

/**
 * <p>Custom WrapFactory for the embedded JOSM scripting engine.</p>
 * 
 */
public class MixinWrapFactory extends WrapFactory{
	@SuppressWarnings("unused")
	static private final Logger logger = Logger.getLogger(MixinWrapFactory.class.getName());
	
	@Override
	public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType) {

		// copy/paste from Rhinos default implementation ...
		//
		if (obj == null || obj == Undefined.instance|| obj instanceof Scriptable) {
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
		
		// ... but use a custom wrapper class
		return new NativeJavaObjectWithJSMixin(scope, obj, staticType);
	}
	
	public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
		// copy/paste from Rhinos default implementation ...
		//
		if (obj instanceof Scriptable) {
            return (Scriptable)obj;
        }
        Class<?> cls = obj.getClass();
        if (cls.isArray()) {
            return NativeJavaArray.wrap(scope, obj);
        }
        
		// ... but use a custom wrapper class
        //
        return new NativeJavaObjectWithJSMixin(scope, obj, null);
	}
}
