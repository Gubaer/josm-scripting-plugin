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
		return value === null || value === void 0;
	};
	
	
	my.isSomething = function(val) {
		return ! my.isNothing(val);
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
	
	/**
	 * Asserts that <code>val</code> is defined and non-null.
	 * 
	 * @example
	 * josm.util.assertSomething(null);    // -> throws an exception
	 * josm.util.assertSomething(void 0);  // -> throws an exception
	 * 
	 * josm.util.assertSomting("test");    // -> OK 
	 * josm.util.assertSomething(5);       // -> OK 
	 * 
	 * @function
	 * @name assertSomething
	 * @param {Anything} val the value to check
	 * @param {String} msg  (optional) message if the assertion fails
	 * @param {Object...} values (optional) additional values used in <code>msg</code> placeholders 
	 * @memberOf josm.util 
	 */
	my.assertSomething = function(val) {
		var args;
		if (arguments.length <= 1) {
			args = [my.isSomething(val), "Expected a defined non-null value, got {0}", val];
		} else {
			args = [my.isSomething(val)].concat(Array.prototype.slice.call(arguments,1));
		}
		my.assert.apply(args);
	};
	
	/**
	 * Asserts that <code>val</code> is a number.
	 * 
	 * @function
	 * @name assertNumber
	 * @param {Anything} val the value to check
	 * @param {String} msg  (optional) message if the assertion fails
	 * @param {Object...} values (optional) additional values used in <code>msg</code> placeholders 
	 * @memberOf josm.util
	 */
	my.assertNumber = function(val) {
		var args;
		if (arguments.length <= 1) {
		   args = [my.isSomething(val), "Expected a number, got {0}", val];
		} else {
		   args = [my.isSomething(val)].concat(Array.prototype.slice.call(arguments,1));
		}
		my.assert.apply(args);
	};
	
	/**
	 * Returns true if  <code>val</code> is defined.
	 * 
	 * @function
	 * @name isDef
	 * @param {Anything} val the value to check
	 * @memberOf josm.util
	 */
	my.isDef = function(val) {
		return val !== void 0;  
	};
	
	/**
	 * Returns true if  <code>val</code> is a number.
	 * 
	 * @function
	 * @name isNumber
	 * @param {Anything} val the value to check
	 * @memberOf josm.util
	 */	
	my.isNumber = function(val) {
		return typeof val === "number";
	};
	
	/**
	 * Returns true if  <code>val</code> is a string.
	 * 
	 * @function
	 * @name isString
	 * @param {Anything} val the value to check
	 * @memberOf josm.util
	 */		
	my.isString = function(val) {
		return my.isDef(val) && (typeof val === "string" || val instanceof String);
	};
	
	/**
	 * Replies the number of properties owned by <code>o</code>.
	 * 
	 * @example
	 * 
	 * var o = {p1: "v1", p2: "v2"};
	 * var c = util.countProperties(o);   // ->  2
	 * 
	 * o = {};
	 * c = util.countProperties(o);       // ->  0
	 * 
	 * o = undefined;
	 * c = util.countProperties(o);       // ->  undefined 
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name countProperties
	 */
	my.countProperties  = function(o) {
		if (my.isNothing(o)) return void 0;
		if (! (typeof o === "object")) return void 0;
		var count = 0;
		for (var p in o) {
			if (o.hasOwnProperty(p)) count++;
		}
		return count;
	};
	
	/**
	 * Replies true, if <code>o</code> owns at least one property.
	 * 
	 * @example
	 * 
	 * var o = {p1: "v1", p2: "v2"};
	 * var c = util.hasProperties(o);   // ->  true
	 * 
	 * o = {};
	 * c = util.hasProperties(o);       // ->  false
	 * 
	 * o = undefined;
	 * c = util.hasProperties(o);       // ->  false 
	 * 
	 * @memberOf josm.util
	 * @function
	 * @name hasProperties 
	 */
	my.hasProperties = function(o) {
		var count = my.countProperties(o);
		if (count === void 0) return false;
		return count > 0;
	};	
	
	return my;
}(josm.util || {});