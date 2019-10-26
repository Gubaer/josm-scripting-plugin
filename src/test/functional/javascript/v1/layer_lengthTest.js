/*
 * copy/paste in the JOSM scripting console when no layers are present
 * and click "Run"
 */
var tu = require("josm/unittest");
var util = require("josm/util");
var layers = require("josm/layers");
var console = require("josm/scriptingconsole");

// initially we have no layers
util.assert(layers.length == 0, "length should be 0");
console.println("OK - length is 0");
// add two layers 
layers.addDataLayer();
layers.addDataLayer();
util.assert(layers.length == 2, "should have 2 layers");
console.println("OK - added 2 layers - length is 2");

// remove the layers
layers.remove(0);
layers.remove(0);
util.assert(layers.length == 0, "length should be 0");
console.println("OK - removed 2 layers - length is 0 again");