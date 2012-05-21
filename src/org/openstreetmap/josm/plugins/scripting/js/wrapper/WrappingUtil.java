package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.util.Collection;
import java.util.Iterator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;

import sun.org.mozilla.javascript.Undefined;

public class WrappingUtil {
	static public void assertApi(boolean cond, String message, Object...args) {
		if (! cond) throw new WrappedException( 
		 	WrappingException.we(message, args)
		);
	}
	
	static public Object unwrap(Object o) {
		if (o instanceof Wrapper) {
			o = ((Wrapper)o).unwrap();
		}
		return o;
	}
	
	static public NativeArray toNativeArray(Collection<?> objects, Scriptable scope) {
		Object[] wrapped = new Object[objects.size()];
		int i;
		Iterator<?> it;
		for (it = objects.iterator(), i=0; it.hasNext();) {
			wrapped[i++] = Context.javaToJS(it.next(), scope);
		}
		return new NativeArray(wrapped);
	} 
	
	static public boolean isSomething(Object obj) {
		return obj != null && obj != Undefined.instance;
	}
	
	static public boolean isNothing(Object obj) {
		return obj == null || obj == Undefined.instance;
	}
	
	static public void assertSomething(Object obj, String message, Object ...args) {
		assertApi(isSomething(obj), message, args);
	}
}
