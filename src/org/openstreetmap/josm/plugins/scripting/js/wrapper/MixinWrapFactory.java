package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * <p>Custom WrapFactory for the embedded JOSM scripting engine.</p>
 * 
 */
public class MixinWrapFactory extends WrapFactory{
	@SuppressWarnings("unused")
	static private final Logger logger = Logger.getLogger(MixinWrapFactory.class.getName());

	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
		return new NativeJavaObjectWithJSMixin(scope, javaObject, staticType);
	}
}
