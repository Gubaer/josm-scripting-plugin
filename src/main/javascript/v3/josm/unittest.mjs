/**
* @module josm/unittest
*/

/* global Java */

import * as util from 'josm/util'
const System = Java.type('java.lang.System')

const out = System.out

export function TestCase(name, test) {
  this._name = name
  this._test = test
}

export function test() {
  switch (arguments.length) {
    case 1:
      return new TestCase('No Name', arguments[0])
    case 2:
      return new TestCase(arguments[0], arguments[1])
    default:
      util.assert(false, 'Unsupported arguments')
  }
}

TestCase.prototype.run = function () {
  try {
    this._test()
    out.println('PASS - ' + this._name)
    return true
  } catch (e) {
    out.println(`FAIL - ${this._name}`)
    const desc = e.message || e.description || e
    out.println(`    description: ${desc}`)
    let context
    if (e.lineNumber || e.fileName) {
      context = `   filename: ${e.fileName}, line: ${e.lineNumber}`
    }
    if (context) {
      out.println(`    context: ${context}`)
    }
    return false
  }
}

export function Suite(name) {
  this._name = name
  this._tests = []
}

export function suite() {
  if (arguments.length === 0) return new Suite()
  let idx = 0
  const name = arguments[0]
  let suite
  if (util.isString(name)) {
    suite = new Suite(name)
    idx = 1
  } else {
    suite = new Suite()
  }
  for (let i = idx; i < arguments.length; i++) {
    const test = arguments[i]
    if (test instanceof TestCase) {
      suite.add(test)
    } else if (util.isFunction(test)) {
      suite.add(test(test))
    } else {
      util.assert(false, 'Unsupported arguments')
    }
  }
  return suite
}

Suite.prototype.add = function (test) {
  this._tests.push(test)
}

Suite.prototype.run = function () {
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

export function expectError() {
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

export function expectAssertionError() {
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
