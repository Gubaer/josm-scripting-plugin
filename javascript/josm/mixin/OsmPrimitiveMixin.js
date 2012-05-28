/**
 * <p>Additional properties and functions for JOSMs internal class OsmPrimitive.</p> 
 * 
 * @module josm/mixin/OsmPrimitiveMixin
 *     
 */
var util = require("josm/util");
var User = org.openstreetmap.josm.data.osm.User;

/**
* <p>OsmPrimitiveMixin provides additional properties and methods which you can invoke on an instance of
 * {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/osm/OsmPrimitive.java|OsmPrimitive}. 
 * The native methods of OsmPrimitives are still available for scripting, 
 * unless they are shadowed by names of properties defined in this mixin.</p>
 * 
 * @mixin OsmPrimitiveMixin
 */
var mixin = {};

/**
 * <p>The unique numeric id, positive for global primitives, negative for local primitives.</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // get members 
 * var node = nb.create();
 * var id = node.id;   // -> a negative value  
 * 
 * @memberOf OsmPrimitiveMixin
 * @name id
 * @field
 * @instance
 * @readOnly
 * @type {number}
 * 
 */
mixin.id = {
	get: function() {
		return this.getUniqueId();
	}
};

/**
 * <p>Replies true, if this is a local primitive</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // get members 
 * var node = nb.create();
 * node.isLocal;   // -> true
 * 
 * node = nb.create(1234);
 * node.isLocal;   // -> false  

 * 
 * @memberOf OsmPrimitiveMixin
 * @name isLocal
 * @field
 * @instance
 * @readOnly
 * @type {boolean}
 * 
 */
mixin.isLocal =  {
	get: function() {
		return this.isNew();
	}
};

/**
 * <p>Replies true, if this is a global primitive</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // get members 
 * var node = nb.create();
 * node.isGlobal;   // -> false
 * 
 * node = nb.create(1234);
 * node.isGlobal;   // -> true  

 * 
 * @memberOf OsmPrimitiveMixin
 * @name isGlobal
 * @field
 * @instance
 * @readOnly
 * @type {boolean}
 * 
 */
mixin.isGlobal = {
	get: function() {
		return ! this.isNews();
	}
};

/**
 * <p>Replies the parent dataset or undefiend, if this primitive is not attached to a dataset.</p>
 * 
 * @example
 * var ds = new DataSet();
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 * 
 * // get members 
 * var n1 = nb.create();
 * n1.dataSet;  // -> ds
 * 
 * var nb = require("josm/builder").NodeBuilder;
 * var n2 = nb.create();
 * n2.dataSet;   // -> undefined  
 * 
 * @memberOf OsmPrimitiveMixin
 * @name dataSet
 * @field
 * @instance
 * @readOnly
 * @type org.openstreetmap.josm.data.osm.DataSet
 * 
 */
mixin.dataSet = {
	get: function() {
		var ds = this.getDataSet();
		return ds == null? undefined: ds;
	}
};

/**
 * <p>Set or get the user.</p>
 * 
 * <p><strong>get:</strong> - replies a User object or undefined, if not user is set.</p>
 * 
 * <p><strong>set:</strong> - assign a user, either as instance of User, supplying a user name or the unique
 * global user id as number.</p>
 * 
 * @example
 * var User = org.openstreetmap.josm.data.osm.User; 
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 * 
 *  
 * var n1 = nb.create();
 * // assign a user object 
 * n1.user = new User("foobar");
 * // assign the unique global user with name 'foobar'
 * n1.user = "foobar"; 
 * // assign the unique global user with id 12345
 * n1.user = 12345;
 * 
 * 
 * @memberOf OsmPrimitiveMixin
 * @name dataSet
 * @field
 * @instance
 * @type org.openstreetmap.josm.data.osm.DataSet
 * 
 */
mixin.user = {
	get: function() {
		var user = this.getUser();
		return user == null ? undefined : user;
	},
	set: function(val) {
		util.assert(util.isSomething(user), "null or undefined not allowed");
		if (val instanceof User){
			this.setUser(val);
		} else if (util.isString(val)) {
			var user = User.getByName(val);
			util.assert(user != null, "Can''t set user with name ''{0}''. User doesn''t exist (yet) in the local user cache.", val);
			this.setUser(user);
		} else if (util.isNumber(val)) {
			var user = User.getById(val);
			util.assert(user != null, "Can''t set user with id {0}'. User doesn''t exist (yet) in the local user cache.", val);
			this.setUser(user);
		} else {
			util.assert(false, "Unexpected type of value, got {0}", val);
		}
	}
};

/**
 * <p>Set or get the changeset id this primitive was last modified in.</p>
 * 
 * <p><strong>get:</strong> - replies the changeset id or undefined, if no changeset id is known (i.e. 
 * for local primitives or proxy primitives).</p>
 * 
 * <p><strong>set:</strong> - assign a changset id, a number &gt, 0.</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 * 
 * var n1 = nb.create(12345);
 * // assign the changeset id  
 * n1.changesetId = 6;
 * 
 * @memberOf OsmPrimitiveMixin
 * @name changesetId
 * @field
 * @instance
 * @type {number}
 * 
 */
mixin.changesetId = {
	get: function() {
		var cid = this.getChangesetId();
		return cid == 0 ? undefined: cid;
	},
	
	set: function(val) {
		util.assert(util.isSomething(val), "null or undefined not allowed");
		util.assert(util.isNumber(val), "Expected a number, got {0}", val);
		util.assert(val > 0, "Expected a number > 0, got {0}", val);
		this.setChangesetId(id);
	}		
};

/**
 * <p>Get the timestamp this primitive was last modified on the server. Undefined, if this timestamp
 * isn't known, i.e. for local primitives or for proxy primitives.</p>
 * 
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 * 
 * var n1 = ... // assume n1 was downloaded from the server 
 * n1.timestamp;  // the timestamp of last modification
 * 
 * @memberOf OsmPrimitiveMixin
 * @name timestamp
 * @field
 * @instance
 * @readOnly
 * @type java.util.Date
 * 
 */
mixin.timestamp = {
	get: function() {
		if (this.isTimestampEmpty()) return undefined;
		return this.getTimestamp();
	}
};


/**
 * <p>Synonym for isLocal.</p>
 * 
 * @memberOf OsmPrimitiveMixin
 * @name isNew
 * @field
 * @instance
 * @readOnly
 * @type {boolean}
 * 
 */
mixin.isNew = mixin.isLocal;

exports.forClass = org.openstreetmap.josm.data.osm.OsmPrimitive;
exports.mixin = mixin;