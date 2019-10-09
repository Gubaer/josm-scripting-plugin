/**
 * <p>Provides a collection of namespaces, classes and functions to work with
 * JOSMs menu system.</p>
 *
 * @module josm/ui/menu
 */

/* global Java */

const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
const util = require('josm/util')

/**
 * <p>Represents JOSMs global menu bar.</p>
 *
 * @namespace
 * @name MenuBar
 * @memberOf josm/ui/menu
 */
exports.MenuBar = {}

/**
 * <p>Replies the number of menus in the JOSM menu bar.</p>
 *
 * @example
 * // display the number of menus
 * josm.alert(josm.menu.length)
 *
 * @memberOf MenuBar
 * @name length
 * @field
 * @readOnly
 * @type {number}
 * @summary Replies the number of menus in the JOSM menu bar.
 */
Object.defineProperty(exports.MenuBar, 'length', {
  enumerable: true,
  get: function () {
    if (!MainApplication.getMenu()) return 0
    return MainApplication.getMenu().getMenuCount()
  }
})

/**
 * <p>Replies a menu in the JOSM menu bar.</p>
 *
 * <p><code>key</code> is either a numberic index or one of the following
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
 * @memberOf MenuBar
 * @name get
 * @method
 * @type {javax.swing.JMenu}
 * @summary Replies a menu in the JOSM menu bar.
 * @param {number|string} key  the key denoting the menu.
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
 * <p>Replies an array with the symbolic menu names.</p>
 *
 * @memberOf MenuBar
 * @name menuNames
 * @field
 * @readOnly
 * @type {array}
 * @summary Replies an array with the symbolic menu names.
 */
Object.defineProperty(exports.MenuBar, 'menuNames', {
  enumerable: true,
  get: function () {
    return ['file', 'edit', 'view', 'tools', 'presets',
      'imagery', 'window', 'help']
  }
})

/**
 * <p>JSAction is an action for which a menu item or a toolbar item can be
 * added to  JOSMs menu or JOSMs toolbar respectively.</p>
 *
 * <p>This is just a shortcut for the Java class
 * {@class org.openstreetmap.josm.plugins.scripting.js.JSAction}.</p>
 *
 * <p>The constructor accepts an object with the following optional named
 * parameters.</p>
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
 * @class
 * @name JSAction
 * @memberOf josm/ui/menu
 * @see JSActionMixin
 * @param {object} para  the named parameters (see description)
 */
exports.JSAction = Java.type('org.openstreetmap.josm.plugins.scripting.js.JSAction')
