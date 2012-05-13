package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.assertApi;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

/**
 * Custom wrapper for {@link Node}.
 * 
 * <strong>Additional properties</strong>
 * <ul>
 *   <li><code>lat</code> (read/write)  - the latitude. .</li>
 *   <li><code>lon</code> (read/write) - the longitude. Read/write access.</li>
 *   <li><code>east</code> (read only) - the projected east coordinate, or <code>undefined</code>, if unknown. </li>
 *   <li><code>north</code> (read only) - the projected north coordinate, or <code>undefined</code>, if unknown </li>
 * </ul>
 *
 */
public class NodeWrapper extends OsmPrimitiveWrapper {

	private Node node() {
		return (Node)javaObject;
	}
	
	public NodeWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
	}
	
	@Override
	public boolean has(String name, Scriptable start) {
		if ("lat".equals(name)) return true;
		if ("lon".equals(name)) return true;
		if ("east".equals(name)) return true;
		if ("north".equals(name)) return true;
		return super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if ("lat".equals(name)) return getLat();
		if ("lon".equals(name)) return getLon();
		if ("east".equals(name)) return getEast();
		if ("north".equals(name)) return getNorth();
		return super.get(name, start);
	}
	
	@Override
	public void put(String name, Scriptable start, Object value) {
		if ("lat".equals(name)) {
			setLat(value);
		} else if ("lon".equals(name)) {
			setLon(value);
		} else {
			super.put(name, start, value);			
		}
	}
		
	private Object getLat() {
		Node n = node();
		if (n.isIncomplete() || n.getCoor() == null) return Undefined.instance;
		return n.getCoor().lat();
	}
	
	private Object getLon() {
		Node n = node();
		if (n.isIncomplete() || n.getCoor() == null) return Undefined.instance;
		return n.getCoor().lon();		
	}
	
	private Object getEast() {
		Node n = node();
		EastNorth en = n.getEastNorth();
		if (en == null) return Undefined.instance;
		return en.east();
	}
	
	private Object getNorth() {
		Node n = node();
		EastNorth en = n.getEastNorth();
		if (en == null) return Undefined.instance;
		return en.north();
	}

	
	private void setLat(Object val) {
		assertApi(val != null && val != Undefined.instance, "Can''t assign null or undefined to lat");
		assertApi(val instanceof Number, "Expected a number, got {0}", val);
		double lat = ((Number)val).doubleValue();
		assertApi(LatLon.isValidLat(lat), "Can''t assign an invalid latitude, got {0}", lat);
		assertApi(! node().isIncomplete(), "Can''t set latitude on a proxy node");
		LatLon coor = node().getCoor();
		coor = new LatLon(lat, coor.lon());
		node().setCoor(coor);
	}
	
	private void setLon(Object val) {
		assertApi(val != null && val != Undefined.instance, "Can''t assign null or undefined to lon");
		assertApi(val instanceof Number, "Expected a number, got {0}", val);
		double lon = ((Number)val).doubleValue();
		assertApi(LatLon.isValidLon(lon), "Can''t assign an invalid latitude, got {0}", lon);
		assertApi(! node().isIncomplete(), "Can''t set latitude on a proxy node");
		LatLon coor = node().getCoor();
		if (coor == null) coor = new LatLon(0,0);
		coor = new LatLon(coor.lat(), lon);
		node().setCoor(coor);
	}	
}



