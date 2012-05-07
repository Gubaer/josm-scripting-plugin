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
 */

var Version = org.openstreetmap.josm.data.Version;

/**
 * Replies the current JOSM version string.
 * 
 * @type {string}
 * @readOnly
 */ 
exports.version = Version.getInstance().getVersionString();

