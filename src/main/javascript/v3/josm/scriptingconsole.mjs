/**
 * Provides a set of functions to write to the built-in scripting
 * console.
 *
 * @module josm/scriptingconsole
 */

/* global Java */

// -- imports
const ScriptingConsole =  Plugin.type('org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole')
const MessageFormat = Java.type('java.text.MessageFormat')

function consoleWriter () {
    return ScriptingConsole.static.getInstance().getScriptLog().getLogWriter()
}

/**
 * Prints a string to the console
 *
 * @example
 * import {print} from 'josm/scriptingconsole'
 * print('Hello world!')
 *
 * // or use formatting
 * print('Hello world! My name is {0}', 'foo')
 *
 * @summary  Prints a string to the console
 * @param {string} message the message
 * @param {...object} [values] optional values
 * @static
 * @name print
 * @function
 */
export function print() {
  const args = Array.prototype.slice.call(arguments, 0)

  switch (args.length) {
    case 0:
      return

    case 1:
      consoleWriter().print(args[0] + '')
      return

    default: {
      args[0] = args[0] + '' // make sure first argument is a string
      const msg = MessageFormat.format(args[0], args.slice(1))
      consoleWriter().print(msg)
    }
  }
}

/**
 * Prints a string to the console, including newline
 *
 * @example
 * import {println} from 'josm/scriptingconsole'
 * println('Hello world!')
 *
 * // or use formatting
 * ('Hello world! My name is {0}', 'foo')
 *
 * @summary  Prints a string to the console, including newline
 * @static
 * @name println
 * @function
 * @param {string} message  the message
 * @param {...object} [values] optional values
 */
export function println() {
  var args = Array.prototype.slice.call(arguments, 0)

  switch (args.length) {
    case 0:
      return

    case 1:
      consoleWriter().println(args[0] + '')
      return

    default: {
      args[0] = args[0] + '' // make sure first argument is a string
      const msg = MessageFormat.format(args[0], args.slice(1))
      consoleWriter().println(msg)
    }
  }
}

/**
 * Clears the scripting console.
 *
 * @example
 * import * as console from 'josm/scriptingconsole'
 * console.clear()
 *
 * @summary  Clears the scripting console.
 * @function
 * @name clear
 * @static
 */
export function clear() {
  const action = ScriptingConsole.getInstance().getScriptLog().getClearAction()
  action.actionPerformed(null)
}

/**
 * Shows the scripting console
 *
 * @example
 * import * as console from 'josm/scriptingconsole'
 * console.show()
 *
 * @summary Shows the scripting console
 * @function
 * @name show
 * @static
 */
export function show() {
  ScriptingConsole.showScriptingConsole()
}

/**
 * Hides the scripting console
 *
 * @example
 * import * as console from 'josm/scriptingconsole'
 * console.hide()
 *
 * @summary Hides the scripting console
 * @function
 * @name hide
 * @static
 */
export function hide() {
  ScriptingConsole.hideScriptingConsole()
}

/**
 * Toggles the visibility of the scripting console
 *
 * @example
 * import * as console from 'josm/scriptingconsole'
 * console.toggle()
 *
 * @summary Toggles the visibility of the scripting console
 * @function
 * @name toggle
 * @static
 */
export function toggle() {
  ScriptingConsole.toggleScriptingConsole()
}
