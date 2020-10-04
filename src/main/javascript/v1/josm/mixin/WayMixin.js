/**
 * This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.data.osm.Way}.
 *
 * @module josm/mixin/WayMixin
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Node = org.openstreetmap.josm.data.osm.Node;
var Collection = java.util.Collection;
var HashSet = java.util.HashSet;


/**
 * This mixin  provides additional properties and methods which you can
 * invoke on an instance of {@class org.openstreetmap.josm.data.osm.Way}.
 *
 * You can access nodes using indexed properties, i.e.
 * <pre>way[i]</pre>, see example below.
 *
 * @mixin WayMixin
 * @extends OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.Way
 */
exports.mixin = {};
exports.forClass = org.openstreetmap.josm.data.osm.Way;


function nodeListEquals(l1, l2) {
    if (l1.length != l2.length) return false;
    for(var i=0; i<l1.length; i++) {
        if (l1[i].id != l2[i].id) return false;
    }
    return true;
};

/**
 * Set or get the nodes of a way.
 *
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the nodes as array.</dd>
 *   <dt>set</dt>
 *   <dd>Set the an array or {@class java.util.List} of
 *       {@class org.openstreetmap.josm.data.osm.Node}s.</dd>
 * </dl>
 *
 * @example
 * var way = ...       // create the way;
 * var n1,n2,n3 = ...; // create the nodes
 * way.nodes = [n1,n2,n3];
 * way.nodes;  // -> [n1,n2,n3]
 *
 * @name nodes
 * @property {org.openstreetmap.josm.data.osm.Way.Node[]} nodes the nodes
 * @instance
 * @memberof module:josm/mixin/WayMixin~WayMixin
 * @summary  Set or get the nodes of a way.
 */
exports.mixin.nodes= {
    get: function() {
        if (this.isIncomplete) return undefined;
        var nodes = [];
        for(var it = this.getNodes().iterator(); it.hasNext();) {
            nodes.push(it.next());
        }
        return nodes;
    },
    set: function(val) {
        util.assert(util.isSomething(val),
            "nodes must not be null or undefined");
        var oldnodes = this.nodes;
        if (util.isArray(val) || val instanceof java.util.List) {
            this.setNodes(val);
        } else {
            util.assert(false, "Expected array or list of nodes, got {0}", val);
        }
        if (!nodeListEquals(oldnodes, this.nodes) && !this.modified) {
            this.modified = true;
        }

    }
};

/**
 * Replies the number of nodes.
 *
 * @name length
 * @property {number} length number of nodes
 * @instance
 * @memberof module:josm/mixin/WayMixin~WayMixin
 * @readOnly
 * @summary Replies the number of nodes.
 */
exports.mixin.length =  {
    get: function() {
        return this.getNodesCount();
    }
};

exports.mixin.__getByIndex =  function(idx) {
    util.assert(idx >= 0 && idx < this.length,
        "Node index out of range, got {0}", idx);
    return this.getNode(idx);
};

/**
 * Checks if a node is part of a way.
 *
 * @param {org.openstreetmap.josm.data.osm.Node} node  the node to check
 * @function
 * @instance
 * @name contains
 * @returns {boolean} true, if the node is part of the way
 * @memberof module:josm/mixin/WayMixin~WayMixin
 * @summary Checks if a node is part of a way.
 */
exports.mixin.contains =  function(node) {
    if (util.isNothing(node)) return false;
    util.assert(node instanceof Node, "Expected a Node, got {0}", node);
    return this.containsNode(node);
};

/**
 * The first node or undefined, if this way doesn't have nodes
 * (i.e. because it is a proxy way).
 *
 * @property {org.openstreetmap.josm.data.osm.Node} first the node or undefined
 * @name first
 * @instance
 * @memberof module:josm/mixin/WayMixin~WayMixin
 * @summary The first node
 */
exports.mixin.first = {
    get: function() {
        if (this.length == 0) return undefined;
        return this.getNode(0);
    }
};

/**
 * The last node or undefined, if this way doesn't have nodes
 * (i.e. because it is a proxy way).
 *
 * @property {org.openstreetmap.josm.data.osm.Node} last the node or undefined
 * @name last
 * @instance
 * @memberof module:josm/mixin/WayMixin~WayMixin
 * @summary The last node
 */
exports.mixin.last = {
    get: function() {
        if (this.length == 0) return undefined;
        return this.getNode(this.length-1);
    }
};

/**
 * Removes one or more nodes from the way.
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">remove(n1,n2, ...)</code></dt>
 *   <dd>Removes the nodes. <code>n</code><em>i</em> are instances of
 *       {@class org.openstreetmap.josm.data.osm.Node}.</dd>
 *
 *    <dt><code class="signature">remove(array|collection)</code></dt>
 *   <dd>Removes the nodes. <code>array</code> is a javascript array of
 *   {@class org.openstreetmap.josm.data.osm.Node}s,
 *   <code>collection</code> is a java collection of
 *   {@class org.openstreetmap.josm.data.osm.Node}s.</dd>
 * </dl>
 *
 * @function
 * @param {...org.openstreetmap.josm.data.osm.Node | array | java.util.Collection} nodes the nodes to remove
 * @name remove
 * @instance
 * @memberOf  module:josm/mixin/WayMixin~WayMixin
 * @summary Removes one or more nodes from the way.
 */
exports.mixin.remove = function(){
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
    if (!nodeListEquals(oldnodes, this.nodes) && !this.modified) {
        this.modified = true;
    }
};

exports.mixin = util.mix(require("josm/mixin/OsmPrimitiveMixin").mixin,mixin);