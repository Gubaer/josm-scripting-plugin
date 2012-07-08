package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;

public class Tags extends ScriptableObject {
	private static final long serialVersionUID = 1L;
	static private final Logger logger = Logger.getLogger(Tags.class.getName());
	
	private Tagged primitive;
	public Tags(Tagged primitive) {
		this.primitive = primitive;
		this.setPrototype(TopLevel.getObjectPrototype(RhinoEngine.getInstance().getScope()));
	}

	@Override
	public Object get(String name, Scriptable start) {
		String value = primitive.get(name.trim());
		return value == null ? super.get(name, start) : value;
	}
	
	@Override
	public void put(String name, Scriptable start, Object value) {
		if (value == null || value == Undefined.instance) {
			if (primitive.get(name) != null) {
				primitive.remove(name.trim());
				if (primitive instanceof OsmPrimitive) {
					((OsmPrimitive)primitive).setModified(true);
				}
			}
		} else {
			String s = value.toString();
			boolean modified = primitive.get(name) == null || ! s.equals(primitive.get(name));
			primitive.put(name.trim(), s);
			if (primitive instanceof OsmPrimitive) {
				if (!((OsmPrimitive)primitive).isModified() && modified) {
					((OsmPrimitive)primitive).setModified(true);
				}				
			}			
		}
	}

	@Override
	public void delete(String name) {
		name = name.trim();
		if (primitive.get(name) != null) {
			primitive.remove(name.trim());
			// we could simply call primitive.setModified(true), but there's a lot
			// of event firing going on in JOSM under the hoods - better not to 
			// call this property getters unless realy necessary
			if (primitive instanceof OsmPrimitive) {
				if (! ((OsmPrimitive)primitive).isModified()) {
					((OsmPrimitive)primitive).setModified(true);
				}				
			}
		}		
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return primitive.get(name.trim()) != null;
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
