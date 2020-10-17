/**
 * Provides a collection of namespaces, classes and functions to work with
 * JOSMs menu system.
 *
 * @module josm/ui/menu
 */

/* global Java */
// org.openstreetmap.josm.plugins.scripting.graalvm.JSAction has to be
// injected in the scripting context
/* global JSAction */

const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
// const JSAction = Java.type('org.openstreetmap.josm.plugins.scripting.graalvm.JSAction')
const util = require('josm/util')

/**
 * Represents JOSMs global menu bar.
 *
 * @class
 * @name MenuBar
 */
exports.MenuBar = function () {}

/**
 * Replies the number of menus in the JOSM menu bar.
 *
 * @example
 * // display the number of menus
 * josm.alert(josm.menu.length)
 *
 * @property {number} length the number of menues
 * @name length
 * @summary the number of menus in the JOSM menu bar
 * @memberof module:josm/ui/menu~MenuBar
 * @static
 */
Object.defineProperty(exports.MenuBar, 'length', {
  enumerable: true,
  get: function () {
    if (!MainApplication.getMenu()) return 0
    return MainApplication.getMenu().getMenuCount()
  }
})

/**
 * Replies a menu in the JOSM menu bar.
 *
 * <code>key</code> is either a numberic index or one of the following
 * symbolic names as string:
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
 * const editmenu = josm.menu.get(1)
 *
 * // get the file menu with a symbolic name
 * const filemenu = josm.menu.get('file')
 *
 * @return {javax.swing.JMenu} the menu
 * @summary Replies a menu in the JOSM menu bar.
 * @param {number|string} key  the key denoting the menu
 * @memberof module:josm/ui/menu~MenuBar
 * @name get
 * @function
 * @instance
 */
exports.MenuBar.get = function (key) {
  util.assert(util.isSomething(key), 'key: must not be null or undefined')
  const mainMenu = MainApplication.getMenu()
  if (util.isNumber(key)) {
    util.assert(key >= 0 && key < exports.MenuBar.length,
      'key: index out of range, got {0}', key)
    return mainMenu.getMenu(key)
  } else if (util.isString(key)) {
    key = util.trim(key).toLowerCase()
    switch (key) {
      case 'file': return mainMenu.fileMenu
      case 'edit': return mainMenu.editMenu
      case 'view': return mainMenu.viewMenu
      case 'tools': return mainMenu.toolsMenu
      case 'presets': return mainMenu.presetsMenu
      case 'imagery': return mainMenu.imageryMenu
      case 'window': return mainMenu.windowMenu
      case 'help': return mainMenu.helpMenu
      default:
        util.assert(false,
          'Unsupported key to access a menu, got {0}', key)
    }
  } else {
    util.assert(false, 'Unexpected value, got {0}', key)
  }
}

/**
 * Replies an array with the symbolic menu names.
 *
 * @memberof module:josm/ui/menu~MenuBar
 * @name menuNames
 * @property {array} menuNames the names of the menues in the menu bar
 * @readOnly
 * @summary Replies an array with the symbolic menu names.
 * 
 */
Object.defineProperty(exports.MenuBar, 'menuNames', {
  enumerable: true,
  get: function () {
    return ['file', 'edit', 'view', 'tools', 'presets',
      'imagery', 'window', 'help']
  }
})

/**
 * JSAction is an action for which a menu item or a toolbar item can be
 * added to  JOSMs menu or JOSMs toolbar respectively.
 *
 * This is just a shortcut for the Java class
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.
 *
 * The constructor accepts an object with the following optional named
 * parameters.
 * <dl>
 *   <dt><code class="signature">name:string</code></dt>
 *   <dd>The optional name of the action. Default: an auto generated named.</dd>
 *
 *   <dt><code class="signature">tooltip:string</code></dt>
 *   <dd>The optional tooltip of the action. Default: empty string.</dd>
 *
 *   <dt><code class="signature">iconName:string</code></dt>
 *   <dd>The optional name of an icon. Default: null.</dd>
 *
 *   <dt><code class="signature">toolbarId:string</code></dt>
 *   <dd>The optional name of the tooblar, this action is going to be added to
 *   later. Note, that it isn't added automatically, when this action is
 *   created. Default: null.</dd>
 *
 *   <dt><code class="signature">onExecute:function</code></dt>
 *   <dd>The (optional) function which is called when the action is executed.
 *   Default: null.</dd>
 *
 *   <dt><code class="signature">onInitEnabled:function</code></dt>
 *   <dd>The (optional) function which is called when the <em>enabled</em>
 *   state of the function is evaluated the first time. Default: null.</dd>
 *
 *   <dt><code class="signature">onUpdateEnabled:function</code></dt>
 *   <dd>The (optional) function which is called when the <em>enabled</em>
 *   state of the function is reevaluated, in particular, when layer change
 *   events or selection change events occur. Default: null.</dd>
 * </dl>
 *
 * @example
 * const JSAction = require("josm/ui/menu").JSAction;
 * const action = new JSAction({
 *    name: "My Action",
 *    tooltip: "This is my action",
 *    onInitEnabled: function() {
 *       this.enabled = false
 *    }
 * })
 *
 * action.onExecute = function() {
 *    josm.alert("Action is executing ...")
 * }
 *
 * @property {org.openstreetmap.josm.plugins.scripting.js.JSAction} JSAction
 * @static
 * @name JSAction
 * @memberof module:josm/ui/menu~MenuBar
 */
exports.JSAction = JSAction
