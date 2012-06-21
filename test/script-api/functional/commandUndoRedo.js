/*
 * Functional test for adding objects with a undoable/redoable command
 * to a data layer.
 * 
 * Load in the JOSM scripting console and run.
 */
var command = require("josm/command");
var nb = require("josm/builder").NodeBuilder;
var util = require("josm/util");
var layer = josm.layers.addDataLayer();

var n = nb.create();
// add a node to a layer
command.add(n).applyTo(layer);

// und and redo
josm.commands.undo();
josm.commands.redo();

// node still there?
util.assert(layer.data.has(n))

// clear all commands
josm.commands.clear();