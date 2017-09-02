'use strict';

const URL_PREFIXES = [
    {prefix: "org.openstreetmap.josm", url: "http://josm.openstreetmap.de/doc/"},
    {prefix: "java."                 , url: "http://docs.oracle.com/javase/6/docs/api/"},
    {prefix: "javax.swing."          , url:"http://docs.oracle.com/javase/6/docs/api/"}
];

const util = require("util");

function safeHtmlFilename(fn) {
    return fn.replace(/[^a-zA-Z0-9$\-_\.]/g, "_") + ".html";
}

function matchingUrlPrefixForType(type) {
    return URL_PREFIXES.find(function(urlPrefix) {
        type.indexOf(urlPrefix.prefix) == 0
    });
}

function resolveClassReferences(str) {
    let self = this;
    if (str == null || str === void 0) return "";
    return str.replace(/(?:\[(.+?)\])?\{@class +(.+?)\}/gi,
        function(match, content, longname) {
            let fqclassname = longname.replace(/\//g, ".");
            let classname = fqclassname.replace(/.*\.([^\.]+)$/, "$1");
            content = content || classname;
            return self.resolveTypes(fqclassname, content);
        }
    );
}

function resolveTypes(type, content) {
    let self = this;
    let util = require("util");
    if (type == null || type == undefined) return type;
    let types = String(type).split(",");
    let resolved = [];
    types.forEach(function(type) {
        type = type.trim();
        let urlPrefix = matchingUrlPrefixForType(type);
        if (urlPrefix) {
            let classname = type.replace(/.*\.([^\.]+)$/, "$1");
            content = content || classname;
            type = util.format("<a href='%s' alt='%s' target='javadoc'>%s</a>",
                    url, type, content)
        } else {
            let res = self.data({name: type});
            if (res.count() < 1) return type;
            content  = content || type;
            switch(res.first().kind) {
                case "class":                
                    return util.format("<a href='../classes/%s' alt='%s'>%s</a>",
                         safeHtmlFilename(type), type, content);
                case "mixin":
                    return util.format("<a href='../mixins/%s' alt='%s'>%s</a>",
                         safeHtmlFilename(type), type, content);
                case "module":
                    return util.format("<a href='../modules/%s' alt='%s'>%s</a>",
                         safeHtmlFilename(type), type, content);
                case "namespace":
                    return util.format("<a href='../namespaces/%s' alt='%s'>%s</a>",
                         safeHtmlFilename(type), type, content);
                default:
                    return type;
            }
        }
        resolved.push(type);
    });
    return resolved.join("|");
}

/**
 * Builds the list of property links as <a ...> elements
 */
function buildPropertyLinks(doclet) {
    return propertyNames(doclet).map(function(propertyName) {
        return util.format("<a href='#%s'>%s</a>", doclet.name, propertyName);
    }).join(", ");
}

/**
 * Builds the property summary from the @summary tag; resolves links
 * in the tag value.
 */
function buildPropertySummary(doclet) {
    return this.resolveClassReferences(doclet.summary || "")
}

/**
 * Creates the list of doclet names including the canonical name and
 * a list of optional aliases
 */
function propertyNames(doclet) {
    let names = [];
    names.push(doclet.name);
    if (doclet.alias) {
        names = names.concat(doclet.alias);
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
    switch(doclet.kind) {
        case "class": return "Class " + doclet.name;
        case "mixin": return "Mixin " + doclet.name;
        case "namespace": return "Namespace " + doclet.name;
        case "module": return "Module " + doclet.name;
        default: return "";        
    }
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