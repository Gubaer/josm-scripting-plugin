

var URL_PREFIXES = {
	"org.openstreetmap.josm": "http://josm.openstreetmap.de/doc/",
	"java."                 : "http://docs.oracle.com/javase/6/docs/api/",
	"javax.swing."          : "http://docs.oracle.com/javase/6/docs/api/"
 };


function safeHtmlFilename(fn) {
	return fn.replace(/[^a-zA-Z0-9$\-_\.]/g, "_") + ".html";
}

function docPrefix(type) {
	 for (var prefix in URL_PREFIXES) {
		 if (type.indexOf(prefix) == 0) return URL_PREFIXES[prefix];
	 }
	 return undefined;
 }

function resolveClassReferences(str) {
	var self = this;
	if (str == null || str === void 0) return "";
 	str = str.replace(/(?:\[(.+?)\])?\{@class +(.+?)\}/gi,
	    function(match, content, longname) {
			  var fqclassname = longname.replace(/\//g, ".");  
			  var classname = fqclassname.replace(/.*\.([^\.]+)$/, "$1"); 
			  content = content || classname;
			  return self.resolveTypes(fqclassname, content);
	    }
     );
     return str;
};

function resolveTypes(type, content) {	
	 //var MessageFormat = java.text.MessageFormat;
    var util = require("util");
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
			 //type = MessageFormat.format("<a href=''{0}'' alt=''{1}'' target=''javadoc''>{2}</a>", url, type, content)
			type = util.format("<a href='%s' alt='%s' target='javadoc'>%s</a>", url, type, content)
		 } else {
			 var res = this.data({name: type});
			 if (res.count() < 1) return type;
			 content  = content || type;
			 if (res.first().kind == "class") {
				 //return MessageFormat.format("<a href=''../classes/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 return util.format("<a href='../classes/%s' alt='%s'>%s</a>", safeHtmlFilename(type), type, content);
			 } else if (res.first().kind == "mixin") {
				 //return MessageFormat.format("<a href=''../mixins/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 return util.format("<a href='../mixins/%s' alt='%s'>%s</a>", safeHtmlFilename(type), type, content);
			 } else if (res.first().kind == "module") {
				 //return MessageFormat.format("<a href=''../modules/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 return util.format("<a href='../modules/%s' alt='%s'>%s</a>", safeHtmlFilename(type), type, content);
			 } else if (res.first().kind == "namespace") {
				 //return MessageFormat.format("<a href=''../namespaces/{0}'' alt=''{1}''>{2}</a>", safeHtmlFilename(type), type, content);
				 return util.format("<a href='../namespaces/%s' alt='%s'>%s</a>", safeHtmlFilename(type), type, content);
			 }  else {
				 return type;
			 }
		 }
		 resolved.push(type);		 
	 }
	 return resolved.join("|"); 
}


/**
 * Builds the list of property links as <a ...> elements
 */
function buildPropertyLinks(doclet) {
   var name = doclet.name;
   var names = propertyNames(doclet);

   var links = [];
   for (var i=0; i<names.length; i++) {
      links.push("<a href='#" + name + "'>" + names[i] + "</a>");
   }
   return links.join(", ");
}

/**
 * Builds the property summary from the @summary tag; resolves links
 * in the tag value.
 */
function buildPropertySummary(doclet) {
    var summary = doclet.summary || "";
    return this.resolveClassReferences(summary)
}

/**
 * Creates the list of doclet names including the canonical name and
 * a list of optional aliases
 */
function propertyNames(doclet) {
   var names = [];
   names.push(doclet.name);
   if (doclet.alias) {
     for (var ai = 0; ai < doclet.alias.length; ai++) {
       names.push(doclet.alias[ai]);
     }
   }
   return names;
}
/**
 * Build the title string for a doc page of a type (a class, a mixin,
 * a namespace, or a module).
 * 
 * @param doclet the doclet representing the type 
 * @returns {String}
 */
function buildTitleForType(doclet) {
	if (doclet.kind == "class") return "Class " + doclet.name
	else if (doclet.kind == "mixin") return "Mixin " + doclet.name
	else if (doclet.kind == "namespace") return "Namespace " + doclet.name
	else if (doclet.kind == "module") return "Module " + doclet.name
	else return "";
}

function ViewHelper(data) {
	this.data = data; 
}

ViewHelper.prototype.resolveClassReferences = resolveClassReferences;
ViewHelper.prototype.resolveTypes = resolveTypes;
ViewHelper.prototype.buildPropertySummary = buildPropertySummary;
ViewHelper.prototype.buildPropertyLinks = buildPropertyLinks;
ViewHelper.prototype.buildTitleForType = buildTitleForType;

exports.ViewHelper = ViewHelper;
exports.safeHtmlFilename = safeHtmlFilename;