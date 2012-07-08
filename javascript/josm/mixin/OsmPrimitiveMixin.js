(function() {
	
/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.data.osm.OsmPrimitive}.</p>
 * 
 * @module josm/mixin/OsmPrimitiveMixin 
 */
var util = require("josm/util");
var User = org.openstreetmap.josm.data.osm.User;
var Tags = org.openstreetmap.josm.plugins.scripting.js.api.Tags;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var Map  = java.util.Map;

/**
* <p>OsmPrimitiveMixin provides additional properties and methods which you can invoke on an instance of
 * {@class org.openstreetmap.josm.data.osm.OsmPrimitive}.
 *  
 * @mixin OsmPrimitiveMixin
 * @memberOf josm/mixin/OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.OsmPrimitive
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
 * @summary The unique numeric id
 */
mixin.id = {
	get: function() {
		return this.getUniqueId();
	}
};

/**
 * <p>The version of the object.</p>
 * 
 * <p>Only global objects have a version. The version for local and proxy objects is <var>undefined</var>.</p>
 * 
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 * 
 * // local object - get version is undefined  
 * var node = nb.create();
 * node.version;  // -&gt; undefined
 * 
 * // global object - get version is defined  
 * var node = nb.create(12345, {version: 9});
 * node.version;  // -&gt; 9
 * 
 * // proxy object - get version is undefined  
 * var node = nb.createProxy(12345);
 * node.version;  // -&gt; undefined 
 * 
 * @memberOf OsmPrimitiveMixin
 * @name version
 * @field
 * @instance
 * @readOnly
 * @type {number}
 * @summary The version of the object.
 */
mixin.version = {
	get: function() {
		var version = this.$getVersion();
		return version == 0 ? undefined : version;
	}
};

/**
 * <p>Replies true, if this is a local primitive.</p>
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
 * @summary Replies true, if this is a local primitive.
 * @alias local
 * @alias isNew
 */
mixin.isLocal =  mixin.local = mixin.isNew = {
	get: function() {
		return this.$isNew();
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
 * @alias global
 * @field
 * @instance
 * @readOnly
 * @type {boolean}
 * @summary Replies true, if this is a global primitive
 */
mixin.isGlobal = mixin.global = {
	get: function() {
		return ! this.$isNew();
	}
};

/**
 * <p>Replies the parent dataset or undefined, if this primitive is not attached to a dataset.</p>
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
 * @summary Replies the parent dataset.
 * 
 */
mixin.dataSet = {
	get: function() {
		var ds = this.$getDataSet();
		return ds == null? undefined: ds;
	}
};

/**
 * <p>Set or get the user.</p>
 * 
 * <p><strong>get:</strong> - replies a User object or undefined, if not user is set.</p>
 * 
 * <p><strong>set:</strong> - assign a user, either as instance of User, supplying a user name or the unique
 * global user id as number. Assign null or undefined to assign no user.</p>
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
 * @memberOf OsmPrimitiveMixin
 * @name user
 * @field
 * @instance
 * @type org.openstreetmap.josm.data.osm.DataSet
 * @summary Set or get the user.
 */
mixin.user = {
	get: function() {
		var user = this.$getUser();
		return user == null ? undefined : user;
	},
	set: function(val) {
		if (util.isNothing(val)) {
			this.$setUser(null);
		} else if (val instanceof User){
			this.$setUser(val);
		} else if (util.isString(val)) {
			var user = User.getByName(val);
			util.assert(user != null, "Can''t set user with name ''{0}''. User doesn''t exist (yet) in the local user cache.", val);
			util.assert(user.size() == 1, "Can''t set user with name ''{0}''. Found {0} matching users.", val, user.size());			
			this.$setUser(user.get(0));
		} else if (util.isNumber(val)) {
			var user = User.getById(val);
			util.assert(user != null, "Can''t set user with id {0}'. User doesn''t exist (yet) in the local user cache.", val);
			this.$setUser(user);
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
 * @summary et or get the changeset id this primitive was last modified in.
 * 
 */
mixin.changesetId = {
	get: function() {
		var cid = this.$getChangesetId();
		return cid == 0 ? undefined: cid;
	},
	
	set: function(val) {
		util.assert(util.isSomething(val), "null or undefined not allowed");
		util.assert(util.isNumber(val), "Expected a number, got {0}", val);
		util.assert(val > 0, "Expected a number > 0, got {0}", val);
		this.$setChangesetId(val);
	}		
};

/**
 * <p>Get the timestamp this primitive was last modified on the server. Undefined, if this timestamp
 * isn't known, i.e. for local primitives or for proxy primitives.</p>
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
 * @summary Get the timestamp this primitive was last modified on the server.
 */
mixin.timestamp = {
	get: function() {
		if (this.$isTimestampEmpty()) return undefined;
		return this.$getTimestamp();
	}
};

/**
 * <p>Replies true if this a proxy object, in JOSM terminology called an incomplete object.</p>
 * 
 * @memberOf OsmPrimitiveMixin
 * @name isIncomplete
 * @alias isProxy
 * @alias incomplete
 * @alias proxy
 * @field
 * @instance
 * @readOnly
 * @type {boolean}
 * @summary Replies true if this is a proxy object
 */
mixin.isIncomplete = mixin.isProxy = mixin.incomplete = mixin.proxy = {
	get: function() {
		return this.$isIncomplete();
	}	
};

/**
 * <p>Sets or gets whether this object is <em>modified</em>.</p>
 *
 * <p>Supported alias: <code>modified</code>.</p>
 * 
 * @example
 * var node = ....           // create a node
 * node.modified;            // -> true or false 
 * node.isModified = true;   // sets the modified flag  to true 
 * 
 * @summary Sets or gets whether this object is <em>modified</em>.
 * @name isModified
 * @alias modified 
 * @field
 * @instance
 * @type boolean
 */
mixin.isModified = mixin.modified = {
	get: function() {
		return this.$isModified();
	},
	set: function(value) {
		util.assert(util.isDef(value), "value: missing mandatory value");
		this.$setModified(Boolean(value));
	}
};

function applyTagMap(obj, tags) {
	if (util.isNothing(tags)) return;
	for(var it=tags.keySet().iterator(); it.hasNext();){
		var key = it.next();
		var value = tags.get(key);
		if (util.isNothing(key)) continue;
		if (util.isNothing(value)) continue;
		key = util.trim(key + "");		
		value = value + "";
		if (! obj.has(key) || value != obj.get(key)) {
			obj.$put(key, value);
			if (!obj.modified) obj.modified = true;
		}
	}
};

function applyTagObject(obj,tags) {
	if (util.isNothing(tags)) return;
	for(var p in tags) {			
		if (!tags.hasOwnProperty(p)) continue;
		var value = tags[p];
		var key = util.trim(p);
		if (util.isNothing(value)) continue;
		var value = value + "";
		if (! obj.has(key) || value != obj.get(key)) {
			obj.$put(key, value);
			if (!obj.modified) obj.modified = true;
		}
	}
};

/**
 * <p>Get or set the tags of the object.</p>
 * 
 * <p><strong>get:</strong> - replies the tags as javascript object.</p>
 * 
 *  <p><strong>set:</strong>
 *    <ul>
 *      <li>assign null or undefined to remove all tags</li>
 *      <li>assign an object to set its properties as tags.</li>
 *      <li>assign an java.util.Map to set its elements as tags</li>
 *    </ul>
 *  </p>
 *  
 *  <p>null values and undefined tag values aren't assigned. tag keys are normalized, i.e. leading and
 *  trailing white space is removed. Both, tag keys and tag values, are converted to strings.</p>
 *  
 * @example
 * var node = .... // create a node
 * 
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"}; 
 * node.tags.amenity;  // -> restaurant
 * node.tags.name;     // -> Obstberg
 * 
 * // remove all tags 
 * node.tags = null;
 * 
 * // set tags using a java map 
 * var tags = new java.util.HashMap();
 * tags.put("amenity", "restaurant");
 * tags.put("name", "Obstberg");
 * node.tags = tags; 
 * 
 * @memberOf OsmPrimitiveMixin
 * @name tags
 * @field
 * @instance
 * @type {object}
 * @summary Get or set the tags of the object.
 */
mixin.tags = {
	get: function() {
		return new Tags(this);
	},
	set: function(tags) {
		var ntags = this.keySet().size();
		this.removeAll();			
		if (ntags && ! this.modified) this.modified = true;
		if (util.isNothing(tags)) {
			// skip
		} else if (tags instanceof Map) {
			applyTagMap(this, tags);
		} else if (typeof tags === "object") {
			applyTagObject(this, tags);
		} else {
			util.assert(false, "Can''t assign tags from object {0}", tags);
		}
	}
};

/**
 * <p>Replies an array with the tag keys.</p>
 *   
 * @example
 * var node = .... // create a node
 * 
 * // get the tag keys
 * var keys = node.keys; 

 * @memberOf OsmPrimitiveMixin
 * @name keys
 * @field
 * @instance
 * @type {array}
 * @summary Replies an array with the tag keys.
 */
mixin.keys = {
	get: function() {
		var ret = [];
		for (var it = this.keySet().iterator(); it.hasNext();) ret.push(it.next());
		return ret; 
	}	
};

/**
 * <p>Replies the value of a tag, or undefined, if the tag isn't set.</p>
 * 
 * @example
 * var node = .... // create a node
 * 
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"}; 
 * node.get("amenity");  // -> restaurant
 * node.get("name");     // -> Obstberg
 * 
 * @memberOf OsmPrimitiveMixin
 * @param {string} name the tag name. Must not be null or undefined. Non-string values are converted to
 *   a string. Leading and trailing whitespace is removed.
 * @name get
 * @method
 * @instance
 * @type {string}
 * @summary Replies the value of a tag.
 */
mixin.get = function(name) {
	if (util.isNothing(name)) return undefined;
	name = util.trim(name + "");
	var value = this.$get(name);
	return value == null ? undefined : value;
};

/**
 * <p>Set a tag or a collection of tags.</p>
 * 
 * <p><strong>Signatures</strong></p>
 * <dl>
 *   <dt><strong>set(name,value)</strong></dt>
 *   <dd>sets a tag given a name and a value.<br/>
 *   <var>name</var> must not be null or undefined. Non-string values are converted to
 *   a string. Leading and trailing whitespace is removed.<br/>
 *   <var>value</var>: if null or undefined, the tag is removed. Any other value is converted to a string
 *   </dd>
 *   
 *   <dt><strong>set(tags)</strong></dt>
 *   <dd><var>tags</var> is either a java object whose properties are set as tags, or a java.util.Map.</dd>
 * </dl>
 * 
 * 
 * @example
 * var node = .... // create a node
 * 
 * // set the tags using a javascript object
 * node.set("amenity", "restaurant");
 * node.set("name", "obstberg"); 
 * node.get("amenity");  // -> restaurant
 * node.get("name");     // -> Obstberg
 * 
 * // set the tags using an object
 * node.set({amenity:"restaurant", name: "Obstberg"});
 * node.get("amenity");  // -> restaurant
 * node.get("name");     // -> Obstberg
 * 
 * @memberOf OsmPrimitiveMixin
 * @name set
 * @method
 * @instance
 * @type {string}
 * @summary Set a tag or a collection of tags.
 */
mixin.set = function() {
	function setWithNameAndValue(name,value){
		util.assert(util.isSomething(name), "name: must not be null or undefined");
		name = util.trim(name + "");
		if (util.isNothing(value)) {
			this.remove(name);			
		} else {
			value = value + "";
			if (!this.has(name) || value != this.get(name)) {
				this.$put(name,value);
				if (!this.modified) this.modified = true;
			}
		}		
	}
	
	function setFromObject(tags) {
		if (util.isNothing(tags)) return;
		if (tags instanceof Map) {
			applyTagMap(this,tags);
		} else if (typeof tags === "object") {
			applyTagObject(this, tags);
		} else {
			util.assert(false, "Can''t assign tags from object {0}", tags);
		}
	}
	
	switch(arguments.length){
	case 0: 
		return;
	case 1: 
		setFromObject.call(this,arguments[0]);
		break;
	case 2: 
		setWithNameAndValue.call(this, arguments[0], arguments[1]);
		break;
	}
};

/**
 * <p>Replies true, if the object has a tag with key <var>key</var>.</p>
 * 
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><strong>has(key)</strong></dt>
 *   <dd> <var>key</var> is a string. Replies true, if the 
 *   object has a tag with this key. Before matching, leading and trailing white space
 *   is removed from <var>key</var>.</dd>
 *   
 *   <dt><strong>has(regexp)</strong></dt>
 *   <dd><var>regexp</var> is a regular expression. Replies true, if the object has at least
 *   one tag whose key matches with <var>regexp</var>.</dd>
 * </dl>
 * 
 * @example
 * var node = .... // create a node
 * 
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"}; 
 * 
 * // test wheter the tags are set 
 * node.has("amenity");     // -> true
 * node.has("no-such-tag"); // -> false
 * 
 *  // use a regexp
 * node.has(/^a/);               // -> true
 * node.has(/^name(:.*)?$/i);    // -> false 
 * 
 * @memberOf OsmPrimitiveMixin
 * @param {string} key the tag key. Must not be null or undefined. Non-string values are converted to
 *   a string. Leading and trailing whitespace is removed.
 * @name has
 * @method
 * @instance
 * @type {boolean}
 * @summary Checks whether a primitive has a tag.
 */
mixin.has = function(key) {
	if (util.isNothing(key)) return false;
	// Strange: key instanceof RegExp doesn't work 
	if (key.constructor.name == "RegExp") {
		for(var it=this.keySet().iterator(); it.hasNext();) {
			if (key.test(it.next())) return true;
		}
		return false; 
	} else {
		key = util.trim(key + "");
		return this.$hasKey(key);		
	}
};

/**
 * <p>Removes a tag.</p>
 * 
 * @example
 * var node = .... // create a node
 * 
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"}; 
 * node.remove("amenity");
 * node.has("amenity"); // -> false 
 * 
 * @memberOf OsmPrimitiveMixin
 * @param {string} name the tag name. Must not be null or undefined. Non-string values are converted to
 *   a string. Leading and trailing whitespace is removed.
 * @name remove
 * @method
 * @instance
 * @type {boolean}
 * @summary Removes a tag.
 */
mixin.remove = function(key){
	if (util.isNothing(key)) return;
	key = util.trim(key + "");
	if (this.has(key)) {
		this.$remove(key);
		if (! this.modified) this.modified = true;
	}
};

/**
* <p>Replies true if this object is a node</p>
* 
* @memberOf OsmPrimitiveMixin
* @name isNode
* @field
* @instance
* @readOnly
* @type {boolean}
* @summary Replies true if this object is a node 
*/
mixin.isNode = {
	get: function() {
		return this.getType() == OsmPrimitiveType.NODE;
	}
};

/**
* <p>Replies true if this object is a way</p>
* 
* @memberOf OsmPrimitiveMixin
* @name isWay
* @field
* @instance
* @readOnly
* @type {boolean}
* @summary Replies true if this object is a way 
*/
mixin.isWay = {
	get: function() {
		return this.getType() == OsmPrimitiveType.WAY;
	}
};


/**
* <p>Replies true if this object is a relation</p>
* 
* @memberOf OsmPrimitiveMixin
* @name isRelation
* @field
* @instance
* @readOnly
* @type {boolean}
* @summary Replies true if this object is a relation
*/
mixin.isRelation = {
	get: function() {
		return this.getType() == OsmPrimitiveType.RELATION;
	}
};

/**
* <p>Set or get wheter this primitive is deleted.</p>
* 
* <p>In order to invoke the native method <code>isDeleted()</code>, prefix the name
* with $, i.e.
* <pre>
*    node.$isDeleted(); // -> same as node.isDeleted  without parantheses
* </pre>
* </p>
* 
* @memberOf OsmPrimitiveMixin
* @name isDeleted
* @alias deleted
* @field
* @instance
* @type {boolean}
* @summary Set or get wheter this primitive is deleted.
*/
mixin.isDeleted = mixin.deleted = {
	get: function() {
		return this.$isDeleted();
	},
	set: function(value) {
		util.assert(util.isDef(value), "value: mandatory value is missing");
		util.assert(typeof value === "boolean", "value: expected a boolean, got {0}", value);
		this.$setDeleted(value);
	}
};

exports.forClass = org.openstreetmap.josm.data.osm.OsmPrimitive;
exports.mixin = mixin;

}());