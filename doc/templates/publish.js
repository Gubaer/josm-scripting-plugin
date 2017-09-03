'use strict';
/**
 * publish
 *
 */

const fs   = require("fs-extra"),
    path = require("path"),
    template = require('jsdoc/template'),
    helper = require('jsdoc/util/templateHelper'),
    ViewHelper = require("viewhelper").ViewHelper,
    safeHtmlFilename = require("viewhelper").safeHtmlFilename;

/**
 *
 * @param {TAFFY} data - A TaffyDB collection representing
 *                       all the symbols documented in your code.
 * @param {object} opts - An object with options information.
 */
exports.publish = function(data, opts, tutorials) {

    function publishDoclet(doclet, config) {
        let dir = path.join(
                opts.destination,
                config.path
        );
        fs.ensureDirSync(dir);
        let file = path.join(dir, safeHtmlFilename(doclet.name));
        let fragment = view.render(config.template, {
            doclet: doclet,
            data: data,
            ViewHelper: ViewHelper
        });
        let html = view.render('page.tmpl', {
            title: config.title + " " + doclet.name,
            paths: {
                stylesheets: "../../stylesheets/",
                javascripts: "../../javascript/"
            },
            body: fragment,
            showHeader: false,
        });
        console.log("%s <%s>: writing to <%s>", config.title, doclet.name, file);
        fs.writeFileSync(file, html, "utf8");
    };

    function publishTOC() {
        let filepath = path.join(opts.destination, "apitoc.html");
        fs.ensureDirSync(opts.destination);
        let fragment = view.render("toc.tmpl", {
            data: data,
            safeHtmlFilename: safeHtmlFilename
        });
        console.log("TOC: writing to <%s>", filepath);
        fs.writeFileSync(filepath, fragment, "utf8");
    };

    let view = new template.Template(opts.template + "/tmpl");

    data({kind: "mixin"}).each(function(mixin) {
        publishDoclet(mixin, {
            title: "Mixin",
            path: "mixins",
            template: "type.tmpl",
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
