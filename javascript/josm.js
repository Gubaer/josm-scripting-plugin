(function() {
/**
 * <p>This module is auto-loaded by the scripting plugin. It provides the
 * implementation of the global <code>josm</code> object.</p>
 *
 * @module josm
 *
 */

var Version = org.openstreetmap.josm.data.Version;
var MessageFormat = java.text.MessageFormat;
var out = java.lang.System.out;
var ScriptingConsole =
    org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole;
var JOptionPane = javax.swing.JOptionPane;
var HelpAwareOptionPane = org.openstreetmap.josm.gui.HelpAwareOptionPane;
var Main = org.openstreetmap.josm.Main;
var util = require("josm/util");

/**
 * <p>This is the global JOSM API object. It represents a running JOSM
 * instance.</p>
 *
 * @namespace josm
 */

/**
 * <p>Replies the current JOSM version string.</p>
 *
 * @example
 * josm.alert(josm.version);
 *
 * @memberOf josm
 * @name version
 * @field
 * @readOnly
 * @instance
 * @type {string}
 * @summary JOSM version string
 */
Object.defineProperty(exports, "version", {
    enumerable: true,
    get: function() {
        return Version.getInstance().getVersionString();
    }
});

/**
 * <p>Replies the layers object.</p>
 *
 * @example
 * josm.alert("num layers: " + josm.layers.length);
 *
 * // name of first layer
 * josm.alert("num layers: " + josm.layers.get(0).name);
 *
 * @memberOf josm
 * @name layers
 * @field
 * @readOnly
 * @instance
 * @type {object}
 * @summary accessor for JOSM layers
 */
Object.defineProperty(exports, "layers", {
    enumerable: true,
    get: function() {
        return require("josm/layers");
    }
});

/**
 * <p>Displays an alert window with a message</p>
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">alert(message)</code><dt>
 *   <dd>Displays an information message with an OK button.</dd>
 *
 *   <dt><code class="signature">alert(message, ?options)</code><dt>
 *   <dd>Displays a message. The look and feel of the alert window depends on
 *   the <var>options</var>. The following options are supported:
 *   <dl>
 *      <dt><code>title</code>:string</dt>
 *      <dd>(optional) the window title. A string is expected. Empty string
 *      if missing.</dt>
 *
 *      <dd><code>messageType</code></dt>
 *      <dd>(optional) the message type. Use one of the following values:
 *         <ul>
 *            <li>{@class javax.swing.JOptionPane}.INFORMATION_MESSAGE,
 *            "info","information"</li>
 *            <li>{@class javax.swing.JOptionPane}.ERROR_MESSAGE,
 *                     "error"</li>
 *            <li>{@class javax.swing.JOptionPane}.WARNING_MESSAGE,
 *                    "warning", "warn"</li>
 *            <li>{@class javax.swing.JOptionPane}.QUESTION_MESSAGE,
 *                    "question"</li>
 *            <li>{@class javax.swing.JOptionPane}.PLAIN_MESSAGE,
 *                    "plain"</li>
 *         </ul>
 *         Default value is
 *         {@class javax.swing.JOptionPane}.INFORMATION_MESSAGE.
 *         String values are not case sensitive and leading and
 *         trailing white space is removed.
 *      </dd>
 *   </dl>
 *   </dd>
 * </dl>
 *
 * @example
 * // display an information alert
 * josm.alert("Hello World!");
 *
 * // display an error alert
 * josm.alert("Got an error", {
 *    title: "Error Alert",
 *    messageType: "error"
 * });
 *
 * @memberOf josm
 * @instance
 * @method
 * @name alert
 * @summary display a message
 */
exports.alert = function() {
    var map = {
            "information": JOptionPane.INFORMATION_MESSAGE,
            "info": JOptionPane.INFORMATION_MESSAGE,
            "error": JOptionPane.ERROR_MESSAGE,
            "warning": JOptionPane.WARNING_MESSAGE,
            "warn": JOptionPane.INFORMATION_MESSAGE,
            "question": JOptionPane.QUESTION_MESSAGE,
            "plain": JOptionPane.PLAIN_MESSAGE
    };
    function titleFromOptions(options) {
        if (util.isString(options.title)) return options.title;
        return "";
    };
    function messageTypeFromOptions(options) {
        if (util.isNumber(options.messageType)) {
            var mt = options.messageType;
            for (var key in map){
                if (!map.hasOwnProperty(key)) continue;
                if (mt == map[key]) return mt;
            }
            return JOptionPane.INFORMATION_MESSAGE;
        } else if (util.isString(options.messageType)) {
            var opt = util.trim(options.messageType).toLowerCase();
            var ret = map[opt];
            return ret !== undefined ? ret : JOptionPane.INFORMATION_MESSAGE;
        }
        return JOptionPane.INFORMATION_MESSAGE;
    };

    switch(arguments.length){
    case 0: return;
    case 1:
        HelpAwareOptionPane.showOptionDialog(Main.parent, arguments[0],"",
            JOptionPane.INFORMATION_MESSAGE,null);
        return;
    default:
        if (typeof arguments[1] !== "object") {
            HelpAwareOptionPane.showOptionDialog(Main.parent, arguments[0],"",
                JOptionPane.INFORMATION_MESSAGE,null);
            return;
        }
        var title = titleFromOptions(arguments[1]);
        var messageType = messageTypeFromOptions(arguments[1]);
        HelpAwareOptionPane.showOptionDialog(Main.parent, arguments[0],title,
            messageType,null);
    }
};

/**
 * <p>Opens one or more files in JOSM.</p>
 *
 * <p>Accepts a variable number of files. Each argument is either a string
 *    (a file name) or a java.io.File.</p>
 *
 * <p>Creates and opens layers in JOSM, depending on the kind of file opened:
 * </p>
 * <ul>
 *   <li>creates a data layer for data files</li>
 *   <li>creates a gpx layer for gpx files</li>
 *   <li>creates an image layer for a directory with images</li>
 *   <li>etc.</li>
 * </ul>
 *
 * * @example
 * // open a data file in a new data layer
 * josm.open("/my/data/file.osm");
 *
 * @memberOf josm
 * @instance
 * @method
 * @name open
 * @summary Opens one or more files in JOSM
 */
exports.open = function() {
    var OpenFileAction = org.openstreetmap.josm.actions.OpenFileAction;
    var io = java.io;

    var files = [];
    for (var i=0; i<arguments.length; i++) {
        var file = arguments[i];
        if (util.isNothing(file)) continue;
        if (util.isString(file)) {
            files.push(new io.File(file));
        } else if (file instanceof io.File) {
            files.push(file);
        } else {
            util.assert(false, "unexpected value, got {0}", file);
        }
    }
    OpenFileAction.openFiles(files);
};

/**
 * <p>Replies the global command history.</p>
 *
 * <p>Use this object to undo/redo commands, or to clear the command
 *  history.</p>
 *
 * @example
 * // undoes the last command
 * josm.commands.undo();
 *
 * // redoes two commands
 * josm.commands.redo(2);
 *
 * @memberOf josm
 * @name commands
 * @field
 * @readOnly
 * @instance
 * @type {CommandHistory}
 * @summary Replies the global command history.
 */
var commands = require("josm/command").CommandHistory;
Object.defineProperty(exports, "commands", {
    enumerable: true,
    value: commands
});

/**
 * <p>Replies an accessor for JOSMs menu bar.</p>
 *
 * <p>Use this object to inspect or modify the menu bar, i.e. to add additional
 * menu items.</p>
 *
 * @memberOf josm
 * @name menu
 * @field
 * @readOnly
 * @instance
 * @type {MenuBar}
 * @summary Replies an accessor for JOSMs menu bar.
 */
var menu = require("josm/ui/menu").MenuBar;
Object.defineProperty(exports, "menu", {
    enumerable: true,
    value: menu
});

/**
* <p>Loads a class using the class loader of a 3d-party plugin</p>
*
* @memberOf josm
* @name loadClassFrom3dPartyPlugin
* @instance
* @method
* @summary Loads a class using the class loader of a 3d-party plugin
*
* @example
* var console = require("josm/scriptingconsole");
* var cls = josm.loadClassFrom3dPartyPlugin(
*    "contourmerge",
*    "org.openstreetmap.josm.plugins.contourmerge.ContourMergePlugin"
* );
* // isEnabled() is a static method of the class ContourMergePlugin
* console.println(cls.isEnabled());
*/
exports.loadClassFrom3dPartyPlugin = function(pluginName, className) {
    var ScriptingPlugin =
        org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
    var cls = ScriptingPlugin.loadClassFrom3dPartyPlugin(
        pluginName,
        className
    );
    return cls;
}

}());