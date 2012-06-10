var tu = require("josm/unittest");
var util = require("josm/util");
var conf = require("josm/api").ApiConfig;
var out = java.lang.System.out;
var URL = java.net.URL;

tu.suite("ApiConfig.serverUrl",
	tu.test("get", function() {
		var url = conf.serverUrl;
		out.println("url: " + url);
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