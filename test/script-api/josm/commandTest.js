var tu = require("josm/unittest");
var util = require("josm/util");
var command = require("josm/command");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var wb = require("josm/builder").WayBuilder;
var OsmDataLayer = org.openstreetmap.josm.gui.layer.OsmDataLayer;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;

tu.suite("AddCommand",
	test("constructor - with array", function() {		
		var n1 = nb.create();
		var n2 = nb.create();
		var cmd = new command.AddCommand([n1,n2]);
		util.assert(cmd._objs.length == 2, "incorrect length");
	}),
	test("constructor - with list", function() {
		var n1 = nb.create();
		var n2 = nb.create();
		var list = new java.util.ArrayList();
		list.add(n1); list.add(n2);
		var cmd = new command.AddCommand(list);
		util.assert(cmd._objs.length == 2, "incorrect length");
	}),
	test("constructor - illegal arguments", function() {
		tu.expectAssertionError("should not be null", function() {
			var cmd = new command.AddCommand(null);
		});
		tu.expectAssertionError("should not be undefined", function() {
			var cmd = new command.AddCommand(undefined);
		});
	})
).run();	

tu.suite("add",
	test("add - one node", function() {		
		var n1 = nb.create();
		var cmd = command.add(n1);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 1, "wrong length");
	}),
	test("add - array of nodes and ways", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var cmd = command.add([n1,w1]);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 2, "wrong length");
	}),
	test("add - list of nodes and ways", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var list = new java.util.ArrayList();
		list.add(n1); list.add(w1);
		var cmd = command.add(list);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 2, "wrong length");
	}),
	test("add - arguments with nulls, undefined, and doublettes", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var cmd = command.add(n1,null,w1, undefined, n1);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 2, "wrong length");
	}),
	test("add - array with nulls, undefined, and doublettes", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var cmd = command.add([n1,null,w1, undefined, n1]);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 2, "wrong length");
	}),
	test("add - mixed arguments", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var list = new java.util.ArrayList();
		list.add(nb.create()); list.add(nb.create());
		var cmd = command.add(n1, [n1,w1], null, [list, n1, undefined]);
		util.assert(cmd instanceof command.AddCommand, "wrong type");
		util.assert(cmd._objs.length == 4, "wrong length");
	}),
	test("createJOSMCommand - one object", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var cmd = command.add(n1);
		var layer = new OsmDataLayer(new DataSet(), null, null);
		var jcmd = cmd.createJOSMCommand(layer);
		util.assert(jcmd, "should exist");		
	}),
	test("createJOSMCommand - one object", function() {
		var n1 = nb.create();
		var w1 = wb.create();
		var cmd = command.add([n1,w1]);
		var layer = new OsmDataLayer(new DataSet(), null, null);
		var jcmd = cmd.createJOSMCommand(layer);
		util.assert(jcmd, "should exist");
	})
).run();