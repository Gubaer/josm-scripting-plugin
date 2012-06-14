package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * <p>Custom WrapFactory for the embedded JOSM scripting engine. Wraps java objects and classes
 * in the custom wrappers {@link NativeJavaClassWithJSMixin} and {@link NativeJavaObjectWithJSMixin}
 * respectively.</p>
 * 
 */
public class MixinWrapFactory extends WrapFactory{
	@SuppressWarnings("unused")
	static private final Logger logger = Logger.getLogger(MixinWrapFactory.class.getName());

	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType) {
		return new NativeJavaObjectWithJSMixin(scope, javaObject, staticType);
	}

	@Override
	public Scriptable wrapJavaClass(Context cx, Scriptable scope, Class javaClass) {
		return new NativeJavaClassWithJSMixin(scope, javaClass);
	}
}
