/*
 * Functional test for chaning objects with a undoable/reduable command
 * to a data layer.
 *
 * Load in the JOSM scripting console and run.
 */
var command = require("josm/command");
var nb = require("josm/builder").NodeBuilder;
var wb = require("josm/builder").WayBuilder;
var rb = require("josm/builder").RelationBuilder;
var member = require("josm/builder").RelationBuilder.member;

var util = require("josm/util");
var layer = josm.layers.addDataLayer();

var n = nb.create();
// add a node to a layer
command.add(n).applyTo(layer);

command.change(n, {lat: 11.11}).applyTo(layer);
util.assert(n.lat == 11.11, "1- unexpected lat");

command.change(n, {lon: 22.22}).applyTo(layer);
util.assert(n.lon == 22.22, "2 -unexpected lon");

command.change(n, {pos: {lat: 33.33, lon: 44.44}}).applyTo(layer);
util.assert(n.lat == 33.33, "3 - unexpected lat");
util.assert(n.lon == 44.44, "4 - unexpected on");

command.change(n, {
    lat: 55.55,
    tags: {
        name: "myname"
    }
}).applyTo(layer);

// -------------------- changing node list
// create and add a new way
var w = wb.withNodes(nb.create(),nb.create()).create(1234);
command.add(w.nodes, w).applyTo(layer);

// create and add three nodes
var newnodes =  [nb.create(),nb.create(),nb.create()]
command.add(newnodes).applyTo(layer);

// change the ways node list
command.change(w, {nodes: newnodes}).applyTo(layer);

// -------------------- changing relation members
var n = nb.create();
var w = wb.create();
var r = rb.create();
command.add(n,w,r).applyTo(layer);
var relation = rb.withMembers(n,w,r).create();
command.add(relation).applyTo(layer);

command.change(relation, {members: [member("role.1", n)]}).applyTo(layer);
util.assert(relation.length == 1, "unexpected number of members");

