/**
 * publish 
 * 
 */

var fs       = require("fs"),
    template = require('jsdoc/template'),
    helper = require('jsdoc/util/templateHelper');

var out = java.lang.System.out;
	
var path = function(components){
	var File = java.io.File;
	switch(arguments.length) {
	case 0: return undefined;
	case 1: return arguments[0];
	default: 
		var file = new File(arguments[0]);
		for (var i=1; i< arguments.length; i++) file = new File(file, arguments[i]);
		return file.toString();
	}
};

function safeHtmlFilename(fn) {
	return fn.replace(/[^a-zA-Z0-9$\-_\.]/g, "_") + ".html";
};

function isString(value) {
	return typeof value === "string" || value instanceof String;
}

function mkdirs(file, parent) {
	var File = java.io.File;
	var f;
	if (isString(file)) {
		f = new File(file);
	} else if (file instanceof File) {
		f = file; 
	} else {
		f = new File(String(file));
	}
	if (!!parent) {
		f = f.getParentFile();
	}
	f.mkdirs();
};
 

/**
 * 
 * @global
 * @param {TAFFY} data - A TaffyDB collection representing
 *                       all the symbols documented in your code.
 * @param {object} opts - An object with options information.
 */
exports.publish = function(data, opts, tutorials) {
		
	function resolveTypes(type, content) {
		 var URL_PREFIXES = {
			"org.openstreetmap.josm": "http://josm.openstreetmap.de/doc/",
			"java."                 : "http://docs.oracle.com/javase/6/docs/api/",
			"javax.swing."          : "http://docs.oracle.com/javase/6/docs/api/"
		 };
		 function docPrefix(type) {
			 for (var prefix in URL_PREFIXES) {
				 if (type.indexOf(prefix) == 0) return URL_PREFIXES[prefix];
			 }
			 return undefined;
		 }
		 var MessageFormat = java.text.MessageFormat;
		 if (type == null || type == undefined) return type;
		 var types = String(type).split(",");
		 var resolved = [];
		 for (var i=0; i< types.length; i++){
			 var type = types[i];
			 type = type.replace(/^\s+/,"").replace(/\+$/, ""); // trim
			 var prefix = docPrefix(type);
			 if (prefix) {
				 var classname = type.replace(/.*\.([^\.]+)$/, "$1"); 
				 var url = prefix + type.replace(/\./g, "/") + ".html";
				 content = content || classname;
				 type = MessageFormat.format("<a href=''{0}'' alt=''{1}'' target=''javadoc''>{2}</a>", url, type, content)
			 } else {
				 var res = data().get({name: type});
				 if (res.length < 1) return type;
				 content  = content || type;
				 if (res[0].kind == "class") {
					 return MessageFormat.format("<a href=''../classes/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 } else if (res[0].kind == "mixin") {
					 return MessageFormat.format("<a href=''../mixins/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 } else if (res[0].kind == "module") {
					 return MessageFormat.format("<a href=''../modules/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 } else if (res[0].kind == "namespace") {
					 return MessageFormat.format("<a href=''../namespaces/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 }  else {
					 return type;
				 }
			 }
			 resolved.push(type);		 
		 }
		 return resolved.join("|"); 
	 };	
 
	 function resolveClassReferences(str) {
		if (str == null || str === void 0) return "";
	    var MessageFormat = java.text.MessageFormat;
	 	str = str.replace(/(?:\[(.+?)\])?\{@class +(.+?)\}/gi,
		    function(match, content, longname) {
				  var fqclassname = longname.replace(/\//g, ".");  
				  var classname = fqclassname.replace(/.*\.([^\.]+)$/, "$1"); 
				  content = content || classname;
				  return resolveTypes(fqclassname, content);
		    }
	     );
	     return str;
	  };
  
	var viewHelper = {
		resolveTypes: resolveTypes,
		resolveClassReferences: resolveClassReferences
	};

	function publishDoclet(doclet, config) {
		var filepath = path(opts.destination, config.path, safeHtmlFilename(doclet.name));
		mkdirs(filepath, true /* for parent */);
		var fragment = view.render(config.template, {
			doclet: doclet,
		    data: data,
		    viewHelper: viewHelper,
		    title: config.title + " " + doclet.name
		});
		var html = view.render('page.tmpl', {
			title: config.title + " " + doclet.name,
			paths: {
				stylesheets: "../../stylesheets/",
				javascripts: "../../javascript/"
			},
			body: fragment,
			showHeader: false
		});
		out.println(config.title + " <" + doclet.name + ">: writing to <" + filepath + ">");
		fs.writeFileSync(filepath, html, "utf8");
	};

	function publishTOC() {
		var filepath = path(opts.destination, "apitoc.html");
		mkdirs(filepath, true /* for parent */);
		var fragment = view.render("toc.tmpl", {
		    data: data,
		    viewHelper: viewHelper
		});
		out.println("TOC: writing to <" + filepath + ">");
		fs.writeFileSync(filepath, fragment, "utf8");
	};

	function dump(obj) {
		if (!obj) return;
		out.println("***********************************************");
		for (var p in obj) {
			if (! obj.hasOwnProperty(p)) continue;
			out.println(p + "-->" + obj[p]);
		}
	}

	var view = new template.Template(opts.template + "/tmpl");

	data({kind: "mixin"}).each(function(mixin) {
		publishDoclet(mixin, {
			title: "Mixin",
			path: "mixins",
			template: "type.tmpl"
		});
	});	   
	
	data({kind: "class"}).each(function(cls) {
		if (cls.name == cls.memberof) return;
		publishDoclet(cls, {
			title: "Class",
			path: "classes",
			template: 'type.tmpl'
		});
	});

	data({kind: "namespace"}).each(function(cls) {
		publishDoclet(cls, {
			title: "Namespace ",
			path: "namespaces",
			template: 'type.tmpl'
		});
	});	   

	data({kind: "module"}).each(function(module) {
		publishDoclet(module, {
			title: "Module ",
			path: "modules",
			template: 'module.tmpl'
		});;
	});	   
	
	publishTOC();

};
