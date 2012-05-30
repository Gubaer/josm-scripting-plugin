package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;

public class Tags extends ScriptableObject {
	private static final long serialVersionUID = 1L;
	static private final Logger logger = Logger.getLogger(Tags.class.getName());
	
	private OsmPrimitive primitive;
	public Tags(OsmPrimitive primitive) {
		this.primitive = primitive;
		this.setPrototype(TopLevel.getObjectPrototype(RhinoEngine.getRootScope()));
	}

	@Override
	public Object get(String name, Scriptable start) {
		String value = primitive.get(name.trim());
		return value == null ? super.get(name, start) : value;
	}
	
	@Override
	public void put(String name, Scriptable start, Object value) {
		if (value == null || value == Undefined.instance) {
			primitive.remove(name.trim());
		} else {
			String s = value.toString();
			primitive.put(name.trim(), s);
		}
	}

	@Override
	public void delete(String name) {
		primitive.remove(name.trim());
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return primitive.hasKey(name.trim());
	}

	@Override
	public String getClassName() {
		return "Tags";
	}

	@Override
	public Object[] getAllIds() {		
		Collection<String> keys = primitive.keySet();
		if (keys.size() == 0){
			return new Object[]{};
		} else {
			Object[] ret = new Object[keys.size()];
			Iterator<String> it; int i;
			for (it = keys.iterator(), i=0; it.hasNext(); i++ ) {
				ret[i] = it.next();
			}
			return ret;
		}
	}
	
	@Override
	public Object[] getIds() {		
		return getAllIds();
	}

	@Override
	public Object getDefaultValue(Class<?> typeHint) {
		if (typeHint == null || typeHint == String.class || typeHint == Scriptable.class) {  
			StringBuffer sb = new StringBuffer();
			Collection<String> keys = primitive.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext();) {
				String key = it.next();
				if (sb.length() > 0) sb.append(",");
				sb.append(key).append("=").append(primitive.get(key));
			}
			return sb.toString();
		} else if (typeHint == Number.class) {
			return primitive.keySet().size();
		} else if (typeHint == Boolean.class) {
			return ! primitive.keySet().isEmpty();
		} 
		return null;
	}
}
