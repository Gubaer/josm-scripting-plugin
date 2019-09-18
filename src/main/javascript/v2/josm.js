    /**
     * This module is auto-loaded by the scripting plugin. It provides the
     * implementation of the global <code>josm</code> object.
     *
     * @module josm
     */

     /* global require */
    
    const Version = Java.type('org.openstreetmap.josm.data.Version')
    const MessageFormat = Java.type('java.text.MessageFormat')
    const System = Java.type('java.lang.System')
    const out = System.out
    const ScriptingConsole = Java.type(
        'org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole'
    )
    const JOptionPane = Java.type('javax.swing.JOptionPane')
    const HelpAwareOptionPane = Java.type('org.openstreetmap.josm.gui.HelpAwareOptionPane')
    const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')

    const util = require('josm/util')
    const layers = require('josm/layers')
    
    /**
     * This is the global JOSM API object. It represents a running JOSM
     * instance.
     *
     * @namespace josm
     */
    
    /**
     * Replies the current JOSM version string.
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
            return Version.getInstance().getVersionString()
        }
    })
    
    /**
     * Replies the layers object.
     *
     * @example
     * josm.alert("num layers: " + josm.layers.length)
     *
     * // name of first layer
     * josm.alert("num layers: " + josm.layers.get(0).name)
     *
     * @memberOf josm
     * @name layers
     * @field
     * @readOnly
     * @instance
     * @type {object}
     * @summary accessor for JOSM layers
     */
    Object.defineProperty(exports, 'layers', {
        enumerable: true,
        get: function() {
            return layers
        }
    })
    
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
     * josm.alert("Hello World!")
     *
     * // display an error alert
     * josm.alert("Got an error", {
     *    title: "Error Alert",
     *    messageType: "error"
     * })
     *
     * @memberOf josm
     * @instance
     * @method
     * @name alert
     * @summary display a message
     */
    exports.alert = function() {
        const map = {
            "information": JOptionPane.INFORMATION_MESSAGE,
            "info": JOptionPane.INFORMATION_MESSAGE,
            "error": JOptionPane.ERROR_MESSAGE,
            "warning": JOptionPane.WARNING_MESSAGE,
            "warn": JOptionPane.INFORMATION_MESSAGE,
            "question": JOptionPane.QUESTION_MESSAGE,
            "plain": JOptionPane.PLAIN_MESSAGE
        }

        function titleFromOptions(options) {
            return util.isString(options.title) ? options.title : ""
        }

        function messageTypeFromOptions(options) {
            if (util.isNumber(options.messageType)) {
                const mt = options.messageType
                for (let key in map){
                    if (!map.hasOwnProperty(key)) continue
                    if (mt == map[key]) return mt
                }
                return JOptionPane.INFORMATION_MESSAGE;
            } else if (util.isString(options.messageType)) {
                const opt = util.trim(options.messageType).toLowerCase()
                const ret = map[opt]
                return ret !== undefined ? ret : JOptionPane.INFORMATION_MESSAGE
            }
            return JOptionPane.INFORMATION_MESSAGE
        }
    
        switch(arguments.length){
        case 0: return
        case 1:
            HelpAwareOptionPane.showOptionDialog(MainApplication.getMainFrame(),
                arguments[0],"",
                JOptionPane.INFORMATION_MESSAGE,null)
            return
        default:
            if (typeof arguments[1] !== "object") {
                HelpAwareOptionPane.showOptionDialog(MainApplication.getMainFrame(),
                    arguments[0],"",
                    JOptionPane.INFORMATION_MESSAGE,null)
                return
            }
            const title = titleFromOptions(arguments[1])
            const messageType = messageTypeFromOptions(arguments[1])
            HelpAwareOptionPane.showOptionDialog(MainApplication.getMainFrame(),
                arguments[0],title,messageType,null)
        }
    }
    
    /**
     * Opens one or more files in JOSM.
     * <p>
     * Accepts a variable number of files. Each argument is either a string
     * (a file name) or a java.io.File.
     * <p>
     * Creates and opens layers in JOSM, depending on the kind of file opened:
     * <p>
     * <ul>
     *   <li>creates a data layer for data files</li>
     *   <li>creates a gpx layer for gpx files</li>
     *   <li>creates an image layer for a directory with images</li>
     *   <li>etc.</li>
     * </ul>
     *
     * * @example
     * // open a data file in a new data layer
     * josm.open("/my/data/file.osm")
     *
     * @memberOf josm
     * @instance
     * @method
     * @name open
     * @summary Opens one or more files in JOSM
     */
    exports.open = function() {
        const OpenFileAction = Java.type('org.openstreetmap.josm.actions.OpenFileAction')
        const File = Java.type('java.io.File')
    
        let files = []
        for (let i=0; i<arguments.length; i++) {
            let file = arguments[i]
            if (util.isNothing(file)) {
                continue
            } else if (util.isString(file)) {
                files.push(new File(file))
            } else if (file instanceof File) {
                files.push(file)
            } else {
                util.assert(false, 'unexpected value, got {0}', file)
            }
        }
        OpenFileAction.openFiles(files)
    }
    
    /**
     * Replies the global command history.
     * <p>
     *
     * Use this object to undo/redo commands, or to clear the command
     * history.
     *
     * @example
     * // undoes the last command
     * josm.commands.undo()
     *
     * // redoes two commands
     * josm.commands.redo(2)
     *
     * @memberOf josm
     * @name commands
     * @field
     * @readOnly
     * @instance
     * @type {CommandHistory}
     * @summary Replies the global command history.
     */
    const { CommandHistory } = require("josm/command")
    Object.defineProperty(exports, 'commands', {
        enumerable: true,
        value: CommandHistory
    })
    
    /**
     * Replies an accessor for JOSMs menu bar.
     * <p>
     *
     * Use this object to inspect or modify the menu bar, i.e. to add additional
     * menu items.
     *
     * @memberOf josm
     * @name menu
     * @field
     * @readOnly
     * @instance
     * @type {MenuBar}
     * @summary Replies an accessor for JOSMs menu bar.
     */

    //TODO(karl): implement
    // const menu = require("josm/ui/menu").MenuBar;
    const menu = undefined
    Object.defineProperty(exports, 'menu', {
        enumerable: true,
        value: menu
    })
    
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
    * const console = require("josm/scriptingconsole")
    * const cls = josm.loadClassFrom3dPartyPlugin(
    *    "contourmerge",
    *    "org.openstreetmap.josm.plugins.contourmerge.ContourMergePlugin"
    * )
    * // isEnabled() is a static method of the class ContourMergePlugin
    * console.println(cls.isEnabled())
    */
    exports.loadClassFrom3dPartyPlugin = function(pluginName, className) {        
        const ScriptingPlugin = Java.type(
            'org.openstreetmap.josm.plugins.scripting.ScriptingPlugin'
        )
        const cls = ScriptingPlugin.loadClassFrom3dPartyPlugin(
            pluginName,
            className
        )
        return cls
    }