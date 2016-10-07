(function() {

/**
* <p>This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class javax.swing.JMenu}.</p>
 *
 * @module josm/mixin/JMenuMixin
 */

var util = require("josm/util");

var mixin = {};

mixin.add = function() {
    util.assert(arguments.length > 0,
        "Expected > 0 arguments, got {0}", arguments.length);
    if (arguments.length instanceof JSAction) {
        var action = arguments[0];
        action.addToMenu(this, arguments[1]);
    } else {
        // invoke the native add method
        this.$add.apply(this, Array.prototype.slice.call(arguments,0));
    }
};

}());