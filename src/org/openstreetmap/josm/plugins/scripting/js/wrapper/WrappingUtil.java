package org.openstreetmap.josm.plugins.scripting.js.wrapper;

public class WrappingUtil {
	static public void assertApi(boolean cond, String message, Object...args) throws WrappingException{
		if (! cond) throw WrappingException.we(message, args);
	}
}
