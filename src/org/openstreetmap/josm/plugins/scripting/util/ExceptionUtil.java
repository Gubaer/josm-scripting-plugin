package org.openstreetmap.josm.plugins.scripting.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {	
	static public  String stackTraceAsString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
}
