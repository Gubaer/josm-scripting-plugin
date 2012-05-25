var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Node = org.openstreetmap.josm.data.osm.Node;
var Collection = java.util.Collection;
var HashSet = java.util.HashSet;

exports.forClass = org.openstreetmap.josm.data.osm.Way;

var mixin = {    
	nodes: {
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
	},
	
	length: {
		get: function() {
			return this.getNodesCount();
		}
	},
	
	__getByIndex: function(idx) {
		util.assert(idx >= 0 && idx < this.length, "Node index out of range, got {0}", idx);
		return this.getNode(idx);
	},
	
	contains: function(node) {
		if (util.isNothing(node)) return false;
		util.assert(node instanceof Node, "Expected a Node, got {0}", node);
		return this.containsNode(node);
	},
	
	first: {
		get: function() {
			if (this.length == 0) return undefined;
			return this.getNode(0);
		}
	},
	
	last: {
		get: function() {
			if (this.length == 0) return undefined;
			return this.getNode(this.length-1);
		}
	},
	
	remove: function(){
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
	}
};

exports.mixin = util.mix( 
		require("josm/mixin/OsmPrimitiveMixin").mixin,
		mixin
);
