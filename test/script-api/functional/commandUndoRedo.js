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

// add a node and check node is there
command.add(n).applyTo(layer);
util.assert(layer.data.has(n));

// undo - node still there? 
josm.commands.undo();
util.assert(! layer.data.has(n));

// redo - node again there?
josm.commands.redo();
util.assert(layer.data.has(n));

// clear all commands
josm.commands.clear();