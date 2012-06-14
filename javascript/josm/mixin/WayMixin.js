(function() {
/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.data.osm.Way}.</p>
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
 * <p>This mixin 
 * provides additional properties and methods which you can invoke on an instance of
 * {@class org.openstreetmap.josm.data.osm.Way}.</p>
 *  
 * <p>You can access nodes using indexed properties, i.e. 
 * <pre>way[i]</pre>, 
 * see example below.
 * 
 * @mixin WayMixin
 * @extends OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.Way
 * @memberof josm/mixin/WayMixin
 */
var mixin = {};

function nodeListEquals(l1, l2) {
	if (l1.length != l2.length) return false;
	for(var i=0; i<l1.length; i++) {
		if (l1[i].id != l2[i].id) return false;
	}
	return true;
};

/**
 * <p>Set or get the nodes of a way.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the nodes as array.</dd>
 *   <dt>set</dt>
 *   <dd>Set the an array or {@class java.util.List} of {@class org.openstreetmap.josm.data.osm.Node}s.</dd>
 * </dl>
 * 
 * @example
 * var way = ...       // create the way;
 * var n1,n2,n3 = ...; // create the nodes 
 * way.nodes = [n1,n2,n3];
 * way.nodes;  // -> [n1,n2,n3]
 * 
 * @field
 * @instance
 * @name nodes
 * @type array
 * @memberof WayMixin
 * @summary  Set or get the nodes of a way.
 */
mixin.nodes = {
	get: function() {
		if (this.isIncomplete) return undefined;
		var nodes = [];
		for(var it = this.getNodes().iterator(); it.hasNext();) nodes.push(it.next());
		return nodes; 
	},
	set: function(val) {
		util.assert(util.isSomething(val), "nodes must not be null or undefined");
		var oldnodes = this.nodes;
		if (util.isArray(val) || val instanceof java.util.List) {
			this.setNodes(val);
		} else {
			util.assert(false, "Expected array or list of nodes, got {0}", val);
		}
		if (!nodeListEquals(oldnodes, this.nodes) && !this.modified) this.modified = true;
	}
};

/**
 * <p>Replies the number of nodes.</p>
 * 
 * @field
 * @instance
 * @name length
 * @type number
 * @memberof WayMixin
 * @readOnly
 * @summary Replies the number of nodes.
 */
mixin.length =  {
	get: function() {
		return this.getNodesCount();
	}
};
	
mixin.__getByIndex =  function(idx) {
	util.assert(idx >= 0 && idx < this.length, "Node index out of range, got {0}", idx);
	return this.getNode(idx);
};
	
/**
 * <p>Checks if a node is part of a way.</p>
 * 
 * @param {org.openstreetmap.josm.data.osm.Node} node  the node to check
 * @method
 * @instance
 * @name contains
 * @type boolean
 * @memberof WayMixin
 * @summary Checks if a node is part of a way.
 */
mixin.contains =  function(node) {
	if (util.isNothing(node)) return false;
	util.assert(node instanceof Node, "Expected a Node, got {0}", node);
	return this.containsNode(node);
};

/**
 * <p>The first node or undefined, if this way doesn't have nodes 
 * (i.e. because it is a proxy way).</p>
 * 
 * @type org.openstreetmap.josm.data.osm.Node
 * @field
 * @instance
 * @name first
 * @memberof WayMixin
 * @summary The first node
 */
mixin.first = {
	get: function() {
		if (this.length == 0) return undefined;
		return this.getNode(0);
	}
};

/**
 * <p>The last node or undefined, if this way doesn't have nodes 
 * (i.e. because it is a proxy way).</p>
 * 
 * @type org.openstreetmap.josm.data.osm.Node
 * @field
 * @instance
 * @name last
 * @memberof WayMixin
 * @summary The last node
 */	
mixin.last = {
	get: function() {
		if (this.length == 0) return undefined;
		return this.getNode(this.length-1);
	}
};

/**
 * <p>Removes one or more nodes from the way.</p>
 * 
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">remove(n1,n2, ...)</code></dt>
 *   <dd>Removes the nodes. <code>n</code><em>i</em> are instances of {@class org.openstreetmap.josm.data.osm.Node}.</dd>
 *
 *    <dt><code class="signature">remove(array|collection)</code></dt>
 *   <dd>Removes the nodes. <code>array</code> is a javascript array of {@class org.openstreetmap.josm.data.osm.Node}s,
 *   <code>collection</code> is a java collection of {@class org.openstreetmap.josm.data.osm.Node}s.</dd>
 * </dl>
 * 
 * @method
 * @instance
 * @name remove
 * @memberOf WayMixin
 * @summary Removes one or more nodes from the way.
 */	
mixin.remove = function(){
	var candidates = new HashSet();
	function remember(o){
		if (util.isNothing(o)) return;
		if (o instanceof Node) {
			candidates.add(o);
		} else if (util.isArray(o)) {
			for(var i=0; i< o.length; i++) remember(o[i]);
		} else if (o instanceof Collection){
			for(var it= o.iterator(); it.hasNext();) remember(it.next());
		} else {
			// ignore 
		}
	}
	var oldnodes = this.nodes;
	for (var i=0;i < arguments.length; i++) remember(arguments[i]);
	this.removeNodes(candidates);
	if (!nodeListEquals(oldnodes, this.nodes) && !this.modified) this.modified = true;
};

exports.mixin = util.mix(require("josm/mixin/OsmPrimitiveMixin").mixin,mixin);

}());