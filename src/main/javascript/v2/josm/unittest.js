/**
* @module josm/unittest
*/

/* global Java */
/* global require */

const util = require('josm/util')
const System = Java.type('java.lang.System')

const out = System.out

exports.TestCase = function (name, test) {
  this._name = name
  this._test = test
}

exports.test = function () {
  switch (arguments.length) {
    case 1:
      return new exports.TestCase('No Name', arguments[0])
    case 2:
      return new exports.TestCase(arguments[0], arguments[1])
    default:
      util.assert(false, 'Unsupported arguments')
  }
}

exports.TestCase.prototype.run = function () {
  try {
    this._test()
    out.println('PASS - ' + this._name)
    return true
  } catch (e) {
    // out.println('FAIL - ' + this._name + ' - ' + e.toSource() + (e.description ? ' - ' + e.description : ''))
    out.println('FAIL - ' + this._name + ' - ' + (e.description ? ' - ' + e.description : ''))
    return false
  }
}

exports.Suite = function (name) {
  this._name = name
  this._tests = []
}

exports.suite = function () {
  if (arguments.length === 0) return new exports.Suite()
  let idx = 0
  const name = arguments[0]
  let suite
  if (util.isString(name)) {
    suite = new exports.Suite(name)
    idx = 1
  } else {
    suite = new exports.Suite()
  }
  for (let i = idx; i < arguments.length; i++) {
    const test = arguments[i]
    if (test instanceof exports.TestCase) {
      suite.add(test)
    } else if (util.isFunction(test)) {
      suite.add(exports.test(test))
    } else {
      util.assert(false, 'Unsupported arguments')
    }
  }
  return suite
}

exports.Suite.prototype.add = function (test) {
  this._tests.push(test)
}

exports.Suite.prototype.run = function () {
  out.println('----------------------------------------------------------------------')
  if (this._name) {
    out.println('suite: ' + this._name)
    out.println('----------------------------------------------------------------------')
  }
  let numtests = 0
  let numfail = 0
  let numok = 0
  for (let i = 0; i < this._tests.length; i++) {
    const ret = this._tests[i].run()
    numtests++
    if (ret) {
      numok++
    } else {
      numfail++
    }
  }
  out.println('----------------------------------------------------------------------')
  out.println(' # tests: ' + numtests + ' # PASS : ' + numok + '  # FAIL : ' + numfail)
  out.println('----------------------------------------------------------------------')
  return numfail
}

exports.expectError = function () {
  let name = 'no name'
  let f
  switch (arguments.length) {
    case 0: return
    case 1: f = arguments[0]; break
    case 2:
      name = arguments[0]
      f = arguments[1]
      break
    default:
      util.assert(false, 'Unexpected number of arguments')
  }
  try {
    f()
    util.assert(false, "''{0}'': should have failed. Didn''t catch an error.", name)
  } catch (e) {
    // OK
  }
}

exports.expectAssertionError = function () {
  let name = 'no name'
  let f
  switch (arguments.length) {
    case 0: return
    case 1: f = arguments[0]; break
    case 2:
      name = arguments[0]
      f = arguments[1]
      break
    default:
      util.assert(false, 'Unexpected number of arguments')
  }
  try {
    f()
    util.assert(false, "''{0}'': should have failed. Didn''t catch an error.", name)
  } catch (e) {
    if (e.name !== 'AssertionError') {
      util.assert(false, "''{0}'': expected an AssertionError, caught {1}.", name, e.toSource())
    }
  }
}
