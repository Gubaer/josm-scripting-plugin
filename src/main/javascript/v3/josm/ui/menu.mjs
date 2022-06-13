/**
 * Provides a collection of namespaces, classes and functions to work with
 * JOSMs menu system.
 *
 * @module josm/ui/menu
 */

/* global Java */

const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
import * as util from 'josm/util'

/**
 * Represents JOSMs global menu bar.
 *
 * @class
 * @name MenuBar
 */
export const MenuBar =  {}

/**
 * Replies the number of menus in the JOSM menu bar.
 *
 * @example
 * import josm from 'josm'
 * // display the number of menus
 * josm.alert(josm.menu.length)
 *
 * @property {number} length the number of menues
 * @name length
 * @summary the number of menus in the JOSM menu bar
 * @memberof module:josm/ui/menu~MenuBar
 * @static
 */
Object.defineProperty(MenuBar, 'length', {
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
 * import josm from 'josm'
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
MenuBar.get = function (key) {
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
Object.defineProperty(MenuBar, 'menuNames', {
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
 *   <dd class="param-desc">The optional name of the action. Default: an auto generated name.</dd>
 *
 *   <dt><code class="signature">tooltip:string</code></dt>
 *   <dd class="param-desc">The optional tooltip of the action. Default: empty string.</dd>
 *
 *   <dt><code class="signature">iconName:string</code></dt>
 *   <dd class="param-desc">The optional name of an icon. Default: null.</dd>
 *
 *   <dt><code class="signature">toolbarId:string</code></dt>
 *   <dd class="param-desc">The optional name of the tooblar to which this action is added.
 *   Note that it isn't added automatically, when this action is created. Default: null.</dd>
 *
 *   <dt><code class="signature">onExecute:function</code></dt>
 *   <dd class="param-desc">The (optional) function which is called when the action is executed.
 *   Default: null.</dd>
 *
 *   <dt><code class="signature">onInitEnabled:function</code></dt>
 *   <dd class="param-desc">The (optional) function which is called when the <em>enabled</em>
 *   state of the function is evaluated the first time. Default: null.</dd>
 *
 *   <dt><code class="signature">onUpdateEnabled:function</code></dt>
 *   <dd class="param-desc">The (optional) function which is called when the <em>enabled</em>
 *   state of the function is reevaluated, in particular, when layer change
 *   events or selection change events occur. Default: null.</dd>
 * </dl>
 *
 * @example
 * import {JSAction} from 'josm/ui/menu'
 * import * as util from 'josm/util'
 * import josm from 'josm'
 * const JMenuItem = Java.type('javax.swing.JMenuItem')
 *
 * // create the menu action
 * const helloWorldAction = new JSAction({
 *   name: "My Action",
 *   iconName: 'myicon',
 *   toolbarId: 'myToolbarId',
 *   tooltip: "This is my action",
 *
 *   onInitEnabled: function() {
 *     util.println('onInitEnabled: entering ...')
 *   },
 *
 *   onUpdateEnabled: function() {
 *     util.println('onUpdateEnabled: entering ...')
 *   },
 *
 *   onExecute: function() {
 *     util.println('Hello World!')
 *   }
 * })
 *
 * // register a new menu item in the file menu
 * const fileMenu = josm.menu.get('file')
 * fileMenu.addSeparator()
 * fileMenu.add(new JMenuItem(helloWorldAction))
 *
 *
 * @property {org.openstreetmap.josm.plugins.scripting.graalvm.JSAction} JSAction
 * @static
 * @name JSAction
 * @memberof module:josm/ui/menu~MenuBar
 */
export const JSAction = Plugin.type('org.openstreetmap.josm.plugins.scripting.graalvm.JSAction')
