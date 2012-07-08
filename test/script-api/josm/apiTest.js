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



tu.suite("ApiConfig.getCredentials",
	tu.test("get basic credentials", function() {
		var credentials = conf.getCredentials("basic");
		out.println("user=" + credentials.user + ", password=" + credentials.password);
	}),
	tu.test("get oauth credentials", function() {
		var credentials = conf.getCredentials("oauth");
		out.println(String(credentials));
	}),
	tu.test("unsupported authentication methods", function() {
		tu.expectAssertionError("null authMethod", function() {
			var credentials = conf.getCredentials(null);
		});
		tu.expectAssertionError("undefined authMethod", function() {
			var credentials = conf.getCredentials(undefined);
		});
		tu.expectAssertionError("null authMethod", function() {
			var credentials = conf.getCredentials("nosuchmethod");
		});
	})	
).run();

tu.suite("ApiConfig.setCredentials",
	tu.test("set basic credentials - with object", function() { 
		var cred1 = {user: "testuser", password: "testpassword"};
		conf.setCredentials("basic", cred1);
		var cred2 = conf.getCredentials("basic");
		util.assert(cred1.user == cred2.user, "users should be equals");
		util.assert(cred1.password == cred2.password, "password should be equals");
	}),
	tu.test("set basic credentials - with PasswordAuthentication", function() { 
		var PasswordAuthentication = java.net.PasswordAuthentication;
		var cred1 = new PasswordAuthentication("tu2", new java.lang.String("tp2").toCharArray());
		conf.setCredentials("basic", cred1);
		var cred2 = conf.getCredentials("basic");
		util.assert(cred2.user == "tu2", "users should be equals");
		util.assert(cred2.password == "tp2", "password should be equals");
	}),
	tu.test("set oauth credentials - with object", function() { 
		var cred1 = {key: "testkey", secret: "testsecret"};
		conf.setCredentials("oauth", cred1);
		var cred2 = conf.getCredentials("oauth");
		util.assert(cred1.key == cred2.key, "key should be equals");
		util.assert(cred1.secret == cred2.secret, "secret should be equals");
	}),
	tu.test("set oauth credentials - with OAuthToken", function() { 
		var OAuthToken = org.openstreetmap.josm.data.oauth.OAuthToken;
		var token = new OAuthToken("tk1", "ts1");
		conf.setCredentials("oauth", token);
		var cred2 = conf.getCredentials("oauth");
		util.assert(cred2.key == "tk1", "key should be equals");
		util.assert(cred2.secret == "ts1" , "secret should be equals");
	}),
	
	tu.test("unsupported authentication methods", function() {
		var cred = {user: "testuser", password: "testpassword"};
		tu.expectAssertionError("null authMethod", function() {
			conf.setCredentials(null,cred);
		});
		tu.expectAssertionError("undefined authMethod", function() {
			conf.setCredentials(undefined,cred);
		});
		tu.expectAssertionError("null authMethod", function() {
			conf.setCredentials("nosuchmethod",cred);
		});
	})	
	
).run();