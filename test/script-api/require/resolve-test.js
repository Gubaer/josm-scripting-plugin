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


test("id must not be null", function() {
	try {
		require.resolve(null);
		fail("Expected exception, didn't get one");
	} catch(e) {
		// OK
	}
});

test("id must be defined", function() {
	try {
		require.resolve(void(0));
		fail("Expected exception, didn't get one");
	} catch(e) {
		// OK
	}
});

test("property 'repositories' present", function() {
	if (!require.hasOwnProperty("repositories")) {
		fail("Didn't find expected property 'repositories'");
	}
});

test("add null or undefined as location", function() {
	require.addRepository(null);
	require.addRepository(void(0));	
});

var assertRepository = function(pattern) {
	var repo = require.repositories;
	for(var i=0; i < repo.length; i++) {
		if (repo[i].toString().indexOf(pattern) != -1) return;
	}
	fail("Expected repository matching pattern '" + pattern + "' - nothing found");
};

test("add local path as repository", function() {
	require.addRepository("/home/test/apath");
	assertRepository("/home/test/apath");
});

test("add local jar file as repository", function() {
	var JarFile = java.util.jar.JarFile;
	var jar = projectPath("dist/scripting.jar");
	require.addRepository(new JarFile(jar));
	assertRepository(jar);
});

test("add local zip file as repository", function() {
	var ZipFile = java.util.zip.ZipFile;
	var jar = projectPath("dist/scripting.jar");
	require.addRepository(new ZipFile(jar));
	assertRepository(jar);
});

test("add local dir as repository (given a file)", function() {
	require.addRepository(new File("/home/test/yetanotherpath"));
	assertRepository("/home/test/yetanotherpath");
});

test("require an existing sample module", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("sampleModul1");
	assertDefined(url, "resolve should have resulted in a defined URL");	
});

test("require an existing sample module - leading and trailing white space", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("  sampleModul1  ");
	assertDefined(url, "resolve should have resulted in a defined URL");	
});

test("require an existing sample module - leading multiple slashes", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("///sampleModul1");
	assertDefined(url, "resolve should have resulted in a defined URL");	
});


test("require an non-existing sample module", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("noSuchUrl");
	assertUndefined(url, "resolve should have resulted in an undefined URL");	
});

test("require an existing sample module - sub module", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("sub/subsub/subModul2");
	assertDefined(url, "resolve should have resulted in a defined URL");
});

test("require an existing sample module - sub module, using backslashes", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("sub\\subsub\\subModul2");
	assertDefined(url, "resolve should have resulted in a defined URL");
});

test("require an existing sample module - sub module, using mixed slashes", function() {
	require.resetRepositories();
	require.addRepository(projectPath("test/script-api/require"));
	var url = require.resolve("sub//\\subsub\\///subModul2");
	assertDefined(url, "resolve should have resulted in a defined URL");
});


test("require an existing sample module from a jar", function() {
	require.resetRepositories();
	var jar  = projectPath("test/script-api/require/modules.jar");
	require.addRepository(new URL("jar:file:" + jar + "!/jarsub"));
	var url = require.resolve("jarModule");
	assertDefined(url, "resolve should have resulted in a defined URL");
});


test("require from an non existing jar", function() {
	require.resetRepositories();
	var jar  = projectPath("test/script-api/require/nosuchjar.jar");
	require.addRepository(new URL("jar:file:" + jar + "!/jarsub"));
	var url = require.resolve("jarModule");
	assertUndefined(url, "should not have found the jar");
});

test("require from an existing jar - module doesn't exist", function() {
	require.resetRepositories();
	var jar  = projectPath("test/script-api/require/modules.jar");
	require.addRepository(new URL("jar:file:" + jar + "!/nosuchdir"));
	var url = require.resolve("jarModule");
	assertUndefined(url, "should not have found the jar");
});


runtests();



