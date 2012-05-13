package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Node;

import sun.org.mozilla.javascript.Undefined;
import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.assertApi;


public class WayWrapper extends OsmPrimitiveWrapper{

	private Way way() {
		return (Way)javaObject;
	}
	
	public WayWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
	}
	
	public Object get(String name, Scriptable start) {
		if ("nodes".equals(name)) return getNodes();
		if ("length".equals(name)) return getLength();
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
		}
	}
	
	private Object getNodes() {
		return new NativeArray(way().getNodes().toArray());
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
}
