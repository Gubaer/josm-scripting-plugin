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
	
	var safeHtmlFilename = require("viewhelper").safeHtmlFilename;

	function publishDoclet(doclet, config) {
		var filepath = path(opts.destination, config.path, safeHtmlFilename(doclet.name));
		mkdirs(filepath, true /* for parent */);
		var fragment = view.render(config.template, {
			doclet: doclet,
		    data: data
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
		    data: data
		});
		out.println("TOC: writing to <" + filepath + ">");
		fs.writeFileSync(filepath, fragment, "utf8");
	};

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
