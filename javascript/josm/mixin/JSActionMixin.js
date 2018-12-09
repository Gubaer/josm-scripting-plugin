(function() {

/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the
 * native java class
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.</p>
 *
 * @module josm/mixin/JSActionMixin
 */
var util = require("josm/util");

var Preferences = org.openstreetmap.josm.data.Preferences;

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the
 * native java class
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}. It
 * provides additional properties and methods which you can invoke on an
 * instance of
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.</p>
 *
 * @mixin JSActionMixin
 * @forClass org.openstreetmap.josm.plugins.scripting.js.JSAction
 * @memberOf josm/mixin/JSActionMixin
 */
var mixin = {};

/**
 * <p>Set or get the function to be called when the <em>enabled</em> state
 * should be initialized.</p>
 *
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the function or undefined, if no function has been assigned.
 *   </dd>
 *   <dt>set</dt>
 *   <dd>Set the function, or null or undefined to remove the function.</dd>
 * </dl>
 *
 * @memberOf JSActionMixin
 * @name onInitEnabled
 * @field
 * @instance
 * @type {function}
 * @summary Set or get the function to be called when the <em>enabled</em>
 * state of the action should be reevaluated.
 */
mixin.onInitEnabled = {
    get: function() {
        var value = this.$getOnInitEnabled();
        return value == null ? undefined: value;
    },
    set: function(fun) {
        if (util.isSomething(fun)) {
            util.assert(util.isFunction(fun),
                "fun: expected a function, got {0}", fun);
        } else {
            fun = null;
        }
        this.$setOnInitEnabled(fun);
    }
};

/**
 * <p>Set or get the function to be called when the <em>enabled</em> state of
 * the action should be reevaluated.</p>
 *
 * <dl>
 *   <dt>get</dt>
 *   <dd>Replies the function or undefined, if no function has been assigned.
 *   </dd>
 *   <dt>set</dt>
 *   <dd>Set the function, or null or undefined to remove the function.</dd>
 * </dl>
 *
 * @memberOf JSActionMixin
 * @name onUpdateEnabled
 * @field
 * @instance
 * @type {function}
 * @summary Set or get the function to be called when the <em>enabled</em>
 * state of the action should be reevaluated.
 */
mixin.onUpdateEnabled = {
    enumerable: true,
    get: function() {
        var value =  this.$getOnUpdateEnabled();
        return value == null ? undefined : value;
    },
    set: function(fun) {
        if (util.isSomething(fun)) {
            util.assert(util.isFunction(fun),
                "fun: expected a function, got {0}", fun);
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
 *   <dd>Replies the function or undefined, if no function has been assigned.
 *   </dd>
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
            util.assert(util.isFunction(fun),
                "fun: expected a function, got {0}", fun);
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
 *   <dd>Set null or undefined, to clear the name. Any other value is converted
 *   to a string.</dd>
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
 *   <dd>Replies the tooltip as string, or undefined, if no tooltip is set.
 *   </dd>
 *   <dt>set</dt>
 *   <dd>Set null or undefined, to clear the tooltip. Any other value is
 *   converted to a string.</dd>
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
 *    .addToMenu(josm.menu.get("edit"));
 *
 * @method
 * @name addToMenu
 * @summary Adds an action to a menu.
 * @memberOf JSActionMixin
 * @instance
 * @param {java.swing.JMenu} menu  the menu. This should be one of the global
 *     JOSM menus.
 * @param {number} index (optional) the index where to add the menu.
 *     Default if missing: adds the menu at the end
 */
mixin.addToMenu = function(menu, index) {
    var MainMenu = org.openstreetmap.josm.gui.MainMenu;

    util.assert(util.isSomething(menu), "menu: must not be null or undefined");
    util.assert(menu instanceof javax.swing.JMenu,
        "menu: expected a JMenu, got {0}", menu);
    if (util.isDef(index)) {
        util.assert(util.isNumber(index),
            "index: expected a number, got {0}", index);
        util.assert(index >= 0,
            "index: expected a number >= 0, got {0}", index);
    }
    // FIXME support for named options
    //    options = options || {};
    //    util.assert(typeof options === "object", "options: expected an object,
    //     got {0}", options);
    // var inExpertModeOnly = util.isDef(options.inExpertModeOnly)
    //      ? false : Boolean(options.inExpertModeOnly);
    var inExpertModeOnly = false;
    if (util.isDef(index)) {
        MainMenu.add(menu, this, inExpertModeOnly, index);
    } else {
        MainMenu.add(menu, this, inExpertModeOnly);
    }
};

function atFromOptions(options) {
    if (!options || !util.isDef(options.at)) return undefined;
    if (util.isString(options.at)) {
        var at = util.trim(options.at).toLowerCase();
        switch(at) {
        case "start": return 0;
        case "end": return -1;
        default:
            util.assert(false,
                "at: unsupported string value, got ''{0}''", options.at);
        }
    } else if (util.isNumber(options.at)) {
        util.assert(options.at >= 0,
            "at: expected number >= 0, got {0}", options.at);
        return options.at;
    } else {
        util.assert(false, "at: unsupported value, got {0}", options.at);
    }
};

function beforeFromOptions(options) {
    if (!options || !options.before) return undefined;
    if (util.isString(options.before)) return util.trim(options.before);
    util.assert(false, "before: unsupported value, got {0}", options.before);
};

function afterFromOptions(options) {
    if (!options || !options.after) return undefined;
    if (util.isString(options.after)) return util.trim(options.after);
    util.assert(false, "after: unsupported value, got {0}", options.after);
};

/**
 * <p>Adds an action to the toolbar.</p>
 *
 * <p>If no parameters are passed in, the action is registered with its
 * toolbar id (see property tooblarId) for being displayed in the toolbar.
 * It isn't displayed, however, unless the toolbar id is already included
 * in the toolbar preferences. If not, the user first have to configure it
 * manually in the preference dialog.</p>
 *
 * <p>Use one of the named options described below, if you want to enforce,
 * that the action is displayed at a a specific position in the toolbar.
 * If you use one of these options, the toolbar configuration is saved
 * to the preferences.</p>
 *
 * <code>options</code> supports the following named parameters:
 * <dl>
 *   <dt><code class="signature">toolbarId: string</code> </dt>
 *   <dd>Optional toolbar id, overriding an already assigned toolbar id.</dd>
 *
 *   <dt><code class="signature">at: number, "start", "end"</code> </dt>
 *   <dd>Display the toolbar entry at a specific position, at the start or at
 *   the end of the toolbar.</dd>
 *
 *   <dt><code class="signature">after: string</code> </dt>
 *   <dd>Display the toolbar entry after the entry with toolbar
 *   id <code>after</code>.</dd>
 *
 *   <dt><code class="signature">before: string</code> </dt>
 *   <dd>Display the toolbar entry before the entry with toolbar
 *   id <code>before</code>.</dd>
 * </dl>
 *
 * @example
 * var JSAction = require("josm/ui/menu").JSAction;
 * var action = new JSAction({name: "My Action", toolbarId: "myaction"});
 *
 * // add the action after the "open" action in the toolbar
 * action.addToToolbar({after: "open"});
 *
 * // add the action at the end of the toolbar
 * action.addToToolbar({at: "end"});
 *
 * @method
 * @name addToToolbar
 * @summary Adds an action to the toolbar.
 * @memberOf JSActionMixin
 * @instance
 * @param {objects} options  optional named parameters
 */
mixin.addToToolbar = function(options) {
    var Preferences = org.openstreetmap.josm.data.Preferences;
    var MainApplication = org.openstreetmap.josm.gui.MainApplication;
    var ArrayList = java.util.ArrayList;
    options = options || {};
    util.assert(typeof options === "object",
        "options: expected an object, got {0}", options);
    if (util.isSomething(options.toolbarId)) {
        this.putValue("toolbarId", String(options.toolbarId));
    }

    MainApplication.getToolbar().register(this);
    var toolbarId = this.getValue("toolbarId");
    var toolbarPrefs = new ArrayList(
        Preferences.main().getList("toolbar",new ArrayList()));

    // The following is clumsy. We have to fiddle with preference settings
    // in order to display a toolbar entry at a specific position
    //
    var at = atFromOptions(options);
    var before = beforeFromOptions(options);
    var after = afterFromOptions(options);
    // if we got the parameter 'at', we add the toolbar entry at this position
    //
    if (util.isDef(at)) {
        if (at == -1) {
            toolbarPrefs.remove(toolbarId);
            toolbarPrefs.add(toolbarId);
        } else {
            toolbarPrefs.remove(toolbarId);
            toolbarPrefs.add(Math.min(toolbarPrefs.size(), at), toolbarId);
        }       
        Preferences.main().putList("toolbar", toolbarPrefs);
    } else if (util.isDef(after)) {
        // if we got the parameter 'after', we try to insert it after the
        // entry given as value for 'after'
        //
        var at = toolbarPrefs.indexOf(after);
        if (at != -1) {
            toolbarPrefs.remove(toolbarId);
            toolbarPrefs.add(at + 1, toolbarId);
        }
        Preferences.main().putList("toolbar", toolbarPrefs);
    } else if (util.isDef(before)) {
        // if we got the parameter 'before', we try to insert it before the
        // entry given as value for 'before'
        //
        var at = toolbarPrefs.indexOf(before);
        if (at != -1) {
            toolbarPrefs.remove(toolbarId);
            toolbarPrefs.add(at, toolbarId);
        }
        Preferences.main().putList("toolbar", toolbarPrefs);
    }
    MainApplication.getToolbar().refreshToolbarControl();
};

exports.forClass = org.openstreetmap.josm.plugins.scripting.js.JSAction;
exports.mixin = mixin;

}());