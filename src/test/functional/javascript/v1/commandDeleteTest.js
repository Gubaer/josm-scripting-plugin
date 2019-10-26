/*
 * Functional test for deleting objects with a undoable/reduable command
 * to a data layer.
 * 
 * Load in the JOSM scripting console and run.
 */
var command = require("josm/command");
var util = require("josm/util");
var layer = josm.layers.addDataLayer();

var ds = layer.data;
var n1 = ds.nodeBuilder.create(1234);
var n2 = ds.nodeBuilder.withPosition(3,4).create();

var w1 = ds.wayBuilder.withTags({highway: "residential"}).withNodes(n1,n2)
    .create(777);
var r1 = ds.relationBuilder.withMembers(n1,w1).create();

// delete three objects in the layer 
command.delete(n1,w1,n2).applyTo(layer);
var ds = layer.data;
util.assert(ds.get(n1).deleted);
util.assert(ds.get(n2).deleted);
util.assert(ds.get(w1).deleted);
