/**
 * <p>Provides a set of functions to write to the built-in scripting
 * console.</p>
 *
 * @module josm/scriptingconsole
 */
/* global Java */

// -- imports
const MessageFormat = Java.type('java.text.MessageFormat')

// const scripting = org.openstreetmap.josm.plugins.scripting;
const ScriptingConsole = Java.type('org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole')

function consoleWriter () {
  // TODO(karl): have to call getInstance(), getScriptLog(), etc.?
  return ScriptingConsole.instance.scriptLog.logWriter
}

/**
 * <p>Prints a string to the console</p>
 *
 * @example
 * const console = require('josm/scriptingconsole')
 * console.print('Hello world!')
 *
 * // or use formatting
 * console.print('Hello world! My name is {0}', 'foo')
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary  Prints a string to the console
 * @name print
 */
exports.print = function () {
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
 * <p>Prints a string to the console, including newline</p>
 *
 * @example
 * const console = require('josm/scriptingconsole')
 * console.println('Hello world!')
 *
 * // or use formatting
 * console.println('Hello world! My name is {0}', 'foo');
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary  Prints a string to the console, including newline
 * @name println
 */
exports.println = function () {
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
 * <p>Clears the scripting console.</p>
 *
 * @example
 * const console = require('josm/scriptingconsole')
 * console.clear()
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary  Clears the scripting console.
 * @name clear
 */
exports.clear = function () {
  // TODO(karl): have to invoke getInstance(), etc. ?
  const action = ScriptingConsole.instance.scriptLog.clearAction
  action.actionPerformed(null)
}

/**
 * <p>Shows the scripting console</p>
 *
 * @example
 * const console = require('josm/scriptingconsole')
 * console.show()
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary Shows the scripting console
 * @name show
 */
exports.show = function () {
  ScriptingConsole.showScriptingConsole()
}

/**
 * <p>Hides the scripting console</p>
 *
 * @example
 * var console = require('josm/scriptingconsole')
 * console.hide()
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary Hides the scripting console
 * @name hide
 */
exports.hide = function () {
  ScriptingConsole.hideScriptingConsole()
}

/**
 * <p>Toggles the visibility of the scripting console</p>
 *
 * @example
 * const console = require('josm/scriptingconsole')
 * console.toggle()
 *
 * @memberof josm/scriptingconsole
 * @method
 * @summary Toggles the visibility of the scripting console
 * @name toggle
 */
exports.toggle = function () {
  ScriptingConsole.toggleScriptingConsole()
}
