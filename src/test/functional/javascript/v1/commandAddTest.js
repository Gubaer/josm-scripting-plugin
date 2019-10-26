/*
 * Functional test for adding objects with a undoable/reduable command
 * to a data layer.
 * 
 * Load in the JOSM scripting console and run.
 */
var command = require("josm/command");
var nb = require("josm/builder").NodeBuilder;
var layer = josm.layers.addDataLayer();

// add a node to a layer
command.add(nb.create()).applyTo(layer);

// another approach: add two nodes to a layer
layer.apply(command.add(nb.create(),nb.create()));