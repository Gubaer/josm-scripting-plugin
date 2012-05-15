package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import org.mozilla.javascript.WrappedException;

public class WrappingUtil {
	static public void assertApi(boolean cond, String message, Object...args) {
		if (! cond) throw new WrappedException( 
		 	WrappingException.we(message, args)
		);
	}
}
