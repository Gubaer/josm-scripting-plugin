/**
 * This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.data.osm.OsmPrimitive}.
 *
 * @module josm/mixin/OsmPrimitiveMixin
 */
var util = require("josm/util");
var User = org.openstreetmap.josm.data.osm.User;
var Tags = org.openstreetmap.josm.plugins.scripting.js.api.Tags;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;
var Map  = java.util.Map;

/**
 * OsmPrimitiveMixin provides additional properties and methods which you
 * can invoke on an instance of
 * {@class org.openstreetmap.josm.data.osm.OsmPrimitive}.
 *
 * @mixin OsmPrimitiveMixin
 * @forClass org.openstreetmap.josm.data.osm.OsmPrimitive
 */
exports.mixin = {};
exports.forClass = org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * The unique numeric id, positive for global primitives, negative for local
 * primitives.
 *
 * @example
 * var nb = require("josm/builder").NodeBuilder;
 *
 * // get members
 * var node = nb.create();
 * var id = node.id;   // -> a negative value
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name id
 * @property {number} id  the numberic id
 * @readOnly
 * @summary The unique numeric id
 * @instance
 */
exports.mixin.id = {
    get: function() {
        return this.getUniqueId();
    }
};

/**
 * The version of the object.
 *
 * Only global objects have a version. The version for local and proxy
 * objects is <var>undefined</var>.
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name version
 * @property {number} version the primitive version
 * @readOnly
 * @summary The version of the object.
 * @instance
 */
exports.mixin.version = {
    get: function() {
        var version = this.$getVersion();
        return version == 0 ? undefined : version;
    }
};

/**
 * Replies true, if this is a local primitive.
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name isLocal
 * @property {boolean} isLocal true, if this is a local primitive
 * @readOnly
 * @summary Replies true, if this is a local primitive.
 * @alias local
 * @alias isNew
 * @instance
 */
exports.mixin.isLocal = exports.mixin.local = exports.mixin.isNew = {
    get: function() {
        return this.$isNew();
    }
};

/**
 * Replies true, if this is a global primitive
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name isGlobal
 * @alias global
 * @property {boolean} isGlobal  true, if this is a global primitive
 * @readOnly
 * @summary Replies true, if this is a global primitive
 * @instance
 */
exports.mixin.isGlobal = exports.mixin.global = {
    get: function() {
        return ! this.$isNew();
    }
};

/**
 * Replies the parent dataset or undefined, if this primitive is not
 * attached to a dataset.
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name dataSet
 * @readOnly
 * @property {org.openstreetmap.josm.data.osm.DataSet} dataSet  the parent dataset
 * @summary Replies the parent dataset.
 * @instance
 *
 */
exports.mixin.dataSet = {
    get: function() {
        var ds = this.$getDataSet();
        return ds == null? undefined: ds;
    }
};

/**
 * Set or get the user.
 *
 * <strong>get:</strong> - replies a User object or undefined, if not
 * user is set.
 *
 * <strong>set:</strong> - assign a user, either as instance of User,
 * supplying a user name or the unique global user id as number. Assign null
 * or undefined to assign no user.
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name user
 * @property {org.openstreetmap.josm.data.osm.User} user  the user who last modified the primitive
 * @summary Set or get the user.
 * @instance
 */
exports.mixin.user = {
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
            util.assert(user != null,
                "Can''t set user with name ''{0}''. User doesn''t exist (yet) "
              + "in the local user cache.", val);
            util.assert(user.size() == 1,
                "Can''t set user with name ''{0}''. Found {0} matching users.",
                val, user.size());
            this.$setUser(user.get(0));
        } else if (util.isNumber(val)) {
            var user = User.getById(val);
            util.assert(user != null,
                "Can''t set user with id {0}'. "
              + "User doesn''t exist (yet) in the local user cache.", val);
            this.$setUser(user);
        } else {
            util.assert(false, "Unexpected type of value, got {0}", val);
        }
    }
};

/**
 * Set or get the changeset id this primitive was last modified in.
 *
 * <strong>get:</strong> - replies the changeset id or undefined, if no
 * changeset id is known (i.e.  for local primitives or proxy primitives).
 *
 * <strong>set:</strong> - assign a changset id, a number &gt, 0.
 *
 * @example
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 *
 * var n1 = nb.create(12345);
 * // assign the changeset id
 * n1.changesetId = 6;
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name changesetId
 * @property {number} changesetId  the changset id
 * @summary set or get the changeset id this primitive was last modified in.
 * @instance
 *
 */
exports.mixin.changesetId = {
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
 * Get the timestamp this primitive was last modified on the server.
 * Undefined, if this timestamp isn't known, i.e. for local primitives or
 * for proxy primitives.
 *
 * @example
 * var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
 *
 * var n1 = ... // assume n1 was downloaded from the server
 * n1.timestamp;  // the timestamp of last modification
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name timestamp
 * @readOnly
 * @property {java.util.Date} timestamp the timestamp of last modification
 * @summary Get the timestamp this primitive was last modified on the server.
 * @instance
 */
mixin.timestamp = {
    get: function() {
        if (this.$isTimestampEmpty()) return undefined;
        return this.$getTimestamp();
    }
};

/**
 * Replies true if this a proxy object, in JOSM terminology called an
 * incomplete object.
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name isIncomplete
 * @alias isProxy
 * @alias incomplete
 * @alias proxy
 * @readOnly
 * @property {boolean} isIncomplete true if this is a proxy object
 * @summary Replies true if this is a proxy object
 * @instance
 */
exports.mixin.isIncomplete = exports.mixin.isProxy = exports.mixin.incomplete = exports.mixin.proxy = {
    get: function() {
        return this.$isIncomplete();
    }
};

/**
 * Sets or gets whether this object is <em>modified</em>.
 *
 * Supported alias: <code>modified</code>.
 *
 * @example
 * var node = ....           // create a node
 * node.modified;            // -> true or false
 * node.isModified = true;   // sets the modified flag  to true
 *
 * @summary Sets or gets whether this object is <em>modified</em>.
 * @name isModified
 * @alias modified
 * @property {boolean} isModified  true, if modified
 * @memberof module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @instance
 */
exports.mixin.isModified = exports.mixin.modified = {
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
 * Get or set the tags of the object.
 *
 * <strong>get:</strong> - replies the tags as javascript object.
 *
 *  <strong>set:</strong>
 *    <ul>
 *      <li>assign null or undefined to remove all tags</li>
 *      <li>assign an object to set its properties as tags.</li>
 *      <li>assign an java.util.Map to set its elements as tags</li>
 *    </ul>
 *  
 *
 *  null values and undefined tag values aren't assigned. tag keys are
 *  normalized, i.e. leading and trailing white space is removed. Both, tag
 *  keys and tag values, are converted to strings.
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name tags
 * @property {object} tags  the tags
 * @summary Get or set the tags of the object.
 * @instance
 */
exports.mixin.tags = {
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
 * Replies an array with the tag keys.
 *
 * @example
 * var node = .... // create a node
 *
 * // get the tag keys
 * var keys = node.keys;

 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name keys
 * @property {array} keys  the tag keys
 * @summary Replies an array with the tag keys.
 * @readonly
 * @instance
 */
exports.mixin.keys = {
    get: function() {
        var ret = [];
        for (var it = this.keySet().iterator(); it.hasNext();) {
            ret.push(it.next());
        }
        return ret;
    }
};

/**
 * Replies the value of a tag, or undefined, if the tag isn't set.
 *
 * @example
 * var node = .... // create a node
 *
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"};
 * node.get("amenity");  // -> restaurant
 * node.get("name");     // -> Obstberg
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @param {string} name the tag name. Must not be null or undefined.
 *   Non-string values are converted to a string. Leading and trailing
 *   whitespace is removed.
 * @name get
 * @function
 * @returns {string} the tag value
 * @summary Replies the value of a tag.
 */
exports.mixin.get = function(name) {
    if (util.isNothing(name)) return undefined;
    name = util.trim(name + "");
    var value = this.$get(name);
    return value == null ? undefined : value;
};

/**
 * Set a tag or a collection of tags.
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><strong>set(name,value)</strong></dt>
 *   <dd>sets a tag given a name and a value.<br/>
 *   <var>name</var> must not be null or undefined. Non-string values are
 *   converted to a string. Leading and trailing whitespace is removed.<br/>
 *   <var>value</var>: if null or undefined, the tag is removed. Any other
 *   value is converted to a string
 *   </dd>
 *
 *   <dt><strong>set(tags)</strong></dt>
 *   <dd><var>tags</var> is either a java object whose properties are set as
 *   tags, or a java.util.Map.</dd>
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name set
 * @function
 * @param {any} tag key/value pair, see description
 * @summary Set a tag or a collection of tags.
 */
exports.mixin.set = function() {
    function setWithNameAndValue(name,value){
        util.assert(util.isSomething(name),
            "name: must not be null or undefined");
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
 * Replies true, if the object has a tag with key <var>key</var>.
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><strong>has(key)</strong></dt>
 *   <dd> <var>key</var> is a string. Replies true, if the
 *   object has a tag with this key. Before matching, leading and trailing
 *   white space is removed from <var>key</var>.</dd>
 *
 *   <dt><strong>has(regexp)</strong></dt>
 *   <dd><var>regexp</var> is a regular expression. Replies true, if the
 *   object has at least one tag whose key matches with <var>regexp</var>.</dd>
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
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @name has
 * @function
 * @returns {boolean} true, if there is a tag with this key
 * @param {(string | regexp)} key  the tag key we are looking up
 * @summary Checks whether a primitive has a tag.
 */
exports.mixin.has = function(key) {
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
 * Removes a tag.
 *
 * @example
 * var node = .... // create a node
 *
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"};
 * node.remove("amenity");
 * node.has("amenity"); // -> false
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @param {string} name the tag name. Must not be null or undefined.
 *   Non-string values are converted to a string. Leading and trailing
 *   whitespace is removed.
 * @name removeTag
 * @function
 * @summary Removes a tag.
 */
exports.mixin.removeTag = function(key){
    if (util.isNothing(key)) return;
    key = util.trim(key + "");
    if (this.has(key)) {
        this.$remove(key);
        if (! this.modified) this.modified = true;
    }
};

/**
 * <strong>Deprecated</strong>. There is a name conflict with the 
 * <code>remove()</code> method for removing a node in <code>WayMixin</code>. 
 * Use <code>removeTag()</code> instead.
 *
 * Removes a tag.
 *
 * @example
 * var node = .... // create a node
 *
 * // set the tags using a javascript object
 * node.tags = {amenity:"restaurant", name:"Obstberg"};
 * node.remove("amenity");
 * node.has("amenity"); // -> false
 *
 * @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
 * @param {string} name the tag name. Must not be null or undefined.
 *   Non-string values are converted to a string. Leading and trailing
 *   whitespace is removed.
 * @name remove
 * @function
 * @summary Removes a tag.
 */
exports.mixin.remove = exports.mixin.removeTag;

/**
* Replies true if this object is a node
*
* @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
* @name isNode
* @readOnly
* @property {boolean} isNode true if this object is a node
* @summary Replies true if this object is a node
*/
exports.mixin.isNode = {
    get: function() {
        return this.getType() == OsmPrimitiveType.NODE;
    }
};

/**
* Replies true if this object is a way
*
* @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
* @name isWay
* @readOnly
* @property {boolean} isWay true if this object is a way
* @summary Replies true if this object is a way
*/
exports.mixin.isWay = {
    get: function() {
        return this.getType() == OsmPrimitiveType.WAY;
    }
};

/**
* Replies true if this object is a relation
*
* @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
* @name isRelation
* @readOnly
* @property {boolean} isRelation true if this object is a relation
* @summary Replies true if this object is a relation
*/
exports.mixin.isRelation = {
    get: function() {
        return this.getType() == OsmPrimitiveType.RELATION;
    }
};

/**
* Set or get wheter this primitive is deleted.
*
* In order to invoke the native method <code>isDeleted()</code>,
* prefix the name with $, i.e.
* <pre>
*    node.$isDeleted(); // -> same as node.isDeleted  without parantheses
* </pre>
* 
*
* @memberOf module:josm/mixin/OsmPrimitiveMixin~OsmPrimitiveMixin
* @name isDeleted
* @alias deleted
* @property {boolean} isDeleted  true, if this primitive is deleted
* @summary Set or get wheter this primitive is deleted.
*/
exports.mixin.isDeleted = exports.mixin.deleted = {
    get: function() {
        return this.$isDeleted();
    },
    set: function(value) {
        util.assert(util.isDef(value), "value: mandatory value is missing");
        util.assert(typeof value === "boolean",
            "value: expected a boolean, got {0}", value);
        this.$setDeleted(value);
    }
};


