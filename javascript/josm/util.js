goog.provide("josm.util");
goog.require("josm");

/**
 * <p>Provides a set of static utility functions.</p>
 * 
 * @namespace 
 * @name josm.util
 */
josm.util = function(my) {
	// -- imports 
	var MessageFormat = java.text.MessageFormat;
	
	/**
	 * Checks whether a value is null or undefined.
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name isNothing
	 * @param {Object} value  the value to check
	 * @type {Boolean}
	 * @return false, if <code>value</code> is null or undefined; true, otherwise  
	 */
	my.isNothing = function(value) {
		return value == null || value == undefined;
	};
	
	/**
	 * <p>Trims leading and trailing whitespace from <code>s</code>.</p> 
	 * 
	 * <p>Replies s, if s is null
	 * or undefined. Any other value is converted to a string, then leading and trailing white
	 * space is removed.</p>
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name trim
	 * @param {String} s  the string to be trimmed
	 * @type {String} 
	 */
	my.trim = function(s){
		if (s == null || s == undefined) return s;
		return String(s).replace(/^\s+|\s+$/, '');
	};
	
	/**
	 * <p>Assert a condition and throw an Error if the condition isn't met.</p>
	 * 
	 * <p><strong>Usage:</strong>
	 * <dl>
	 *   <dt><code>assert()</code></dt>
	 *   <dd>Does nothing</dd>
	 *   
	 *    <dt><code>assert(cond)</code></dt>
	 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an Error.</dd>
	 *    
	 *    <dt><code>assert(cond, msg)</code></dt>
	 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an Error, whose <code>description</code> property
	 *    is set to <code>msg</code>.</dd>
     *
     *	  <dt><code>assert(cond, msg, objs...)</code></dt>
	 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an Error, whose <code>description</code> property
	 *    is set to the formatted message <code>msg</code>. Internally uses <code>java.text.MessageFormat</code> to format the message.</dd>
     *
	 * </dl>
	 * 
	 * @example
	 * // throws an Error
	 * assert(false);                  
	 * 
	 * // throws an Error e, with e.description == "My message"
	 * assert(false, "My message");    
	 * 
	 * // throws an Error e, with e.description == "My message: test"
	 * assert(false, "My message: {0}", "test");    
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name assert
	 */
	my.assert = function() {
		switch(arguments.length) {
		case 0: 
			return;
		case 1:			
			if (arguments[0]) return;
			var error = new Error();
			error.name = "AssertionError";
			error.description = "An assertion failed";
			throw error;
			
		case 2: 
			if (arguments[0]) return;
			var error = new Error();
		    error.name = "AssertionError";
		    error.description = arguments[1];
		    throw error;
		    
		default:
			if (arguments[0]) return;
		    var error = new Error();
		    error.name = "AssertionError";
		    var args = Array.prototype.slice.call(arguments,0)
		    error.description = MessageFormat.format(arguments[1], args.slice(2));
		    throw error;
		}
	};
	
	return my;
}(josm.util || {});