/**
 * <p>Additional properties and functions for JOSMs internal class
 *  {@link http://josm.openstreetmap.de/browser/josm/trunk/src/org/openstreetmap/josm/data/coor/LatLon.java|LatLon}.</p> 
 * 
 * @module josm/mixin/NodeMixin
 *     
 */

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

exports.forClass = org.openstreetmap.josm.data.coor.LatLon;
exports.mixin    = mixin;