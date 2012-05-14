package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingException.we;

import java.util.Map;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Custom wrapper for {@link OsmPrimitive}.
 * 
 */
public class OsmPrimitiveWrapper extends NativeJavaObject  {
	
	private OsmPrimitive obj() {
		return (OsmPrimitive)javaObject;
	}
	
	public OsmPrimitiveWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType, false);
	}
		
	@Override
	public boolean has(String name, Scriptable start) {
		if ("tags".equals(name)) return true;
		if ("user".equals(name)) return true;
		if ("ds".equals(name)) return true;
		if ("dataSet".equals(name)) return true;
		if ("version".equals(name)) return true;
		return super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if ("tags".equals(name)) return tagAccessor;
		if ("user".equals(name)) return getUser();
		if ("ds".equals(name)) return getDataSet();
		if ("dataSet".equals(name)) return getDataSet();
		if ("version".equals(name)) return getVersion();
		return super.get(name, start);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		if ("tags".equals(name)) {
			if (value == null || value == Undefined.instance) {
				obj().removeAll();
			} else if (value instanceof Map) {
				obj().removeAll();
				obj().setKeys((Map)value);
			} else {
				throw we("Can''t assign tags from object {0}", value);
			}
			return;
		}
		super.put(name, start, value);
	}
	
	private Object getUser() {
		if (obj().getUser() == null) return Undefined.instance;
		return obj().getUser();
	}
	
	private Object getDataSet() {
		if (obj().getDataSet() == null) return Undefined.instance;
		return obj().getDataSet();
	}
	
	private Object getVersion() {
		int version = obj().getVersion();
		return version == 0 ? Undefined.instance : version;
	}

	private TagAccessor tagAccessor = new TagAccessor();
	private class TagAccessor extends ScriptableObject {		
		public Object get(String name, Scriptable start) {
			if (!obj().hasKey(name))
				return Undefined.instance;
			return obj().get(name);
		}

		public void put(String name, Scriptable start, Object value) {
			if (name == null || name == Undefined.instance) return;
			name = name.trim();
			if (name.isEmpty()) return;
			if (value == null || value == Undefined.instance) {
				obj().remove(name);
				return;
			}
			if (! (value instanceof String)) value = value.toString();
			obj().put(name, (String)value);			
		}
		
		@Override
		public void delete(String name) {
			if (name == null || name == Undefined.instance) return;
			name = name.trim();
			if (name.isEmpty()) return;
			obj().remove(name);
		}

		@Override
		public String getClassName() {
			return getClass().getName();
		}
	};	
}
