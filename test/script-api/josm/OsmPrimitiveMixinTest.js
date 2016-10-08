var tu = require("josm/unittest");
var util = require("josm/util");

var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;
var User = org.openstreetmap.josm.data.osm.User;
var Node    = org.openstreetmap.josm.data.osm.Node;
var Way   = org.openstreetmap.josm.data.osm.Node;
var Way     = org.openstreetmap.josm.data.osm.Way;
var LatLon     = org.openstreetmap.josm.data.coor.LatLon;
var ArrayList = java.util.ArrayList;
var HashMap = java.util.HashMap;

var suites = [];

suites.push(tu.suite("properties access",
	tu.test("property 'id'", function() {
		var n = nb.create(1234);		
		util.assert(n.id === 1234, "unexpected id");
		
		tu.expectError("id - set - should fail", function() {
			var n = nb.create(1234);
			n.id = 1234;
		});
	}),
	tu.test("property 'version'", function() {
		var n = nb.create(1234,{version: 9});		
		util.assert(n.version == 9, "unexpected version");
		
		tu.expectError("version - set - should fail", function() {
			n.version = 9;
		});
	}),
	tu.test("property 'isLocal and 'isNew'", function() {
		var n;
		
		n=nb.create();		
		util.assert(n.isLocal, "1- should be true");
		util.assert(n.isNew, "2 - should be true");
		
		n=nb.create(1);
		util.assert(!n.isLocal, "1- should be false");
		util.assert(!n.isNew, "2 - should be false");
	}),
	tu.test("property 'isGlobal'", function() {
		var n;
		
		n=nb.create();		
		util.assert(!n.isGlobal, "should be false");
		
		n=nb.create(1);
		util.assert(n.isGlobal, "should be true");
	}),
	tu.test("property 'dataSet'", function() {
		var n;
		
		n=nb.create();		
		util.assert(n.dataSet == undefined, "should be undefined");
		
		var ds  = new DataSet();
		var nb1 = nb.forDataSet(ds);		
		n=nb1.create();
		util.assert(n.dataSet === ds, "should be ds");
	}),
	tu.test("property 'user'", function() {
		var n;
		
		var user = User.createOsmUser(1, "test");
		
		n=nb.create();		
		util.assert(n.user == undefined, "should be undefined");
		
		n.user = user;
		util.assert(n.user == user, "1 -should be user");
		
		n.user = "test";
		util.assert(n.user == user, "2 - should be user");
		
		n.user = 1;
		util.assert(n.user == user, "3 - should be user");
		
		n.user = null;
		util.assert(n.user == null, "4 - should be null");
		
		n.user = null;
		util.assert(n.user == null, "5 - should be null");
		
		tu.expectAssertionError("user - can't assign unkonwn user name", function() {
			n.user = "nosuchuser";
		});
		
		tu.expectAssertionError("user - can't assign unkonwn user id", function() {
			n.user = 9999;
		});
		
		tu.expectAssertionError("user - can't assign an object", function() {
			n.user = {name: "test"};
		});
	}),
	
	tu.test("property 'changesetId'", function() {
		var n;
		
		n=nb.create();		
		util.assert(n.changesetId == undefined, "should be undefined");
		
		n=nb.create(1234);
		n.changesetId = 55;
		util.assert(n.changesetId == 55, "should be 55");
	}),
	
	tu.test("property 'isIncomplete' and 'isProxy'", function() {
		var n;
		
		n=nb.create();		
		util.assert(!n.isIncomplete, "1- should be false");
		util.assert(!n.isProxy, "2 - should be false");
		
		n=nb.createProxy(1234);		
		util.assert(n.isIncomplete, "1- should be true");
		util.assert(n.isProxy, "2 - should be true");
	}),
	
	tu.test("property 'isModified'", function() {
		var n;
		
		n=nb.create();		
		util.assert(!n.isModified, "1- should be false");
		util.assert(!n.modified, "2 - should be false");

        n.isModified = true;
        util.assert(n.isModified, "1 - should be true");
        util.assert(n.modified, "2 - should be true");
	}),	
	
	tu.test("property 'isDeleted'", function() {
		var n;
		
		n=nb.create();		
		util.assert(!n.isDeleted, "1- should be false");
		util.assert(!n.deleted, "2 - should be false");

        n.isDeleted = true;
        util.assert(n.isDeleted, "1 - should be true");
        util.assert(n.deleted, "2 - should be true");
        
        n.deleted = false;
		util.assert(!n.isDeleted, "1- should be false");
		util.assert(!n.deleted, "2 - should be false");
		

		tu.expectAssertionError("undefined value", function() {
			n.deleted = undefined;
		});
		
		tu.expectAssertionError("string value not supported", function() {
			n.deleted = "true";
		});
	})	
));


suites.push(tu.suite("setting and getting tags",
	tu.test("set/get a tag", function() {
		var n = nb.create(1234);		
		util.assert(!n.has("name"), "should not have name tag");
		util.assert(n.get("name") == undefined, "1 -name tag should not be defined");
		util.assert(n.tags.name == undefined, "2 - name tag should not be defined");
		
		n.set("name", "aname");
		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be aname");
		util.assert(n.tags.name == "aname", "2 - tag should be aname");		
	}),
	tu.test("removing a tag using set(null)", function() {
		var n = nb.create(1234);				
		n.set("name", "aname");		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be aname");
		util.assert(n.tags.name == "aname", "2 - tag should be aname");
		
		n.set("name", null);
		util.assert(!n.has("name"), "should not have name tag");
		util.assert(n.get("name") == undefined, "1 -name tag should not be defined");
		util.assert(n.tags.name == undefined, "2 - name tag should not be defined");
	}),
	tu.test("removing a tag using set(undefined)", function() {
		var n = nb.create(1234);				
		n.set("name", "aname");		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be aname");
		util.assert(n.tags.name == "aname", "2 - tag should be aname");
		
		n.set("name", undefined);
		util.assert(!n.has("name"), "should not have name tag");
		util.assert(n.get("name") == undefined, "1 -name tag should not be defined");
		util.assert(n.tags.name == undefined, "2 - name tag should not be defined");
	}),
	tu.test("removing a tag using remove(name)", function() {
		var n = nb.create(1234);				
		n.set("name", "aname");		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be aname");
		util.assert(n.tags.name == "aname", "2 - tag should be aname");
		
		n.remove("name");
		util.assert(!n.has("name"), "should not have name tag");
		util.assert(n.get("name") == undefined, "1 -name tag should not be defined");
		util.assert(n.tags.name == undefined, "2 - name tag should not be defined");
	}),
	
	tu.test("normalize tag name, but not tag value", function() {
		var n = nb.create(1234);		
		
		n.set(" name ", " aname ");
		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.has(" name "), "should not have < name > tag");
		util.assert(n.get("name") == " aname ", "1 - tag should be < aname >");
		util.assert(n.tags.name == " aname ", "2 - tag should be < aname >");		
	}),
	
	tu.test("assign a tag - using tags object", function() {
		var n = nb.create(1234);
		
		n.tags.name = "aname";
		util.assert(n.has("name"), "1 - should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.tags.name == "aname", "2 - tag should be <aname>");
		
		n.tags[" name "] = "aname";
		util.assert(n.has("name"), " 3 - should have name tag");
		util.assert(n.get("name") == "aname", "4  - tag should be <aname>");
		util.assert(n.tags.name == "aname", "5 - tag should be <aname>");
	}),
	
	tu.test("assign a tag - using tags object - non string value", function() {
		var n = nb.create(1234);
		
		n.tags.name = 5;
		util.assert(n.has("name"), "1 - should have name tag");
		util.assert(n.get("name") == "5", "1 - tag should be <5>");
		util.assert(n.tags.name == "5", "2 - tag should be <5>");		
	}),
	
	tu.test("remove a tag - using tags object - null value", function() {
		var n = nb.create(1234);
		
		n.tags.name = "aname";
		util.assert(n.has("name"), "1 - should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.tags.name == "aname", "2 - tag should be <aname>");
		
		n.tags.name = null;
		util.assert(!n.has("name"), "should not have a name tag");		
	}),
	
	tu.test("remove a tag - using tags object - undefined value", function() {
		var n = nb.create(1234);
		
		n.tags.name = "aname";
		util.assert(n.has("name"), "1 - should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.tags.name == "aname", "2 - tag should be <aname>");
		
		n.tags.name = undefined;
		util.assert(!n.has("name"), "should not have a name tag");		
	}),
	
	tu.test("remove a tag - using tags object - using delete opeartor", function() {
		var n = nb.create(1234);
		
		n.tags.name = "aname";
		util.assert(n.has("name"), "1 - should have name tag");
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.tags.name == "aname", "2 - tag should be <aname>");
		
		delete n.tags.name;
		util.assert(!n.has("name"), "should not have a name tag");		
	}),
	
	tu.test("assign tags - an object with properties", function() {
		var n = nb.create(1234);		
		
		n.tags = {name: "aname", " normalized ": " a value with blanks "};
				
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.has("normalized"), "should have normalized tag");		
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.get("normalized") == " a value with blanks ", "1 - tag should be < a value with blanks >");
	}),
	tu.test("assign tags - a map", function() {
		var n = nb.create(1234);		
		
		var tags = new HashMap();
		tags.put("name", "aname");
		tags.put(" normalized ", " a value with blanks ");
		
		n.tags = tags;
		
		util.assert(n.has("name"), "should have name tag");
		util.assert(n.has("normalized"), "should have normalized tag");		
		util.assert(n.get("name") == "aname", "1 - tag should be <aname>");
		util.assert(n.get("normalized") == " a value with blanks ", "1 - tag should be < a value with blanks >");
	}),
	tu.test("assign tags - null", function() {
		var n = nb.create(1234);		
		
		n.tags = {name: "aname", " normalized ": " a value with blanks "};
		n.tags = null;
		for (var p in n.tags) {
			if (n.tags.hasOwnProperty(p)) {
				util.assert(false, "should not have a tag");
			}
		}
	}),
	tu.test("assign tags - undefined", function() {
		var n = nb.create(1234);		
		
		n.tags = {name: "aname", " normalized ": " a value with blanks "};
		n.tags = undefined;
		for (var p in n.tags) {
			if (n.tags.hasOwnProperty(p)) {
				util.assert(false, "should not have a tag");
			}
		}
	}),
	tu.test("assign tags - skip null and undefined tags", function() {
		var n = nb.create(1234);		
		
		n.tags = {name: "aname", nulltag: null, undefinedtag: undefined};
		util.assert(n.has("name"), "should have name tag");
		util.assert(!n.has("nulltag"), "should not have null tag");	
		util.assert(!n.has("undefinedtag"), "should not have undefinedtag tag");
	}),
	
	tu.test("assign tags - assign another tags object ", function() {
		var n1 = nb.create(1);		
		var n2 = nb.create(2);
		
		n1.tags = {t1: "v1", t2: "v2"};
		n2.tags = {t3: "v3", t4: "v4"};
		n1.tags = n2.tags;
		
		util.assert(n1.has("t3"), "should have t3 tag");
		util.assert(n1.has("t4"), "should have t4 tag");
		util.assert(!n1.has("t1"), "should  not have t1 tag");
		util.assert(!n1.has("t2"), "should  not have t2 tag");
		
		util.assert(n1.tags.t3 == "v3", "t3 tag should be v3");
		util.assert(n1.tags.t4 == "v4", "t4 tag should be v4");		
	}),
	
	tu.test("has - regexp test", function() {
		var n = nb.create(1234);		
		
		n.tags = {"name:de": "german name", "leVeL.1": "on" };
		util.assert(n.has(/^name/), "1 - should match");
		util.assert(!n.has(/^name$/), "2 - should not match");
		util.assert(n.has(/^name(:.*)?$/), "3 - should match");
		
		util.assert(!n.has(/^level/), "4 - should not match");
		util.assert(n.has(/^level/i), "5 - should  match");
		util.assert(n.has(/^level\.\d+$/i), "6 - should  match");
	})
));

exports.run = function() {
    for (var i=0; i<suites.length; i++) {
        suites[i].run();
    }
};