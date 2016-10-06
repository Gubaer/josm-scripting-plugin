/**
 * <p>Provides a set of static utility functions.</p>
 *
 * @module josm/util
 */
// -- imports
var MessageFormat = java.text.MessageFormat;

/**
 * Checks whether a value is null or undefined.
 *
 * @param {object} value  the value to check
 * @type {boolean}
 * @return false, if <code>value</code> is null or undefined; true, otherwise
 * @memberof josm/util
 * @method
 * @summary  Checks whether a value is null or undefined.
 * @name isNothing
 */
exports.isNothing = function(value) {
    return value === null || value === void 0;
};

/**
 * Checks whether a value is neither null nor undefined.
 *
 * @param {object} value  the value to check
 * @type {boolean}
 * @return false, if <code>value</code> is null or undefined; true, otherwise
 * @memberof josm/util
 * @method
 * @summary  Checks whether a value is neither null nor undefined.
 * @name isSomething
 */
exports.isSomething = function(val) {
    return ! exports.isNothing(val);
};

/**
 * <p>Trims leading and trailing whitespace from <code>s</code>.</p>
 *
 * <p>Replies s, if s is null or undefined. Any other value is converted to a
 * string, then leading and trailing white space is removed.</p>
 *
 * @param {string} s  the string to be trimmed
 * @type {string}
 * @memberof josm/util
 * @method
 * @summary  Trims leading and trailing whitespace from <code>s</code>.
 * @name trim
 */
exports.trim = function(s){
    if (exports.isNothing(s)) return s;
    return (s + '').replace(/^\s+/, '').replace(/\s+$/, '');
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
 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an
 *    Error.</dd>
 *
 *    <dt><code>assert(cond, msg)</code></dt>
 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an
 *    Error, whose <code>description</code> property
 *    is set to <code>msg</code>.</dd>
 *
 *      <dt><code>assert(cond, msg, objs...)</code></dt>
 *    <dd>Checks the condition <code>cond</code>. If it is false, throws an
 *    Error, whose <code>description</code> property
 *    is set to the formatted message <code>msg</code>. Internally uses
 *    <code>java.text.MessageFormat</code> to format the message.</dd>
 *
 * </dl>
 *
 * @example
 * var util = require("josm/util");
 * // throws an Error
 * util.assert(false);
 *
 * // throws an Error e, with e.description == "My message"
 * util.assert(false, "My message");
 *
 * // throws an Error e, with e.description == "My message: test"
 * util.assert(false, "My message: {0}", "test");
 *
 * @memberof josm/util
 * @method
 * @summary  Assert a condition and throw an Error if the condition isn't met.
 * @name assert
 */
exports.assert = function() {
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
 * var util = require("josm/util");
 * util.assertSomething(null);    // -> throws an exception
 * util.assertSomething(void 0);  // -> throws an exception
 *
 * util.assertSomting("test");    // -> OK
 * util.assertSomething(5);       // -> OK
 *
 * @param {any} val the value to check
 * @param {string} msg  (optional) message if the assertion fails
 * @param values (optional) additional values used in <code>msg</code>
 * placeholders
 * @memberof josm/util
 * @method
 * @summary  Asserts that <code>val</code> is defined and non-null.
 * @name assertSomething
 */
exports.assertSomething = function(val) {
    var args;
    if (arguments.length <= 1) {
        args = [exports.isSomething(val),
                "Expected a defined non-null value, got {0}", val];
    } else {
        args = [exports.isSomething(val)].concat(
            Array.prototype.slice.call(arguments,1));
    }
    exports.assert.apply(args);
};

/**
 * Asserts that <code>val</code> is a number.
 *
 * @param {Anything} val the value to check
 * @param {String} msg  (optional) message if the assertion fails
 * @param values (optional) additional values used in <code>msg</code>
 * placeholders
 * @memberof josm/util
 * @method
 * @summary  Asserts that <code>val</code> is a number.
 * @name assertNumber
 */
exports.assertNumber = function(val) {
    var args;
    if (arguments.length <= 1) {
       args = [exports.isSomething(val), "Expected a number, got {0}", val];
    } else {
       args = [exports.isSomething(val)]
               .concat(Array.prototype.slice.call(arguments,1));
    }
    exports.assert.apply(args);
};

/**
 * Returns true if  <code>val</code> is defined.
 *
 * @param {any} val the value to check
 * @memberof josm/util
 * @method
 * @summary  Returns true if  <code>val</code> is defined.
 * @name isDef
 * @type boolean
 */
exports.isDef = function(val) {
    return val !== void 0;
};

/**
 * Returns true if  <code>val</code> is a number.
 *
 * @param {any} val the value to check
 * @memberof josm/util
 * @method
 * @summary  Returns true if  <code>val</code> is a number.
 * @name isNumber
 * @type boolean
 */
exports.isNumber = function(val) {
    return typeof val === "number";
};

/**
 * Returns true if  <code>val</code> is a string.
 *
 * @param {any} val the value to check
 * @return true, if val is a string or a String object
 * @memberof josm/util
 * @method
 * @summary Returns true if  <code>val</code> is a string.
 * @name isString
 * @type boolean
 */
exports.isString = function(val) {
    return exports.isDef(val)
            && (typeof val === "string" || val instanceof String);
};

/**
 * Replies true if <code>val</code> is an array.
 *
 * @param {anything} val the value to check
 * @return true, if val is an array
 * @memberof josm/util
 * @method
 * @summary Replies true if <code>val</code> is an array.
 * @name isArray
 * @type boolean
 */
exports.isArray = function(val) {
    return Object.prototype.toString.call( val ) === '[object Array]';
};

/**
 * Replies true if <code>val</code> is a list of arguments.
 *
 * @param {anything} val the value to check
 * @return true, if val is a list of arguments
 * @memberof josm/util
 * @method
 * @summary Replies true if <code>val</code> is a list of arguments.
 * @name isArguments
 * @type boolean
 */
exports.isArguments = function(val) {
    return Object.prototype.toString.call(val) === '[object Arguments]';
};

/**
 * Replies the number of properties owned by <code>o</code>.
 *
 * @example
 * var util = require("josm/util");
 * var o = {p1: "v1", p2: "v2"};
 * var c = util.countProperties(o);   // ->  2
 *
 * o = {};
 * c = util.countProperties(o);       // ->  0
 *
 * o = undefined;
 * c = util.countProperties(o);       // ->  undefined
 *
 * @param {any} o the object
 * @memberof josm/util
 * @method
 * @summary Replies the number of properties owned by <code>o</code>.
 * @name countProperties
 * @type number
 */
exports.countProperties  = function(o) {
    if (exports.isNothing(o)) return void 0;
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
 * var util = require("josm/util");
 * var o = {p1: "v1", p2: "v2"};
 * var c = util.hasProperties(o);   // ->  true
 *
 * o = {};
 * c = util.hasProperties(o);       // ->  false
 *
 * o = undefined;
 * c = util.hasProperties(o);       // ->  false
 *
 * @param {any} o the object
 * @memberof josm/util
 * @method
 * @summary Replies true, if <code>o</code> owns at least one property.
 * @name hasProperties
 * @type boolean
 */
exports.hasProperties = function(o) {
    var count = exports.countProperties(o);
    if (count === void 0) return false;
    return count > 0;
};

/**
 * Replies true, if f is a function.
 *
 * @param {any} f the object
 * @memberof josm/util
 * @method
 * @summary Replies true, if f is a function.
 * @name isFunction
 * @type boolean
 */
exports.isFunction = function(f) {
    return typeof f === "function";
};

/**
 * Mixes the properties of a list of objects into one object.
 *
 * @return a new object which includes the combined properties of the
 *         argument objects
 * @type object
 * @memberof josm/util
 * @method
 * @summary Mixes the properties of a list of objects into one object.
 * @name mix
 */
exports.mix = function(){
    var mixin = {};

    function copyProperties(other){
        for (var p in other){
            if (!other.hasOwnProperty(p)) continue;
            mixin[p] = other[p];
        }
    }

    for (var i=0; i< arguments.length; i++){
        var template = arguments[i];
        if (exports.isNothing(template)) continue;
        if (! (typeof template === "object")) continue;
        copyProperties(template);
    }
    return mixin;
};


/**
 * <p>Prints a message to stdout (including newline).</p>
 *
 * <p>Supports string templates à la java.text.MessageFormat.</p>
 *
 * @example
 *
 * util.println("Hello world! My name is {0}", myname);
 *
 * @memberof josm/util
 * @method
 * @summary Prints a message to stdout (including newline).
 * @name println
 */
exports.println = function() {
    var args = Array.prototype.slice.call(arguments,0);
    if (args.length == 0) return "";
    args[0] = args[0] + ""; // make sure first argument is a string
    java.lang.System.out.println(MessageFormat.format(args[0],args.slice(1)));
};

/**
 * <p>Prints a message to stdout (without newline).</p>
 *
 * <p>Supports string templates à la java.text.MessageFormat.</p>
 *
 * @example
 *
 * util.print("Hello world! My name is {0}", myname);
 *
 * @memberof josm/util
 * @method
 * @summary Prints a message to stdout (without newline).
 * @name print
 */
exports.print = function() {
    var args = Array.prototype.slice.call(arguments,0);
    if (args.length == 0) return "";
    args[0] = args[0] + ""; // make sure first argument is a string
    java.lang.System.out.print(MessageFormat.format(args[0],args.slice(1)));
};

/**
 * <p>Checks whether two java objects are either both null or equal by calling
 * o1.equals(o2).</p>
 *
 * @param {object} o1 a java object or null
 * @param {object} o2 a java object or null
 * @memberof josm/util
 * @method
 * @summary Are two java objects equal.
 * @name javaEquals
 * @type boolean
 */
exports.javaEquals = function(o1, o2) {
    exports.assert(typeof o1 === "object" && typeof o1.equals === "function");
    exports.assert(typeof o2 === "object" && typeof o2.equals === "function");
    if (o1 === null && o2 === null) return true;
    if (o1 === null && o2 !== null) return false;
    return o1.equals(o2);
};

/**
 * <p>Iterates over the elements in <code>collection</code> and invokes
 * <code>delegate()</code> on each element.</p>
 *
 * @param {array|arguments|java.util.Collection} collection the collection of
 *   elements
 * @param {function} delegate the function to call on each elemennt
 * @memberof josm/util
 * @method
 * @summary Iteraties over the elements of a collection
 * @name each
 */
exports.each = function(collection, delegate) {
    if (exports.isNothing(collection) || exports.isNothing(delegate)) return;
    if (exports.isArray(collection) || exports.isArguments(collection)) {
        var len = collection.length;
        for (var i=0; i<len; i++) delegate(collection[i]);
    } else if (collection instanceof java.util.Collection) {
        for (var it = collection.iterator(); it.hasNext();) delegate(it.next());
    } else {
        util.assert(false, "collection: unexpected type of value, got {0}",
            collection);
    }
};

/**
 * <p>Replies true, if a value is an array, an arguments list or a Java
 *     collection.
 *
 * @param {object} value the value to check
 * @memberof josm/util
 * @method
 * @summary Is a value a collection?
 * @name isCollection
 */
exports.isCollection = function(value) {
    return exports.isArray(value) || exports.isArguments(value)
            || value instanceof java.util.Collection;
};