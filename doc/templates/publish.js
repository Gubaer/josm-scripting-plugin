/**
 * publish
 *
 */

var fs   = require("fs-extra"),
    path = require("path"),
    template = require('jsdoc/template'),
    helper = require('jsdoc/util/templateHelper'),
    ViewHelper = require("viewhelper").ViewHelper,
    safeHtmlFilename = require("viewhelper").safeHtmlFilename;

/**
 *
 * @global
 * @param {TAFFY} data - A TaffyDB collection representing
 *                       all the symbols documented in your code.
 * @param {object} opts - An object with options information.
 */
exports.publish = function(data, opts, tutorials) {

    function publishDoclet(doclet, config) {
        var dir = path.join(
                opts.destination,
                config.path
        );
        fs.ensureDirSync(dir);
        var file = path.join(dir, safeHtmlFilename(doclet.name));
        var fragment = view.render(config.template, {
            doclet: doclet,
            data: data,
            ViewHelper: ViewHelper
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
        console.log(config.title + " <" + doclet.name + ">: writing to <" 
                + file + ">");
        fs.writeFileSync(file, html, "utf8");
    };

    function publishTOC() {
        var filepath = path.join(opts.destination, "apitoc.html");
        fs.ensureDirSync(opts.destination);
        var fragment = view.render("toc.tmpl", {
            data: data,
            safeHtmlFilename: safeHtmlFilename
        });
        console.log("TOC: writing to <" + filepath + ">");
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
