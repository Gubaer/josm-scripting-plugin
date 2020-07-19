'use strict';
/**
 * A plugin for the jsdoc3 documentation generator.
 *
 * See https://github.com/jsdoc3/jsdoc/tree/master/plugins
 */
console.log("CustomTagsPlugin: loading ...");
exports.defineTags = function(dictionary) {
    console.log("CustomTagsPlugin: defining tag 'forclass' ...");
    dictionary.defineTag('forclass', {
        mustHaveValue: true,
        onTagged: function(doclet, tag) {
            // just remember the tag
            doclet.forclass = tag.value;
        }
    });
    console.log("CustomTagsPlugin: defining tag 'alias' ...");
    dictionary.defineTag('alias', {
        mustHaveValue: true,
        onTagged: function(doclet, tag) {
            doclet.alias = doclet.alias || [];
            doclet.alias.push(tag.value);
        }
    });
};

