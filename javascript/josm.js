goog.provide("josm");

/**
 * <p>This is the global JOSM API object. It represents a running JOSM instance.</p>
 * @namespace  
 * @name josm 
 */
var josm = function(my) {
	var Version = org.openstreetmap.josm.data.Version;

	/**
	 * @name version
	 * @description Replies the current JOSM version string.
	 * @memberOf josm
	 * @type {String}
	 */ 
	Object.defineProperty(my, "version", {
		get: function() {
			return Version.getInstance().getVersionString();
		}
	});
		
	
	return my;	
}(josm || {});
