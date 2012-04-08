goog.provide("josm.util");
goog.require("josm");

/**
 * <p>Provides a set of static utility functions.</p>
 * 
 * @namespace 
 * @name josm.util
 */
josm.util = function(my) {
	
	/**
	 * Checks whether a value is null or undefined.
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name isNothing
	 * @param {Object} value  the value to check
	 * @type {Boolean}
	 * @return false, if <code>value</code> is null or undefined; true, otherwise  
	 */
	my.isNothing = function(value) {
		return value == null || value == undefined;
	};
	
	return my;
}(josm.util || {});