/**
 * <p>Provides a set of functions to write to the built-in scripting
 * console.</p>
 *
 * @module josm/scriptingconsole
 */
// -- imports
var MessageFormat = java.text.MessageFormat;

var scripting = org.openstreetmap.josm.plugins.scripting;
var ScriptingConsole = scripting.ui.console.ScriptingConsole;

function consoleWriter() {
    return ScriptingConsole.instance.scriptLog.logWriter;
}

/**
 * <p>Prints a string to the console</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.print("Hello world!");
 *
 * // or use formatting
 * console.print("Hello world! My name is {0}", "foo");
 *
 * @summary  Prints a string to the console
 * @param {string} message the message
 * @param {...object} [values] optional values
 * @static
 * @name print
 * @function
 */
exports.print = function() {
    var args = Array.prototype.slice.call(arguments,0);

    switch(args.length) {
    case 0:
        return;

    case 1:
        consoleWriter().print(args[0] + "");
        return;

    default:
        args[0] = args[0] + ""; // make sure first argument is a string
        var msg = MessageFormat.format(args[0],args.slice(1));
        consoleWriter().print(msg);
    }
};

/**
 * <p>Prints a string to the console, including newline</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.println("Hello world!");
 *
 * // or use formatting
 * console.println("Hello world! My name is {0}", "foo");
 *
 * @summary  Prints a string to the console, including newline
 * @static
 * @name println
 * @function
 * @param {string} message  the message
 * @param {...object} [values] optional values
 */
exports.println = function() {
    var args = Array.prototype.slice.call(arguments,0);

    switch(args.length) {
    case 0:
        return;

    case 1:
        consoleWriter().println(args[0] + "");
        return;

    default:
        args[0] = args[0] + ""; // make sure first argument is a string
        var msg = MessageFormat.format(args[0],args.slice(1));
        consoleWriter().println(msg);
    }
};

/**
 * <p>Clears the scripting console.</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.clear();
 *
 * @summary  Clears the scripting console.
 * @function
 * @name clear
 * @static
 */
exports.clear = function() {
    var action = ScriptingConsole.instance.scriptLog.clearAction;
    action.actionPerformed(null);
};

/**
 * <p>Shows the scripting console</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.show();
 *
 * @summary Shows the scripting console
 * @function
 * @name show
 * @static
 */
exports.show = function() {
    ScriptingConsole.showScriptingConsole();
};

/**
 * <p>Hides the scripting console</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.hide();
 *
 * @summary Hides the scripting console
 * @function
 * @name hide
 * @static
 */
exports.hide = function() {
    ScriptingConsole.hideScriptingConsole();
};

/**
 * <p>Toggles the visibility of the scripting console</p>
 *
 * @example
 * var console = require("josm/scriptingconsole");
 * console.toggle();
 *
 * @summary Toggles the visibility of the scripting console
 * @function
 * @name toggle
 * @static
 */
exports.toggle = function() {
    ScriptingConsole.toggleScriptingConsole();
};

