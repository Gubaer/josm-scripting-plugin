(function() {
	
/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.</p>
 * 
 * @module josm/mixin/JSActionMixin 
 */
var util = require("josm/util");

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}. It
 * provides additional properties and methods which you can invoke on an instance of
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.</p>
 *   
 * @mixin JSActionMixin
 * @forClass org.openstreetmap.josm.plugins.scripting.js.JSAction
 * @memberOf josm/mixin/JSActionMixin 
 */
var mixin = {};

/**
 * <p>Set or get the function to be called when the <em>enabled</em> state should
 * be initialized.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the function or undefined, if no function has been assigned. </dd>
 *   <dt>set</dt>
 *   <dd>Set the function, or null or undefined to remove the function.</dd>
 * </dl>
 * 
 * @memberOf JSActionMixin
 * @name onInitEnabled
 * @field
 * @instance
 * @type {function} 
 * @summary Set or get the function to be called when the <em>enabled</em> state of the
 * action should be reevaluated.
 */
mixin.onInitEnabled = {
	get: function() {
		var value = this.$getOnInitEnabled();
		return value == null ? undefined: value;
	}, 
	set: function(fun) {
		if (util.isSomething(fun)) {
			util.assert(util.isFunction(fun), "fun: expected a function, got {0}", fun);
		} else {
			fun = null;
		}
		this.$setOnInitEnabled(fun);
	}
};

/**
 * <p>Set or get the function to be called when the <em>enabled</em> state of the
 * action should be reevaluated.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the function or undefined, if no function has been assigned. </dd>
 *   <dt>set</dt>
 *   <dd>Set the function, or null or undefined to remove the function.</dd>
 * </dl>
 * 
 * @memberOf JSActionMixin
 * @name onUpdateEnabled
 * @field
 * @instance
 * @type {function} 
 * @summary Set or get the function to be called when the <em>enabled</em> state of the
 * action should be reevaluated.
 */
mixin.onUpdateEnabled = {
	enumerable: true,
	get: function() {
		var value =  this.$getOnUpdateEnabled();
		return value == null ? undefined : value;
	}, 
	set: function(fun) {
		if (util.isSomething(fun)) {
			util.assert(util.isFunction(fun), "fun: expected a function, got {0}", fun);
		} else {
			fun = null;
		}
		this.$setOnUpdateEnabled(fun);
	}
};

/**
 * <p>Set or get the function to be called when the action is executed.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the function or undefined, if no function has been assigned. </dd>
 *   <dt>set</dt>
 *   <dd>Set the function, or null or undefined to remove the function.</dd>
 * </dl>
 * 
 * @memberOf JSActionMixin
 * @name onExecute
 * @field
 * @instance
 * @type {function} 
 * @summary Set or get the function to be called when the action is executed.
 */
mixin.onExecute = {
	get: function() {
		var value =  this.$getOnExecute();
		return value == null ? undefined : value;
	}, 
	set: function(fun) {
		if (util.isSomething(fun)) {
			util.assert(util.isFunction(fun), "fun: expected a function, got {0}", fun);
		} else {
			fun = null;
		}
		this.$setOnExecute(fun);
	}
};

/**
 * <p>Set or get the name.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the name as string, or undefined, if no name is set. </dd>
 *   <dt>set</dt>
 *   <dd>Set null or undefined, to clear the name. Any other value is converted to a string.</dd>
 * </dl>
 * 
 * @memberOf JSActionMixin
 * @name name
 * @field
 * @readOnly
 * @instance
 * @type {String} 
 * @summary Set or get the name.
 */
mixin.name = {
	get: function() {
		var Action = javax.swing.Action;
		var value = this.getValue(Action.NAME);
		return value == null ? undefined: value;
	}, 
	set: function(value) {
		var Action = javax.swing.Action;
		value = util.isNothing(value) ? null : String(value);
		this.putValue(Action.NAME, value);
	}
};

/**
 * <p>Set or get the tooltip text.</p>
 * 
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the tooltip as string, or undefined, if no tooltip is set. </dd>
 *   <dt>set</dt>
 *   <dd>Set null or undefined, to clear the tooltip. Any other value is converted to a string.</dd>
 * </dl>
 *
 * @memberOf JSActionMixin
 * @name tooltip
 * @field
 * @readOnly
 * @instance
 * @type {String} 
 * @summary Set or get the tooltip text.
 */
mixin.tooltip = {
	get: function() {
		var Action = javax.swing.Action;
		var value = this.getValue(Action.SHORT_DESCRIPTION);
		return value == null ? undefined: value;
	}, 
	set: function(value) {
		var Action = javax.swing.Action;
		value = util.isNothing(value) ? null : String(value);
		this.putValue(Action.SHORT_DESCRIPTION, value);		
	}
};

/**
 * <p>Adds an action to a menu.</p>
 * 
 * @example 
 * var JSAction = require("josm/ui/menu").JSAction;
 * 
 * // adds a new action to the JOSM edit menu 
 * new JSAction({name: "My Action"})
 *    .addTo(josm.menu.get("edit"));
 *
 * @method
 * @name addTo
 * @signature Adds an action to menu.
 * @memberOf JSMenuMixin
 * @instance
 * @param {java.swing.JMenu} menu  the menu. This should be one of the global JOSM menus.
 * @param {number} index (optional) the index where to add the menu. Default if missing: adds the menu at the end
 */
mixin.addTo = function(menu, index) { 
	var MainMenu = org.openstreetmap.josm.gui.MainMenu;
	 
	util.assert(util.isSomething(menu), "menu: must not be null or undefined");
	util.assert(menu instanceof javax.swing.JMenu, "menu: expected a JMenu, got {0}", menu);
	if (util.isDef(index)) {
		util.assert(util.isNumber(index), "index: expected a number, got {0}", index);
		util.assert(index >= 0, "index: expected a number >= 0, got {0}", index);
	}
	// FIXME support for named options 
	//	options = options || {};
	//	util.assert(typeof options === "object", "options: expected an object, got {0}", options);
	// var inExpertModeOnly = util.isDef(options.inExpertModeOnly) ? false : Boolean(options.inExpertModeOnly);
	var inExpertModeOnly = false;
	if (util.isDef(index)) {
		MainMenu.add(menu, this, inExpertModeOnly, index);
	} else {
		MainMenu.add(menu, this, inExpertModeOnly);
	}
};


exports.forClass = org.openstreetmap.josm.plugins.scripting.js.JSAction;
exports.mixin = mixin;

}());