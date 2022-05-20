/**
 * This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.layer.OsmDataLayer}.
 *
 * @module josm/mixin/OsmDataLayerMixin
 */

var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Bounds = org.openstreetmap.josm.data.Bounds;
var Command = org.openstreetmap.josm.command.Command;

/**
 * This mixin is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.layer.OsmDataLayer}.
 *
 * @mixin OsmDataLayerMixin
 * @forClass org.openstreetmap.josm.gui.layer.OsmDataLayer
 *
 */
exports.mixin = {};
exports.forClass=org.openstreetmap.josm.gui.layer.OsmDataLayer;


/**
 * Applies one or more undoable/redoable commands to the data layer.
 *
 * <strong>Signature</strong>
 * <dl>
 *   <dt><code class="signature">apply(cmd,cmd,...)</code></dt>
 *   <dd><code>cmd</code> is either an instance of
 *   {@class org.openstreetmap.josm.command.Command}
 *   or an object with a method <code>createJOSMCommand(OsmDataLayer)</code>.
 *   </dd>
 * </dl>
 *
 * @param {...(org.openstreetmap.josm.command.Command | object)} commands the commands to apply
 * @function
 * @name apply
 * @summary Apply commands
 * @memberOf module:josm/mixin/OsmDataLayerMixin~OsmDataLayerMixin
 */
exports.mixin.apply = function() {
    var UndoRedoHandler = org.openstreetmap.josm.data.UndoRedoHandler;
    var cmds = [];
    var layer = this;
    util.each(arguments, function(arg) {
        if (util.isNothing(arg)) return;
        if (arg.createJOSMCommand) {
            cmds.push(arg.createJOSMCommand(layer));
        } else if (arg instanceof Command) {
            cmds.push(arg);
        } else {
            util.assert(false, "Unexpected type of arguments, got {0}",arg);
        }
    });
    try {
        this.data.beginUpdate();
        util.each(cmds, function(cmd) {
            UndoRedoHandler.getInstance().add(cmd);
        });
    } finally {
        this.data.endUpdate();
    }
};
