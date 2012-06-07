/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org.openstreetmap.josm.data.osm.Way}.</p>
 * 
 * @module josm/mixin/WayMixin
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Node = org.openstreetmap.josm.data.osm.Node;
var Collection = java.util.Collection;
var HashSet = java.util.HashSet;

exports.forClass = org.openstreetmap.josm.data.osm.Way;

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@josmclass org.openstreetmap.josm.data.osm.Way}. It
 * provides additional properties and methods which you can invoke on an instance of
 * {@josmclass org.openstreetmap.josm.data.osm.Way}.</p>
 *  
 * <p>The native methods of {@josmclass org.openstreetmap.josm.data.osm.Way} are still 
 * available for scripting. Just prefix their name with <code>$</code> if they are hidden
 * by properties or functions defined in this mixin.</p>
 * 
 * <p>RelationMixin inherits the properties and methods of [OsmPrimitiveMixin]{@link OsmPrimitiveMixin}.</p>
 * 
 * <p>You can access nodes using indexed properties, i.e. <code>way[i]</code>, see example below.
 * 
 * @mixin WayMixin
 * @extends OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.Way
 * @memberof josm/mixin/WayMixin
 */
var mixin = {};

mixin.nodes = {
	get: function() {
		if (this.isIncomplete()) return undefined;
		var nodes = [];
		for(var it = this.getNodes().iterator(); it.hasNext();) nodes.push(it.next());
		return nodes; 
	},
	set: function(val) {
		util.assert(util.isSomething(val), "nodes must not be null or undefined");			
		if (val instanceof java.util.List) {
			this.setNodes(val);
		}
	}
};
	
mixin.length =  {
	get: function() {
		return this.getNodesCount();
	}
};
	
mixin.__getByIndex =  function(idx) {
	util.assert(idx >= 0 && idx < this.length, "Node index out of range, got {0}", idx);
	return this.getNode(idx);
};
	
mixin.contains =  function(node) {
	if (util.isNothing(node)) return false;
	util.assert(node instanceof Node, "Expected a Node, got {0}", node);
	return this.containsNode(node);
};
	
mixin.first = {
	get: function() {
		if (this.length == 0) return undefined;
		return this.getNode(0);
	}
};
	
mixin.last = {
	get: function() {
		if (this.length == 0) return undefined;
		return this.getNode(this.length-1);
	}
};
	
mixin.remove = function(){
	var candidates = new HashSet();
	function remember(o){
		if (util.isNothing(o)) return;
		if (o instanceof Node) {
			candidates.add(o);
		} else if (arg instanceof Collection){
			for(var it= arg.iterator(); it.hasNext();) remember(it.next());
		} else {
			// ignore 
		}
	}
	
	for (var i=0;i < arguments.length; i++) remember(arguments[i]);
	this.removeNodes(candidates);		
};

exports.mixin = util.mix(require("josm/mixin/OsmPrimitiveMixin").mixin,mixin);
