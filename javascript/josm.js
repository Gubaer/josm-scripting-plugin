/**
 * <p>This is the global JOSM API object. It represents a running JOSM instance.</p>
 * 
 * @module josm 
 * 
 * @example
 * // print the JOSM version 
 * var josm = require("josm");
 * java.lang.System.out.println("Version: " + josm.version);
 * 
 * @property {string} version  (read-only) the current JOSM version string
 * @property {string} layers (read-only) the layers object. 
 * 
 */

var Version = org.openstreetmap.josm.data.Version;

/**
 * Replies the current JOSM version string.
 * 
 */ 
Object.defineProperty(exports, "version", {
	get: function() {
		return Version.getInstance().getVersionString();
	}
});
 
/**
 * Replies the layers object. 
 * 
 */ 
Object.defineProperty(exports, "layers", {
	get: function() {
		return require("josm/layers");
	}
});
