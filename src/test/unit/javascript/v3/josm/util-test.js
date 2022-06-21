/* global Java */

import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as util from 'josm/util'
const ArrayList = Java.type('java.util.ArrayList')

const suites = []

suites.push(suite('josm/util - each',
  test('each - array', function () {
    let a = [1, 2, 3]
    let sum = 0
    util.each(a, function (n) {
      sum += n
    })
    util.assert(sum === 6, 'should be 6')
  }),
  test('each - List', function () {
    const list = new ArrayList()
    list.add(1); list.add(2); list.add(3)
    let sum = 0
    util.each(list, function (n) {
      sum += n
    })
    util.assert(sum === 6, 'should be 6')
  }),
  test('each - arguments', function () {
    let sum = 0
    function doit () {
      util.each(arguments, function (n) {
        sum += n
      })
    }
    doit(1, 2, 3)
    util.assert(sum === 6, 'should be 6')
  }),
  test('each - four edge cases with null/undefined', function () {
    util.each(null, function () {})
    util.each(undefined, function () {})
    util.each([], null)
    util.each([], undefined)
  }),
  test('each - unexpected collection', function () {
    expectAssertionError('string as collection', function () {
      util.each('1, 2, 3', function () {})
    })
  })
))

suites.push(suite('josm/util - isCollection',
  test('isCollection - array', function () {
    util.assert(util.isCollection([]), 'array should be a collection')
  }),
  test('isCollection - arguments', function () {
    util.assert(util.isCollection(arguments), 'arguments should be a collection')
  }),
  test('isCollection - list', function () {
    util.assert(util.isCollection(new ArrayList()), 'list should be a collection')
  }),
  test("isCollection - everything else isn't a collection", function () {
    util.assert(!util.isCollection(null), "null isn't a collection")
    util.assert(!util.isCollection(undefined), "undefined isn't a collection")
    util.assert(!util.isCollection({}), "an object isn't a collection")
    util.assert(!util.isCollection('foobar'), "an string isn't a collection")
  })
))

export function run() {
  return suites
    .map(function (suite) { return suite.run() })
    .reduce(function (a, b) { return a + b })
}