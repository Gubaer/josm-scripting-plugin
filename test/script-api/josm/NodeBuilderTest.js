var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;

var expectError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		// OK 
	}
};

var expectAssertionError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		if (e.name != "AssertionError") {
			util.assert(false, "Expected AssertionError, got ", e.toSource());
		} 
	}
};

var suite = tu.suite(
	test("local node - most simple node", function() {
		var node = nb.local();
		util.assert(util.isSomething(node));		
	}),
	test("local node - with position", function() {
		var node = nb.withPosition(1,2).local();
		util.assert(util.isSomething(node));
		util.assert(node.getCoor().lat() == 1);
		util.assert(node.getCoor().lon() == 2);
		util.assert(node.lat == 1);
		util.assert(node.lon == 2);
	}),
	test("local node - with missing position", function() {
		expectError(function() {
			var node = nb.withPosition().local();
		});
	}),
	test("local node - with illegal lat", function() {
		expectError(function() {
			var node = nb.withPosition(-91,2).local();
		});
	}),
	test("local node - with illegal lon", function() {
		expectError(function() {
			var node = nb.withPosition(1,181).local();
		});
	}),
	test("local node - with tags", function() {
		var node = nb.withTags({name: "aName"}).local();
		util.assert(util.isSomething(node));	
		util.assert(node.get("name") == "aName");
		util.assert(node.tags.name == "aName");
	}),		
	test("local node - with tags - null", function() {
		var node = nb.withTags(null).local();
	}),
	test("local node - with tags - undef", function() {
		var node = nb.withTags(void(0)).local();
	}),
	test("local node - with tags - unsupported value", function() {
		expectError(function() {
			var node = nb.withTags("string value not allowed").local();
		});
	}),
	
	/* ----------------------------------------------------------------------------- */
	/* tests for global nodes */
	/* ----------------------------------------------------------------------------- */
	test("global node - most simple node", function() {
		var node = nb.global(1);
		util.assert(node.getUniqueId() == 1, "Expected id 1, got {0}", node.getUniqueId());
		util.assert(node.getVersion() == 1, "Expected version 1, got {0}", node.getVersion());
		util.assert(node.id == 1, "Expected id 1, got {0}", node.id);
		util.assert(node.version == 1, "Expected id 1, got {0}", node.version);
	}),
	
	test("global node - with id and version", function() {
		var node = nb.global(2,3);
		util.assert(node.getUniqueId() == 2);
		util.assert(node.getVersion() == 3);
		util.assert(node.id == 2);
		util.assert(node.version == 3);		
	}),

	test("global node - illegal id - 0", function() {
		expectAssertionError(function() {
			var node = nb.global(0);	
		});
	}),

	test("global node - illegal id - negative", function() {
		expectAssertionError(function() {
			var node = nb.global(-1);	
		});
	}),
	
	test("global node - illegal version - 0", function() {
		expectAssertionError(function() {
			var node = nb.global(1,0);	
		});
	}),
	
	test("global node - illegal version - negative", function() {
		expectAssertionError(function() {
			var node = nb.global(1,-1);	
		});
	}),
	
	test("global node - illegal version - not a number", function() {
		expectAssertionError(function() {
			var node = nb.global(1,"5");	
		});
	}),
	
	test("global node - illegal version - not a number - null", function() {
		expectAssertionError(function() {
			var node = nb.global(1,null);	
		});
	})
);

suite.run();