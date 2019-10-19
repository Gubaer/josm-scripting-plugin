/**
 * Provides a set of static utility functions.
 *
 * @module josm/util
 */

/* global Java */

// -- imports
const MessageFormat = Java.type('java.text.MessageFormat')

/** global exports */

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
exports.isNothing = function (value) {
  return value === null || value === undefined
}

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
exports.isSomething = function (val) {
  return !exports.isNothing(val)
}

/**
 * Trims leading and trailing whitespace from <code>s</code>.
 * <p>
 *
 * Replies s, if s is null or undefined. Any other value is converted to a
 * string, then leading and trailing white space is removed.
 *
 * @param {string} s  the string to be trimmed
 * @type {string}
 * @memberof josm/util
 * @method
 * @summary  Trims leading and trailing whitespace from <code>s</code>.
 * @name trim
 */
exports.trim = function (s) {
  if (exports.isNothing(s)) {
    return s
  }
  return (s + '').replace(/^\s+/, '').replace(/\s+$/, '')
}

/**
 * Assert a condition and throw an Error if the condition isn't met.
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
 * const util = require("josm/util")
 * // throws an Error
 * util.assert(false)
 *
 * // throws an Error e, with e.description == "My message"
 * util.assert(false, "My message")
 *
 * // throws an Error e, with e.description == "My message: test"
 * util.assert(false, "My message: {0}", "test")
 *
 * @memberof josm/util
 * @method
 * @summary  Assert a condition and throw an Error if the condition isn't met.
 * @name assert
 */
exports.assert = function () {
  let message, name
  switch (arguments.length) {
    case 0:
      return

    case 1:
      if (arguments[0]) return
      name = 'AssertionError'
      message = 'An assertion failed'
      break

    case 2:
      if (arguments[0]) return
      name = 'AssertionError'
      message = arguments[1]
      break

    default: {
      if (arguments[0]) return
      name = 'AssertionError'
      const args = Array.prototype.slice.call(arguments, 0)
      message = MessageFormat.format(
        args[1],
        args.slice(2)
      )
      break
    }
  }
  const error = new Error(message)
  error.name = name
  throw error
}

/**
 * Asserts that <code>val</code> is defined and non-null.
 *
 * @example
 * const util = require("josm/util");
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
exports.assertSomething = function (val) {
  let args
  if (arguments.length <= 1) {
    args = [exports.isSomething(val),
      'Expected a defined non-null value, got {0}', val]
  } else {
    args = [exports.isSomething(val)].concat(
      Array.prototype.slice.call(arguments, 1))
  }
  exports.assert.apply(args)
}

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
exports.assertNumber = function (val) {
  let args = []
  if (arguments.length <= 1) {
    args = [exports.isSomething(val), 'Expected a number, got {0}', val]
  } else {
    args = [exports.isSomething(val)]
      .concat(Array.prototype.slice.call(arguments, 1))
  }
  exports.assert.apply(args)
}

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
exports.isDef = function (val) {
  return val !== undefined
}

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
exports.isNumber = function (val) {
  return typeof val === 'number'
}

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
exports.isString = function (val) {
  return exports.isDef(val) &&
    (typeof val === 'string' || val instanceof String)
}

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
exports.isArray = function (val) {
  return Object.prototype.toString.call(val) === '[object Array]'
}

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
exports.isArguments = function (val) {
  return Object.prototype.toString.call(val) === '[object Arguments]'
}

/**
 * Replies the number of properties owned by <code>o</code>.
 *
 * @example
 * const util = require("josm/util")
 * let o = {p1: "v1", p2: "v2"}
 * let c = util.countProperties(o)   // ->  2
 *
 * o = {}
 * c = util.countProperties(o)       // ->  0
 *
 * o = undefined
 * c = util.countProperties(o)       // ->  undefined
 *
 * @param {any} o the object
 * @memberof josm/util
 * @method
 * @summary Replies the number of properties owned by <code>o</code>.
 * @name countProperties
 * @type number
 */
exports.countProperties = function (o) {
  if (exports.isNothing(o)) return undefined
  if (!(typeof o === 'object')) return undefined
  let count = 0
  for (const p in o) {
    if (exports.hasProp(o, p)) count++
  }
  return count
}

/**
 * Replies true, if <code>o</code> owns at least one property.
 *
 * @example
 * const util = require("josm/util");
 * let o = {p1: "v1", p2: "v2"};
 * let c = util.hasProperties(o);   // ->  true
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
exports.hasProperties = function (o) {
  const count = exports.countProperties(o)
  if (count === undefined) return false
  return count > 0
}

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
exports.isFunction = function (f) {
  return typeof f === 'function'
}

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
exports.mix = function () {
  const mixin = {}

  function copyProperties (other) {
    for (const p in other) {
      if (!exports.hasProp(other, p)) continue
      mixin[p] = other[p]
    }
  }

  for (let i = 0; i < arguments.length; i++) {
    const template = arguments[i]
    if (exports.isNothing(template)) continue
    if (!(typeof template === 'object')) continue
    copyProperties(template)
  }
  return mixin
}

/**
 * Prints a message to stdout (including newline).
 * <p>
 *
 * Supports the same string templates as {@class java.text.MessageFormat}.
 *
 * @example
 * var myname = "...";
 * util.println("Hello world! My name is {0}", myname);
 * // escape meta characters like {, } or ' with a leading apostrophe
 * util.println(" a pair of curly braces '{'}");
 *
 * @memberof josm/util
 * @method
 * @summary Prints a message to stdout (including newline).
 * @name println
 */
exports.println = function () {
  const args = Array.prototype.slice.call(arguments, 0)
  if (args.length === 0) return ''
  args[0] = args[0] + '' // make sure first argument is a string
  const System = Java.type('java.lang.System')
  System.out.println(MessageFormat.format(args[0], args.slice(1)))
}

/**
 * Prints a message to stdout (without newline).
 * <p>
 * Supports the same string templates as {@class java.text.MessageFormat}
 *
 * @example
 *
 * const  myname = "..."
 * util.print("Hello world! My name is {0}", myname)
 * // escape meta characters like {, } or ' with a leading apostrophe
 * util.print(" a pair of curly braces '{'}")
 *
 * @memberof josm/util
 * @method
 * @summary Prints a message to stdout (without newline).
 * @name print
 */
exports.print = function () {
  const args = Array.prototype.slice.call(arguments, 0)
  if (args.length === 0) return ''
  args[0] = args[0] + '' // make sure first argument is a string
  const System = Java.type('java.lang.System')
  System.out.print(MessageFormat.format(args[0], args.slice(1)))
}

/**
 * Checks whether two java objects are either both null or equal by calling
 * o1.equals(o2).
 *
 * @param {object} o1 a java object or null
 * @param {object} o2 a java object or null
 * @memberof josm/util
 * @method
 * @summary Are two java objects equal.
 * @name javaEquals
 * @type boolean
 */
exports.javaEquals = function (o1, o2) {
  exports.assert(typeof o1 === 'object' && typeof o1.equals === 'function')
  exports.assert(typeof o2 === 'object' && typeof o2.equals === 'function')
  if (o1 === null && o2 === null) return true
  if (o1 === null && o2 !== null) return false
  return o1.equals(o2)
}

/**
 * Iterates over the elements in <code>collection</code> and invokes
 * <code>delegate()</code> on each element.
 *
 * @param {array|arguments|java.util.Collection} collection the collection of
 *   elements
 * @param {function} delegate the function to call on each elemennt
 * @memberof josm/util
 * @method
 * @summary Iteraties over the elements of a collection
 * @name each
 */
exports.each = function (collection, delegate) {
  const Collection = Java.type('java.util.Collection')
  if (exports.isNothing(collection) || exports.isNothing(delegate)) return
  if (exports.isArray(collection) || exports.isArguments(collection)) {
    const len = collection.length
    for (let i = 0; i < len; i++) delegate(collection[i])
  } else if (collection instanceof Collection) {
    for (let it = collection.iterator(); it.hasNext();) delegate(it.next())
  } else {
    exports.assert(false, 'collection: unexpected type of value, got {0}"',
      collection)
  }
}

/**
 * Replies true, if a value is an array, an arguments list or a Java
 * collection.
 *
 * @param {object} value the value to check
 * @memberof josm/util
 * @method
 * @summary Is a value a collection?
 * @name isCollection
 */
exports.isCollection = function (value) {
  const Collection = Java.type('java.util.Collection')
  return exports.isArray(value) ||
    exports.isArguments(value) ||
    value instanceof Collection
}

exports.hasProp = function (o, name) {
  return Object.prototype.hasOwnProperty.call(o, name)
}
