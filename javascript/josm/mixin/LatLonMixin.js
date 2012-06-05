/**
 * <p>Additional properties and functions for JOSMs internal class
 *  {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/coor/LatLon.java|LatLon}.</p> 
 * 
 */
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

/**
 * <p>LatLonMixin provides additional properties and methods which you can invoke on an instance of
 * {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/coor/LatLon.java|LatLon}. 
 * The native methods of JOSM Node are still 
 * available for scripting. If they are masked by methods and properties defined in this wrapper, you can
 * access them by adding the prefix charater $.</p>
 * 
 * @mixin LatLonMixin 
 *
 */
var mixin = {};

/**
 * <p>Get the latitude.</p>
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
 * @memberOf LatLonMixin
 * @name lat
 * @field
 * @readOnly
 * @instance
 * @type {number} 
 */
mixin.lat = {
	get: function() {
		return this.$lat();
	}	
};

/**
 * <p>Get the longitude.</p>
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
 * @memberOf LatLonMixin
 * @name lon
 * @field
 * @readOnly
 * @instance
 * @type {number} 
 */
mixin.lon = {
	get: function() {
		return this.$lon();
	}	
};


/**
 * <p>Creates a LatLon from a javascript object.</p>
 * 
 * @example
 * var pos = LatLon.make({lat: 1, lon: 2});
 * 
 * @param {object} obj  a javascript object with two number properties <code>lat:</code> and 
 *   <code>lon:</code> 
 * * @memberOf LatLonMixin
 * @name make
 * @static
 * @method
 * @type org.openstreetmap.josm.data.coor.LatLon 
 */
mixin.make = function(obj) {
	util.assert(util.isSomething(obj), "obj: must not be null or undefined");
	util.assert(typeof obj === "object", "obj: expected an object, got {0}", obj);
	util.assert(util.isNumber(obj.lat), "obj.lat: expected a number, got {0}", obj.lat);
	util.assert(util.isNumber(obj.lon), "obj.lon: expected a number, got {0}", obj.lon);	
	return new LatLon(obj.lat, obj.lon);
};
mixin.make.static=true;

exports.forClass = org.openstreetmap.josm.data.coor.LatLon;
exports.mixin    = mixin;