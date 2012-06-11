var tu = require("josm/unittest");
var util = require("josm/util");
var conf = require("josm/api").ApiConfig;
var out = java.lang.System.out;
var URL = java.net.URL;

tu.suite("ApiConfig.serverUrl",
	tu.test("get", function() {
		var url = conf.serverUrl;
		util.assert(url, "url expected");
	}),
	tu.test("set - valid url as string", function() {
		conf.serverUrl = "http://test-api.osm.org";
		util.assert(conf.serverUrl == "http://test-api.osm.org", "unexpected url ");
	}),
	tu.test("set - valid url as URL", function() {
		conf.serverUrl = new URL("http://test-api.osm.org");
		util.assert(conf.serverUrl == "http://test-api.osm.org", "unexpected url ");
	}),
	tu.test("set - clear with null", function() {
		conf.serverUrl = null;
		util.assert(conf.serverUrl == conf.defaultServerUrl, "should be default");
	}),
	tu.test("set - clear with undefined", function() {
		conf.serverUrl = undefined;
		util.assert(conf.serverUrl == conf.defaultServerUrl, "should be default");
	})
).run();


tu.suite("ApiConfig.authMethod",
	tu.test("get", function() {
		var authMethod = conf.authMethod;
		util.assert(authMethod, "authMethod expected");
	}),
	tu.test("set - valid method", function() {
		conf.authMethod = "basic";
		util.assert(conf.authMethod == "basic", "1 - unexpected authMethod");
		
		conf.authMethod = "oauth";
		util.assert(conf.authMethod == "oauth", "2 - unexpected authMethod");

	}),
	tu.test("set - valid method (whitespace and case independent)", function() {
		conf.authMethod = "  BasiC  ";
		util.assert(conf.authMethod == "basic", "1 - unexpected authMethod");
		
		conf.authMethod = "   oAuTh\t  ";
		util.assert(conf.authMethod == "oauth", "2 - unexpected authMethod");
	}),		
	tu.test("set - null - should fail", function() {
		tu.expectAssertionError("set - null - should fail", function() {
			conf.serverUrl = null;
		});
	}),
	tu.test("set - undefined - should fail", function() {
		tu.expectAssertionError("set - undefined - should fail", function() {
			conf.serverUrl = undefined;
		});
	})	
).run();