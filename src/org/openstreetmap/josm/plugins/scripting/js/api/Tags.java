package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;

public class Tags extends ScriptableObject {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(Tags.class.getName());

    final private Tagged primitive;
    public Tags(Tagged primitive) {
        this.primitive = primitive;
        this.setPrototype(TopLevel.getObjectPrototype(
                RhinoEngine.getInstance().getScope()));
    }

    @Override
    public Object get(String name, Scriptable start) {
        final String value = primitive.get(name.trim());
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
            boolean modified = primitive.get(name) == null
                    || ! s.equals(primitive.get(name));
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
            // we could simply call primitive.setModified(true), but there's
            // a lot of event firing going on in JOSM under the hoods -
            // better not to call this property getters unless really necessary
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
        return primitive.keySet().stream()
             .toArray((size) -> new Object[size]);
    }

    @Override
    public Object[] getIds() {
        return getAllIds();
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (typeHint == null || typeHint == String.class
                || typeHint == Scriptable.class) {
            return primitive.keySet().stream()
                .map(key -> key + "=" + primitive.get(key))
                .collect(Collectors.joining(","));
        } else if (typeHint == Number.class) {
            return primitive.keySet().size();
        } else if (typeHint == Boolean.class) {
            return ! primitive.keySet().isEmpty();
        }
        return null;
    }
}
