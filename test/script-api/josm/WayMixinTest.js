var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var wb = require("josm/builder").WayBuilder;
var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;

var suite = tu.suite(
	test("way - property 'id' - read", function() {
		var w = wb.create(1234);
		util.assert(w.id === 1234, "unexpected id");
	}),
	
	test("way - property 'id' - write (should fail)", function() {
		tu.expectError(function(){
			var w = wb.create(1234);
			w.id = 23455;
		});
	}),
	
	test("way - property 'length' - read", function() {
		var w=wb.create(12345, {nodes: [nb.create(), nb.create()]});
		util.assert(w.length == 2, "unexpected length");
	}),
	
	test("way - property 'length' - write (should fail)", function() {
		tu.expectError(function(){
			var w=wb.create(12345, {nodes: [nb.create(), nb.create()]});
			w.length = 5;
		});
	}),
	
	test("way - function 'contains'", function() {
		var n1 = nb.create();
		var n2 = nb.create();
		var n3 = nb.create();
		var w = wb.create(12345, {nodes: [n1,n2]});
		
		util.assert(w.contains(n1), "should contain n1");
		util.assert(w.contains(n2), "should contain n2");
		util.assert(!w.contains(n3), "should not contain n3");
		tu.expectError(function(){
			w.contains(null);
		});
		tu.expectError(function(){
			w.contains(undefined);
		});
		tu.expectError(function(){
			w.contains("string not supported");
		});
	}),
	
	test("way - index access", function() {
		var n1 = nb.create();
		var n2 = nb.create();
		var n3 = nb.create();
		var w = wb.create(12345, {nodes: [n1,n2]});
		
		util.assert(w[0] == n1, "index 0 -> n1");
		util.assert(w[1] == n2, "index 1 -> n2");
		tu.expectAssertionError(function(){
			w[-1];
		});
		tu.expectAssertionError(function(){
			w[2];
		});
	})
	
);

suite.run();