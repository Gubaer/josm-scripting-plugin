/**
 * Resolves links to JOSM classes and to JS objects provided by the Scripting
 * Plugin.
 */
(function() {

var JOSM_DOC_URL = "http://josm.openstreetmap.de/doc/";

/**
 * Resolves references to JOSM classes. 
 */
function resolveJOSMClassLinks() {
	function resolve(cls) {
		return JOSM_DOC_URL + cls.replace(/\./g, "/") + ".html";
	};
	
	$("a[data-josm-class]").each(function() {
		$(this).attr("href", resolve($(this).attr("data-josm-class")));
		$(this).attr("target", "externaldoc");
		$(this).attr("title", $(this).attr("data-josm-class"));
	});
};

/**
 * Resolves references to plugin objects
 */
function resolvePluginObjectLinks() {
	function resolve(kind, name) {
		var dirs = {"class": "classes", "mixin": "mixins", "module": "modules", namespace: "namespaces"};
		var ret = dirs[kind] + "/" + name.replace(/[^a-zA-Z0-9-_$]/g, "_") + ".html";
		return "js-doc/" + ret;		
	};
	
	function makeTitle(kind, name) {
		return kind + " " + name;
	};

	$("a[data-js-object]").each(function() {
		var obj = $(this).attr("data-js-object");
		var components = obj.split(":");
		var kind = components[0],
		    name = components[1];
		$(this).attr("href", resolve(kind, name));
		$(this).attr("title", resolve(kind,name));		
	});
};

$(document).ready(function() {
	resolveJOSMClassLinks();
	resolvePluginObjectLinks();
});

}());
