var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var wb = require("josm/builder").WayBuilder;
var nb = require("josm/builder").NodeBuilder;
var rb = require("josm/builder").RelationBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var Node    = org.openstreetmap.josm.data.osm.Node;
var Way     = org.openstreetmap.josm.data.osm.Way;
var ArrayList = java.util.ArrayList;

var suites = [];

suites.push(tu.suite("properties access",
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
));

suites.push(tu.suite("get and set relation members using [...]",
	test("get with [..]", function() {
		var members = [nb.create(), rb.member('role.1', nb.create()), wb.create(12345)];
		var r = rb.withMembers(members).create(12345);
		util.assert(r[0].role == "", "unexpected role");
		util.assert(r[1].member instanceof Node , "unexpected type");
		util.assert(r[2].member instanceof Way , "unexpected type");
		tu.expectAssertionError("index out of range", function() {
			r[3];
		});
	}),
	
	test("set with [..]", function() {
		var members = [nb.create(), rb.member('role.1', nb.create()), wb.create(12345)];
		var r = rb.withMembers(members).create(12345);
		
		r[0] = wb.create(1111);
		util.assert(r[0].member instanceof Way, "should be a way");
		r[1] = rb.member('role.2', nb.create());
		util.assert(r[1].role == "role.2", "unexpected role, should be {0}", "role.2");
		r[3] = nb.create();
		util.assert(r.length == 4, "unexpected length");
		
		tu.expectAssertionError("can't assign null", function() {
			var r = rb.withMembers(members).create(12345);
			r[0] = null;
		});
		
		tu.expectAssertionError("can't assign undefined", function() {
			var r = rb.withMembers(members).create(12345);
			r[0] = undefined;
		});
		
		tu.expectAssertionError("can't assign a string", function() {
			var r = rb.withMembers(members).create(12345);
			r[0] = "should fail";
		});
		
		tu.expectAssertionError("illegal index ", function() {
			var r = rb.withMembers(members).create(12345);
			r[5] = nb.create();
		});
	})
));	

suites.push(tu.suite("modified flag",
	test("modify the members list", function() {
		var members = [nb.create(), rb.member('role.1', nb.create()), wb.create(12345)];		
		var r = rb.withMembers(members).create(12345);
		util.assert(!r.modified, "1 - should not be modified");
		
		r.members = members;	
		util.assert(!r.modified, "2 - should not be modified");
		
		r.members = [members[0], members[1]];	
		util.assert(r.modified, "3 - should be modified");
		
		r.members = [members[0], members[1]];	
		util.assert(r.modified, "4 - should be modified");
		
		r.members = [members[0]];	
		util.assert(r.modified, "5 - should be modified");
	})
));

exports.run = function() {
    for (var i=0; i<suites.length; i++) {
        suites[i].run();
    }
};