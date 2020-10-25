/**
 * This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.data.osm.Changeset}.
 *
 * @module josm/mixin/ChangesetMixin
 */

var util = require("josm/util");
var Tags = org.openstreetmap.josm.plugins.scripting.js.api.Tags;

/**
 * This mixin is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.data.osm.Changeset}.
 *
 * @mixin ChangesetMixin
 * @summary JavaScript mixin for the java class {@class org.openstreetmap.josm.data.osm.Changeset}
 * @forClass org.openstreetmap.josm.data.osm.Changeset
 */
exports.forClass=org.openstreetmap.josm.data.osm.Changeset;
exports.mixin = {};

/**
 * Replies the unique numeric id.
 *
 * @readonly
 * @property {number} id
 * @name id
 * @summary Replies the unique numeric id.
 * @instance
 * @memberof module:josm/mixin/ChangesetMixin~ChangesetMixin
 */
exports.mixin.id = {
    get: function() {
        return this.getId();
    }
};

/**
 * Get or set whether this changeset is open.
 *
 * @property {boolean} isOpen
 * @name isOpen
 * @instance
 * @summary Get or set whether this changeset is open.
 * @memberof module:josm/mixin/ChangesetMixin~ChangesetMixin
 * @alias open
 */
exports.mixin.isOpen = exports.mixin.open = {
    get: function() {
        return this.$isOpen();
    },
    set: function(value) {
        util.assert(typeof value === "boolean",
            "value: expected a boolean, got {0}",
            value);
        this.$setOpen(value);
    }
};

var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Bounds = org.openstreetmap.josm.data.Bounds;

/**
 * Set or get the coordinates of the lower left corner of the bounding box.
 * 
 *
 * <dl>
 *   <dt>get:</dt>
 *   <dd>Replies a {@class org.openstreetmap.josm.data.coor.LatLon} or
 *   undefined, if min isn't known.</dd>
 *
 *   <dt>set:</dt>
 *   <dd>Assign null or undefined to clear the coordinates. Assign a
 *   {@class org.openstreetmap.josm.data.coor.LatLon} or
 *   an object <code>{lat:number, lon: number}</code> to set the coordinates.
 *   </dd>
 * </dl>
 
 * @property {org.openstreetmap.josm.data.coor.LatLon} min the lower left corner
 * @name min
 * @summary Get or set the coordinates of the lower left corner
 * @instance
 * @memberof module:josm/mixin/ChangesetMixin~ChangesetMixin
 */
exports.mixin.min = {
    get: function() {
        var min = this.$getMin();
        return min == null ? undefined : min;
    },
    set: function(value) {
        if (util.isNothing(value)) {
            this.$setMin(null);
            return;
        }
        util.assert(value instanceof LatLon || typeof value ===
            "object", "value: expected a LatLon or an object, got {0}", value);
        if (value instanceof LatLon) {
            this.$setMin(value);
        } else if (typeof value === "object") {
            value = LatLon.make(value);
            this.$setMin(value);
        }
    }
};

/**
 * Set or get the coordinates of the upper right corner of the bounding box.
 * 
 * <dl>
 *   <dt>get:</dt>
 *   <dd>Replies a {@class org.openstreetmap.josm.data.coor.LatLon} or
 *   undefined, if max isn't known.</dd>
 *
 *   <dt>set:</dt>
 *   <dd>Assign null or undefined to clear the coordinates. Assign a
 *   {@class org.openstreetmap.josm.data.coor.LatLon} or
 *   an object <code>{lat:number, lon: number}</code> to set the coordinates.
 *   </dd>
 * </dl>

 * @name max
 * @property {org.openstreetmap.josm.data.coor.LatLon} max the upper right corner
 * @summary Get or set the coordinates of the upper right corner
 * @instance
 * @memberOf module:josm/mixin/ChangesetMixin~ChangesetMixin
 */
exports.mixin.max = {
    get: function() {
        var max = this.$getMax();
        return max == null ? undefined : max;
    },
    set: function(value) {
        if (util.isNothing(value)) {
            this.$setMax(null);
            return;
        }
        util.assert(value instanceof LatLon || typeof value === "object",
            "value: expected a LatLon or an object, got {0}", value);
        if (value instanceof LatLon) {
            this.$setMax(value);
        } else if (typeof value === "object") {
            value = LatLon.make(value);
            this.$setMax(value);
        }
    }
};

/**
 * Get the bounding box the bounding box.
 * <dl>
 *   <dt>get:</dt>
 *   <dd>Replies a {@class org.openstreetmap.josm.data.Bounds} or undefined,
 *   if the bounding box isn't known.</dd>
 * </dl>
 *
 *
 * @name bounds
 * @property {org.openstreetmap.josm.data.Bounds} bounds the bounding box
 * @summary Get the bounding box the bounding box.
 * @memberof module:josm/mixin/ChangesetMixin~ChangesetMixin
 * @instance
 * @readOnly
 */
exports.mixin.bounds = {
    get: function() {
        var bounds = this.$getBounds();
        return bounds == null ? undefined : bounds;
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
 * Get or set the tags of the changeset.
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
 * var cs = .... // create a changeset
 *
 * // set the tags using a javascript object
 * cs.tags = {amenity:"restaurant", name:"Obstberg"};
 * cs.tags.amenity;  // -> restaurant
 * cs.tags.name;     // -> Obstberg
 *
 * // remove all tags
 * cs.tags = null;
 *
 * // set tags using a java map
 * var tags = new java.util.HashMap();
 * tags.put("amenity", "restaurant");
 * tags.put("name", "Obstberg");
 * cs.tags = tags;
 *
 * @memberOf module:josm/mixin/ChangesetMixin~ChangesetMixin
 * @name tags
 * @property {object} tags  the tags
 * @summary Get or set the tags.
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
        } else if (tags instanceof java.util.Map) {
            applyTagMap(this, tags);
        } else if (typeof tags === "object") {
            applyTagObject(this, tags);
        } else {
            util.assert(false, "Can''t assign tags from object {0}", tags);
        }
    }
};

/**
 * Set or get the date this changeset was created.
 * <dl>
 *   <dt>get:</dt>
 *   <dd>Replies a {@class java.utilDate} or undefined, if the date isn't
 *   known.</dd>
 *
 *   <dt>set:</dt>
 *   <dd>Assign null or undefined to clear the date. Assign a
 *   {@class java.util.Date} to set the date. Note, that you can't
 *   assign a javascript Date object.</dd>
 * </dl>
 *
 * @memberOf module:josm/mixin/ChangesetMixin~ChangesetMixin
 * @name createdAt
 * @property {java.util.Date} createdAt  date the changeset was created
 * @summary Set or get the date this changeset was created.
 * @instance
 */
exports.mixin.createdAt = {
    get: function() {
        var d = this.getCreatedAt();
        return d == null ? undefined: d;
    },
    set: function(value) {
        if (util.isNothing(value)) {
            this.setCreatedAt(null);
        } else if (value instanceof Date) {
            util.assert(false,
                "Can''t assign a date from a javascript Date. "
            + "Use a java.util.Date object instead.");
        } else if (value instanceof java.util.Date) {
            this.setCreatedAt(value);
        } else {
            util.assert(false, "Unexpected type of value, got {0}", value);
        }
    }
};

/**
 * Set or get the date this changeset was closed.
 *
 * <dl>
 *   <dt>get:</dt>
 *   <dd>Replies a {@class java.utilDate} or undefined, if the date isn't
 *   known.</dd>
 *
 *   <dt>set:</dt>
 *   <dd>Assign null or undefined to clear the date. Assign a
 *   {@class java.util.Date} to set the date. Note, that you can't assign a
 *   javascript Date object.</dd>
 * </dl>
 *
 * @memberOf module:josm/mixin/ChangesetMixin~ChangesetMixin
 * @name closedAt
 * @property {java.util.Date} closedAt date the changeset was closed
 * @summary Set or get the date this changeset was closed.
 * @instance
 */
exports.mixin.closedAt = {
    get: function() {
        var d = this.getClosedAt();
        return d == null ? undefined: d;
    },
    set: function(value) {
        if (util.isNothing(value)) {
            this.setClosedAt(null);
        } else if (value instanceof Date) {
            util.assert(false,
                "Can''t assign a date from a javascript Date. "
            + "Use a java.util.Date object instead.");
        } else if (value instanceof java.util.Date) {
            this.setClosedAt(value);
        } else {
            util.assert(false, "Unexpected type of value, got {0}", value);
        }
    }
};
