/** 

 * A plugin for the jsdoc3 documentation generator.
 * 
 * See https://github.com/jsdoc3/jsdoc/tree/master/plugins
 */
java.lang.System.out.println("CustomTagsPlugin: loading ...");
exports.defineTags = function(dictionary) {
	java.lang.System.out.println("CustomTagsPlugin: defining tag 'forclass' ...");
	dictionary.defineTag('forclass', {
		mustHaveValue: true,
	    onTagged: function(doclet, tag) {
	    	// just remember the tag
	        doclet.forclass = tag.value; 
	    }
	});
	java.lang.System.out.println("CustomTagsPlugin: defining tag 'alias' ...");
	dictionary.defineTag('alias', {
		mustHaveValue: true,
	    onTagged: function(doclet, tag) {
	    	doclet.alias = doclet.alias || [];
	    	doclet.alias.push(tag.value);
	    }
	});
};

