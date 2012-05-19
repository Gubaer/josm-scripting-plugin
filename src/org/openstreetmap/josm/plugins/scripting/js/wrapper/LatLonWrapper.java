package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.data.coor.LatLon;

public class LatLonWrapper extends NativeJavaObject  {
	private static final long serialVersionUID = 1L;
	
	private LatLon pos() {
		return (LatLon)javaObject;
	}

	public LatLonWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType, false);
	}

	@Override
	public boolean has(String name, Scriptable start) {
		if ("lat".equals(name)) return true;
		if ("lon".equals(name)) return true; 
		return super.has(name, start);
	}

	@Override
	public Object get(String name, Scriptable start) {
		if ("lat".equals(name)) return getLat();
		if ("lon".equals(name)) return getLon();
		return super.get(name, start);
	}
	
	private Object getLat() {
		return pos().lat();
	}
	
	private Object getLon() {
		return pos().lon();
	}
}
