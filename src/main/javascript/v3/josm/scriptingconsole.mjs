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
 * @param {string} message the message
 * @param {...object} [values] optional values
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
 * println('Hello world! My name is {0}', 'foo')
 *
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
 */
export function toggle() {
  ScriptingConsole.toggleScriptingConsole()
}
