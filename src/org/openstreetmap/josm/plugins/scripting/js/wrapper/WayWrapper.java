package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.assertApi;
import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.toNativeArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

import sun.org.mozilla.javascript.Undefined;

public class WayWrapper extends OsmPrimitiveWrapper{
	private static final long serialVersionUID = 1L;

	private Way way() {
		return (Way)javaObject;
	}
	
	public WayWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
	}
	
	@Override
	public boolean has(String name, Scriptable start) {
		if ("nodes".equals(name)) return true;
		if ("length".equals(name)) return true;
		if ("contains".equals(name)) return true;
		if ("add".equals(name)) return true;
		return super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if ("nodes".equals(name)) return getNodes();
		if ("length".equals(name)) return getLength();
		if ("contains".equals(name)) return js_contains;
		if ("add".equals(name)) return js_add;
		return super.get(name, start);
	}
	
	@Override
	public Object get(int index, Scriptable start) {
		if (index >= way().getNodesCount()) return Undefined.instance;
		return way().getNode(index);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		if ("nodes".equals(name)) {
			setNodes(value);
		} else {
			super.put(name, start, value);
		}
	}
	
	private Object getNodes() {
		return toNativeArray(way().getNodes(), parent);
	}
	
	private void setNodes(Object value) {
		assertApi(value != null && value != Undefined.instance, "Can''t set null or undefined as list of nodes");
		List<Node> nodes = new ArrayList<Node>();
		Node last = null;
		if (value instanceof List<?>) {
			List<?> l = (List<?>)value;
			for (int i = 0; i < l.size();i++) {
				Object item = l.get(i);
				if (item == null || item == Undefined.instance) continue;
				item = WrappingUtil.unwrap(item);
				assertApi(item instanceof Node, "Expected instances of Node only, got {0} at index {1}", item, i);
				Node current = (Node)item;
				if (last != null && last.getUniqueId() == current.getUniqueId()) continue;
				nodes.add(current);
				last = current; 
			}
		} 
		assertApi(nodes.size() >= 2, "Expected at least 2 distinct nodes, got {0}", nodes.size());
		way().setNodes(nodes);
	}
	
	private Object getLength() {
		return way().getNodesCount();
	}
	
	static private Function js_contains = new BaseFunction() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof WayWrapper, "Expected a thisObj of type Way, got type ''{0}''", thisObj.getClass());
			Way w = (Way)WrappingUtil.unwrap(thisObj);
			assertApi(args.length == 1, "Expected exactly one argument, got {0}", args.length);
			assertApi(args[0] != null && args[0] != Undefined.instance, "Argument 0: must not be null or undefined");
			Object o = WrappingUtil.unwrap(args[0]);
			assertApi(o instanceof Node, "Argument 0: Expected a Node, got {0}", o);
			Node n = (Node)o;
			return w.containsNode(n);
		}		
	};
		
	static private Function js_add = new BaseFunction() {
		
		protected void add(Way w, Object o) {
			if ( o == null || o == Undefined.instance) return;
			o = WrappingUtil.unwrap(o);
			if (o instanceof Node) {
				w.addNode((Node)o);
			} else if (o instanceof List<?>) {
				for (Object oo: (List<?>)o) {
					add(w, oo);
				}
			} else {
				assertApi(false, "Can''t add an object of type ''{0}'' to a way", o.getClass());
			}
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof WayWrapper, "Expected a thisObj of type Way, got type ''{0}''", thisObj.getClass());
			Way w = (Way)WrappingUtil.unwrap(thisObj);
			int idx = w.getNodesCount();
			Object[] toAdd;
			if (args.length > 0 && args[0] instanceof Number) {
				int n = ((Number)args[0]).intValue();
				assertApi(n >= 0, "Argument 0: Expected an insert index >= 0, got {0}", args[0]);
				idx = Math.min(idx, n);
				toAdd = Arrays.copyOfRange(args, 1, args.length);
			} else {
				toAdd = args;
			}
			for (int i=0; i< toAdd.length; i++) {
				add(w, args[i]);
			}
			return Undefined.instance;
		}		
	};
}
