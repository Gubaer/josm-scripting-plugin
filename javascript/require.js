/**
 * @file This is an implementation of a CommonJS module loader for JOSM.
 */
(function(global) {
	
	// -- java imports
	var URL = java.net.URL;
	var JarFile = java.util.jar.JarFile;
	var ZipFile = java.util.zip.ZipFile;
    var StringBuffer = java.lang.StringBuffer;
    var BufferedReader = java.io.BufferedReader;
    var InputStreamReader = java.io.InputStreamReader;
    var File = java.io.File;
    var MessageFormat = java.text.MessageFormat;
    var StringBuilder = java.lang.StringBuilder;
    var InputStreamReader = java.io.InputStreamReader;
    var BufferedReader = java.io.BufferedReader;
	 
    var throwRequireError = function(message, cause) {
    	var error = new Error();
    	error.name = "RequireError";
    	error.message = message;
    	if (isSomething(cause)) {
    		error.cause = cause;
    	}
    	throw error;
    };
    
    var isUndefined = function(val) {
    	return typeof val === "undefined";
    };
    
    var isSomething = function(val) { 
    	return val !== null && !isUndefined(val);
    };
    
    var isString = function(val) {
    	return isSomething(val) && (typeof val === "string" || val instanceof String);
    };
    
    var trim = function(s) {
    	return s.replace(/^\s+/, "").replace(/\s+$/, "");
    };
    
    var endsWith = function(s, suffix) {
    	return s.indexOf(suffix, s.length - suffix.length) !== -1;
    };
    
    var debuglevel = 10;
    var debug = function(msg, level) {
    	if (level === void(0)) level = 10;
    	if (debuglevel != 0 && level <= debuglevel) {
    		java.lang.System.out.println("DEBUG: require: " + msg);
    	};
    };
    
    var msg = function() {    	
    	switch(arguments.length) {
    	case 0: return "";
    	case 1: return arguments[0] + "";
    	default: return MessageFormat.format(arguments[0] + "", Array.prototype.slice.call(arguments,1));
    	}
    };
        
	/**
	 * Represents a repository of CommonJS modules whose location is given by
	 * an URL. Only file and jar-URLs are supported.
	 * 
	 * @class	
	 * @param {java.net.URL} url  the URL
	 */
    var ModuleRepository = function(location) {
    	if (!isSomething(location)) throwRequireError(msg("Expected defined non-null location, got {0}", location));
    	if (location instanceof java.io.File) {
    		location = location.getCanonicalFile().toURI().toURL();
    	} else if (location instanceof java.net.URI) {
    		location = location.toURL();
    		if (! (location.getProtocol() == "file" || location.getProtocol() == "jar")) {
    			throwRequireError(msg("Only ''file'' and ''jar'' URLs supported as URL protocol, got ''{0}''", location.getProtocol()));
    		}
    	} else if (location instanceof java.net.URL) {
    		if (! (location.getProtocol() == "file" || location.getProtocol() == "jar")) {
    			throwRequireError(msg("Only ''file'' and ''jar'' URLs supported as URL protocol, got '''{0}'''", location.getProtocol()));
    		}
    	} else {
    		throwRequireError(msg("Unsupported type of location, got {0}", location));
    	}
		this.url = location;
	};
	
	/**
	 * Replies the URL where the module with id <code>id</code> can be read from;
	 * undefined, if the URL can't be built or if no such resource is available
	 * in the repository.
	 * 
	 * @param {String} id the module id 
	 * @return {java.net.URL} the URL or undefined 
	 */
	ModuleRepository.prototype.lookup = function(id) { 		
		var url = this.url;
		if (url.getProtocol() == "file") { 			
			var repo, file;
			try {
				repo = new File(url.toURI());
			} catch(e) {
				repo = new File(url.getPath());
			}
			var lookup = function(filename) {
				var file = new File(repo, filename);
				if (file.exists()) {
					debug(msg("module ''{0}'' found in repository ''{1}''. Module file is ''{2}''.", id, url.toString(), file.toString()));
					return file.toURI().toURL();
				} else {
					debug(msg("module ''{0}'' not found in repository ''{1}''. Failed to lookup module file ''{2}''.", id, url.toString(), file.toString()));
					return void(0);
				}
			};			
			var file;
			if (file =  lookup(id)) return file.toURI().toURL();			
			if (! endsWith(id, ".js")) {
	    		if (file = lookup(id + ".js")) return file.toURI().toURL();
			}
			return void(0);    		
		} else if (this.url.getProtocol() == "jar") {
			var s = url.toString();
			var i = s.indexOf("!");
			var jarpath;
			if (i != -1) {				
				jarpath = s.substring(i+1).replace("\/+$","");
				s = s.substring(0,i);
			}
			
			var entry = jarpath + "/" + id;
			var moduleurl = new URL(s + "!" + entry);
			var con = moduleurl.openConnection();
			var module;
			try {
				var jar = con.getJarFile();				
				var entry = jar.getJarEntry(entry);
				if (entry != null) {
					debug(msg("module ''{0}'' found in jar ''{1}''. Module url is ''{2}''.", id, jar.getName(), moduleurl.toString()));
					return moduleurl;
				} 
				debug(msg("module ''{0}'' not found in jar ''{1}''. Module url is ''{2}''.", id, jar.getName(), moduleurl.toString()));					
			} catch(e) {
				debug(msg("module ''{0}'': jar file for module repository ''{1}'' not found.", id, moduleurl.toString()));
			}
			if (! endsWith(id, ".js")) {	
				entry = jarpath + "/" + id + ".js";
	    		moduleurl = new URL(s + "!" + entry);
	    		con = moduleurl.openConnection();
	    		try {
	    			var jar = con.getJarFile();
	    			var entry = jar.getJarEntry(entry);
	    			if (entry != null) {
						debug(msg("module ''{0}'' found in jar ''{1}''. Module url is ''{2}''.", id, jar.getName(), moduleurl.toString()));
						return moduleurl;
					} 
	    			debug(msg("module ''{0}'' not found in jar ''{1}''. Module url is ''{2}''.", id, jar.getName(), moduleurl.toString()));
	    			return moduleurl;
	    		} catch(e) {
	    			debug(msg("module ''{0}'': jar file for module repository ''{1}'' not found.", id, moduleurl.toString()));
	    		}
			}
			return void(0);
		}    	
		return void(0);
	};
	
    /**
     * the list of URLs referring to locations where modules can be loaded from.   
     */
    var repositories;
    var resetRepositories = function() {
    	repositories = [];
    	repositories.push(new ModuleRepository(new File(".")));
    };
    resetRepositories();
	
    
    /**
     * the cache of already loaded modules 
     */ 
	var moduleCache = {};
       
	var readFromUrl = function(url) {		
		var br = new BufferedReader(
			new InputStreamReader(url.openStream(), "UTF8")
		);
		try {
			var sb = new StringBuilder();
			var line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		} finally {
			if (br != null) br.close();
		}
	};

	/**
	 * Load a CommonJS module with id <code>id</code>.
	 * 
	 * @function
	 * @name require
	 * @param {String} id  the module id. Expected a non-null, defined string. 
	 * @return the loaded module 
	 * @throws RequireError  thrown if the module can't be loaded
	 * @example
     *  // load the josm/util module
     *  var util = require("josm/util");
	 * 
	 */
    var require = global.require = function(id) { 
    	if (! isString(id)) {
        	throwRequireError(msg("Expected a module id of type string, got {0}", id));
        }
        
        var moduleContent = '';
        var moduleUrl;
        
        moduleUrl = require.resolve(id);
        if (isUndefined(moduleUrl)) {
        	throwRequireError(msg("Failed to locate module with id ''{0}''", id));        	
        }
        try {
        	moduleContent = readFromUrl(moduleUrl);
        } catch(e) {
        	debug("got e: " + e.toSource());
        	var m = msg("Failed to read module content for module ''{0}'' from url ''{1}''", id, moduleUrl.toString());
        	if (e.hasOwnProperty("message") && typeof e.message !== "undefined") {
        		m += "\n" + e.message;
        	}        			
        	throwRequireError(m);
        }

        try {
        	var normid = normalizeModuleId(id);
            var exports = moduleCache[normid] || {};
            var module = { id: normid, uri: moduleUrl, exports: exports};
            var f = new Function("require", "exports", "module", moduleContent);
            f.call({}, require, exports, module);
            exports = module.exports || exports;
            moduleCache[id] = exports;
            return exports;
        } catch(e) {
        	var m;
        	if (e.name == "SyntaxError") {
        		m = msg("Failed to load module ''{0}''. Syntax error on line {1} in file ''{2}''", normid, e.lineNumber, moduleUrl.toString());
        		m += msg("\nError message: {0}", e.message);        		
         	} else {
         		m = msg("Failed to load module ''{0}'' from URL ''{1}''. Details: ''{2}''", normid, moduleUrl.toString(), e.toSource());
         	}
        	throwRequireError(m, e);
        }
    };
    
    /** 
     * Given a module id, try to resolve its location and reply it as an URL
     * 
     * @function
     * @name resolve
     * @memberOf require
     * @param {String} id  the module id 
     * @return the location of the module as URL, or <code>undefined</code>
     * 
     * @example
     *  var url = require.resolve("josm/util");
     * 
     */
    require.resolve = function(id) {
    	if (!isSomething(id)) return void(0);
    	if (!isString(id))  id += ""; // convert to a string 
    	id = normalizeModuleId(id);
    	if (id == "") return void(0);    	
    	for(var i=0; i < repositories.length; i++) {
    		var url = repositories[i].lookup(id);
    		debug(msg("==> Loooking up module ''{0}'' in repository ''{1}''", id, repositories[i].url.toString()));
    		if (url) return url;
    	}
    	return void(0);
    };
    
    
    /**
     * <p>Resets the list of module repositories.</p>
     */
    require.resetRepositories = function() {
    	resetRepositories();
    };
  
   
    /**
     * <p>Adds a new repository to the list of repositories where CommonJS modules
     * are loaded from.</p>
     * 
     * <code>rep</code> is either
     * <dl>
     *   <dt>a string</dt>
     *   <dd>denoting a path in the local filesystem</dd>
     *
     *   <dt>a java.io.File</dt>
     *   <dd>denoting a path in the local filesystem</dd>
     *   
     *   <dt>a java.util.zip.ZipFile</dt>
     *   <dd>denoting a zip (or jar file) including CommonJS modules</dd>
     *   
     *   <dt>a java.net.URL</dt>
     *   <dd>denoting a <code>file:</code> or <code>jar:</code> url</dd>
     * </dl>
     * 
     * 
     * @param {string|java.io.File|java.net.URL} rep  the repository. Ignored if null or undefined. 
     * @throws RequireError 
     */
    require.addRepository = function(rep) {
    	if (!isSomething(rep)) return;
    	if (isString(rep)) {
    		var url = new File(rep).getCanonicalFile().toURI().toURL();   
    		repositories.push(new ModuleRepository(url));    		
    	} else if (rep instanceof File) {
    		repositories.push(new ModuleRepository(rep.getCanonicalFile().toURI().toURL()));
    	} else if (rep instanceof ZipFile || rep instanceof JarFile) {
    		var s = rep.getName();
    		var furl = new File(rep.getName()).getCanonicalFile().toURI().toURL();
    		var url = new URL("jar:" + furl + "!/");
    		repositories.push(new ModuleRepository(url));    	
    	} else if (rep instanceof URL) {
    		switch(rep.getProtocol() + "") {
    		case "file":
    		case "jar":
    			repositories.push(new ModuleRepository(rep));
    			break;
    		default:
    			throwRequireError(msg("URLs for protocol ''{0}''' not supported as repository locations", rep.getProtocol()));
    		}
    	} else {
    		throwRequireError(msg("Unexpected type of repository location. Got {0}", rep));
    	}
    };
    
    /**
     * Removes the repository at index <code>idx</code> from the list of repositories.
     * 
     * @param {Number} idx the index
     */
    require.removeRepository = function(idx) {
    	if (typeof idx !== "number") return;
    	if (idx < 0 || idx >= repositories.length) return;
    	repositories.splice(idx, idx);
    };
    
    Object.defineProperty(require, "repositories", {
    	get: function() {
    		var urls = [];
    		for (var i=0; i< repositories.length; i++) {
    			urls.push(repositories[i].url);
    		}
    		return urls;
    	}
    });
    
    /**
     * Normalizes a module id. Removes leading and trailing white space, replaces \ by /, collapses
     * sequences of / and removes leading /, if present.
     * If id is null, replies null. if id is undefined, replies undefined. 
     * 
     * @param {String} id  the id. If null or undefined, replies undefined. Anything else than a string
     * is converted internally to a string.
     *  
     */
    var normalizeModuleId = function(id) {
    	if (! isSomething(id)) return void(0);
    	if (! isString(id)) id += ""; // convert to a string
    	return trim(id).replace(/\\/g, "/").replace(/\\/g, "/").replace(/\/+/g, "/").replace(/^\//, "");
    };
       
}(this));