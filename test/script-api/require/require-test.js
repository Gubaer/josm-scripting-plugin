/**
 * Runs this test using Rhino. 
 * 
 * Example:
 *   java -cp $RHINO_JAR \
 *        -Djosm-scripting-plugin.home=/full/path/to/scripting/plugin/project \
 *        org.mozilla.javascript.tools.shell.Main require-test.js
 */

var File = java.io.File;
var URL  = java.net.URL;

PROJECT_HOME=java.lang.System.getProperty("josm-scripting-plugin.home");
if (PROJECT_HOME == null) {
	print("ERROR: system property 'josm-scripting-plugin.home' not set.");
	print("Invoke rhino with the -Djosm-scripting-plugin.home=full/path/to/local/project");
	java.lang.System.exit(1);
} 
print("INFO: PROJECT_HOME is " + PROJECT_HOME);

var projectPath = function(file) {
	return new java.io.File(PROJECT_HOME, file).getCanonicalPath();
}

var fload = function(file) {
	var f = new File(file);
	if (! f.exists() || f.isDirectory() || ! f.canRead()) {
		print("Can't load file '" + file + "'. Aborting.");
		java.lang.System.exit(1);
	}
	load(file);
}

fload(projectPath("javascript/require.js"));
fload(projectPath("test/script-api/require/testlib.js"));

test("require an existing sample module", function() {
	require.resetRepositories();
	var path=projectPath("test/script-api/require");
	require.addRepository(new File(path));
	var module = require("sampleModul1");
	assertDefined(module);	
	assert(module.message == "Hello World!");
});

test("require a module with syntax errors", function() {
	require.resetRepositories();
	var path=projectPath("test/script-api/require");
	require.addRepository(new File(path));
	try {
		var module = require("modulWithSyntaxError");
		fail("should have thrown an exception");
	} catch(e) {
		// OK 
	}
});

runtests();