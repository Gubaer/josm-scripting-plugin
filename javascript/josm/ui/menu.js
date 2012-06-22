(function() {
	
/**
 * <p>Provides a collection of namespaces, classes and functions to work with JOSMs menu
 * system.</p>
 * 
 * @module josm/ui/menu
 */	

/**
 * <p>Represents JOSMs global menu bar.</p>
 * 
 * @namespace 
 * @name MenuBar
 * @memberOf josm/ui/menu
 */	
exports.MenuBar = {};

/**
 * <p>Replies the number of menus in the JOSM menu bar.</p>
 * 
 * @example
 * // display the number of menus 
 * josm.alert(josm.menu.length);
 * 
 * @memberOf MenuBar
 * @name length
 * @field
 * @readOnly
 * @type {number} 
 * @summary Replies the number of menus in the JOSM menu bar.
 */
Object.defineProperty(exports.MenuBar, "length", {
	enumerable: true,
	get: function() {
		var Main = org.openstreetmap.josm.Main;
		if (!Main.main || !Main.main.menu) return 0;
		return Main.main.menu.getMenuCount();
	} 
});

/**
 * <p>Replies a menu in the JOSM menu bar.</p>
 * 
 * <p><code>key</code> is either a numberic index or one of the following symbolic names
 * as string:
 * <ul>
 *   <li><code>file</code></li>
 *   <li><code>edit</code></li>
 *   <li><code>view</code></li>
 *   <li><code>tools</code></li>
 *   <li><code>presets</code></li>
 *   <li><code>imagery</code></li>
 *   <li><code>window</code></li>
 *   <li><code>help</code></li>
 * </ul>
 * 
 * @example
 * // get the edit menu with a numeric index
 * var editmenu = josm.menu.get(1);  
 * 
 * // get the file menu with a symbolic name 
 * var filemenu = josm.menu.get("file");
 * 
 * @memberOf MenuBar
 * @name get
 * @method
 * @type {javax.swing.JMenu} 
 * @summary Replies a menu in the JOSM menu bar.
 * @param {number|string} key  the key denoting the menu.
 */
exports.MenuBar.get = function(key) {
	var Main = org.openstreetmap.josm.Main;
	util.assert(util.isSomething(key), "key: must not be null or undefined");
	if (util.isNumber(key)) {
		util.assert(key >= 0 && key < exports.MenuBar.length, "key: index out of range, got {0}", key);
		return Main.main.menu.getMenu(key);
	} else if (util.isString(key)) {
		key = util.trim(key).toLowerCase();
		switch(key) {
		case "file": return Main.main.menu.fileMenu;
		case "edit": return Main.main.menu.editMenu;
		case "view": return Main.main.menu.viewMenu;
		case "tools": return Main.main.menu.toolsMenu;
		case "presets": return Main.main.menu.presetsMenu;
		case "imagery": return Main.main.menu.imageryMenu;
		case "window": return Main.main.menu.windowMenu;
		case "help": return Main.main.menu.helpMenu;
		default:
			util.assert(false, "Unsupported key to access a menu, got {0}", key);
		}
	} else {
		util.assert(false, "Unexpected value, got {0}", key);
	}
};

/**
 * <p>Replies an array with the symbolic menu names.</p>
 * 
 * @memberOf MenuBar
 * @name menuNames
 * @field
 * @readOnly
 * @type {array} 
 * @summary Replies an array with the symbolic menu names.
 */
Object.defineProperty(exports.MenuBar, "menuNames", {
	enumerable: true,	
	get: function() {
		return ["file", "edit", "view", "tools", "presets", "imagery", "window", "help"];
	} 
});
	
}());
