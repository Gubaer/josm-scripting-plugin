var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var wb = require("josm/builder").WayBuilder;
var nb = require("josm/builder").NodeBuilder;
var rb = require("josm/builder").RelationBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var ArrayList = java.util.ArrayList;

tu.suite("properties access",
	test("id - read", function() {
		var r = rb.create(1234);		
		util.assert(r.id === 1234, "unexpected id");
	}),
	
	test("length - read", function() {
		var r = rb.create(1234);		
		util.assert(r.length === 0, "unexpected length, got {0}", r.length);
	}),
	
	test("members - read", function() {
		var r = rb.create(1234);		
		util.assert(r.members.length == 0, "should be empty array");
		
		r = rb.createProxy(1234);		
		util.assert(r.members.length == 0, "should be empty array, for proxy relation too");
		
		r = rb.withMembers(nb.create(),nb.create()).create(1234);
		util.assert(r.members.length == 2, "unexpected number of members");
	}),
	
	test("members - clear with null or undefined", function() {
		var r; 
		
		r = rb.withMembers(nb.create(),nb.create()).create(1234);
		r.members = null;
		util.assert(r.length == 0, "1 - unexpected number of members");
		
		r = rb.withMembers(nb.create(),nb.create()).create(1234);
		r.members = undefined;
		util.assert(r.length == 0, "2 - unexpected number of members");

		r = rb.withMembers(nb.create(),nb.create()).create(1234);
		r.members = [];
		util.assert(r.length == 0, "3 - unexpected number of members");
	}), 
	
	test("members - assign array or list ", function() {
		var r; 
		
		r = rb.create(1234);
		r.members = [nb.create(), rb.member('role.1', nb.create()), wb.create(12345)];
		util.assert(r.length == 3, "1 - unexpected number of members");
		
		var list = new ArrayList();
		list.add(nb.create());
		list.add(rb.member('role.1', nb.create()));
		list.add(wb.create(12345));
		r = rb.create(1234);
		r.members = list;
		util.assert(r.length == 3, "2 - unexpected number of members");
		
		// nulls and undefined are ignored 
		r = rb.create(1234);
		r.members = [null, nb.create(), rb.member('role.1', nb.create()), undefined, wb.create(12345)];
		util.assert(r.length == 3, "3  - unexpected number of members");
	})
).run();
