/**
 * This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.data.coor.LatLon}.
 * 
 * @module josm/mixin/LatLonMixin
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

/**
 * This mixin provides additional properties and methods which you can 
 * invoke on an instance of
 * {@class org.openstreetmap.josm.data.coor.LatLon}.
 *  
 * @mixin LatLonMixin
 * @forClass org.openstreetmap.josm.data.coor.LatLon
 *
 */
exports.mixin = {};
exports.forClass = org.openstreetmap.josm.data.coor.LatLon;

/**
 * Get the latitude.
 * 
 * @example
 * var LatLon = org.openstreetmap.josm.data.coor.LatLon;
 * 
 * // set members 
 * var pos = new LatLon(1,2);
 * pos.lat;     // -> 1
 * 
 * // you can still invoke the native java method lat(), just add $ as prefix.
 * // Note the parentheses - the native method has to be called as function!
 * pos.$lat();  // -> 1
 * 
 * @memberOf module:josm/mixin/LatLonMixin~LatLonMixin
 * @name lat
 * @property {number} lat  latitude
 * @readOnly
 * @summary Get the latitude.
 * @instance
 */
exports.mixin.lat = {
    get: function() {
        return this.$lat();
    }    
};

/**
 * Get the longitude.
 * 
 * @example
 * var LatLon = org.openstreetmap.josm.data.coor.LatLon;
 * 
 * // set members 
 * var pos = new LatLon(1,2);
 * pos.lon;     // -> 2
 * 
 * // you can still invoke the native java method lon(), just add $ as prefix.
 * // Note the parentheses - the native method has to be called as function!
 * pos.$lon();  // -> 1
 * 
 * @memberOf module:josm/mixin/LatLonMixin~LatLonMixin
 * @name lon
 * @property {number} lon  longitude
 * @readOnly
 * @summary Get the longitude.
 * @instance
 */
exports.mixin.lon = {
    get: function() {
        return this.$lon();
    }    
};


/**
 * Creates a {@class org.openstreetmap.josm.data.coor.LatLon} from a 
 * javascript object.
 * 
 * @example
 * var pos = LatLon.make({lat: 1, lon: 2});
 * 
 * @param {object} obj  a javascript object with two number properties 
 * <code>lat:</code> and 
 *   <code>lon:</code> 
 * @memberOf module:josm/mixin/LatLonMixin~LatLonMixin
 * @name make
 * @static
 * @returns {org.openstreetmap.josm.data.coor.LatLon}  
 * @summary Create a {@class org.openstreetmap.josm.data.coor.LatLon} 
 *      from a javascript object.
 * @function
 */
exports.mixin.make = function(obj) {
    util.assert(util.isSomething(obj), "obj: must not be null or undefined");
    util.assert(typeof obj === "object", 
        "obj: expected an object, got {0}", obj);
    util.assert(util.isNumber(obj.lat), 
        "obj.lat: expected a number, got {0}", obj.lat);
    util.assert(util.isNumber(obj.lon), 
        "obj.lon: expected a number, got {0}", obj.lon);    
    util.assert(LatLon.isValidLat(obj.lat), 
        "obj.lat: expected a valid lat in the range [-90,90], got {0}", 
        obj.lat);
    util.assert(LatLon.isValidLon(obj.lon), 
        "obj.lon: expected a valid lon in the range [-180,180], got {0}", 
        obj.lon);
    return new LatLon(obj.lat, obj.lon);
};
exports.mixin.make.static=true;
