(function() {

/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.layer.OsmDataLayer}.</p>
 *
 * @module josm/mixin/OsmDataLayerMixin
 */

var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;
var Bounds = org.openstreetmap.josm.data.Bounds;
var Command = org.openstreetmap.josm.command.Command;

/**
 * <p>This mixin is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.layer.OsmDataLayer}.</p>
 *
 * @mixin OsmDataLayerMixin
 * @forClass org.openstreetmap.josm.gui.layer.OsmDataLayer
 * @memberof josm/mixin/OsmDataLayerMixin
 *
 */
var mixin = {};

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
 * @method
 * @instance
 * @name apply
 * @summary Apply commands
 * @memberOf OsmDataLayerMixin
 */
mixin.apply = function() {
    var Main = org.openstreetmap.josm.Main;
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
            Main.main.undoRedo.add(cmd);
        });
    } finally {
        this.data.endUpdate();
    }
};

exports.forClass=org.openstreetmap.josm.gui.layer.OsmDataLayer;
exports.mixin = mixin;

}());