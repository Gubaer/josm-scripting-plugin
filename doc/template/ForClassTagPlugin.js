/** 

 * A plugin for the jsdoc3 documentation generator.
 * 
 * See https://github.com/jsdoc3/jsdoc/tree/master/plugins
 */
java.lang.System.out.println("ForClassTagPlugin: loading ...");
exports.defineTags = function(dictionary) {
	java.lang.System.out.println("ForClassTagPlugin: defining tag 'forclass' ...");
	dictionary.defineTag('forclass', {
		mustHaveValue: true,
	    onTagged: function(doclet, tag) {
	    	// just remember the tag
	        doclet.forclass = tag.value; 
	    }
	});
};

