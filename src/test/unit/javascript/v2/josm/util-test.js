/* global Java */
const ArrayList = Java.type('java.util.ArrayList')

const tu = require('josm/unittest')
const util = require('josm/util')

const suites = []
suites.push(tu.suite('josm/util - each',
  tu.test('each - array', function () {
    var a = [1, 2, 3]
    var sum = 0
    util.each(a, function (n) {
      sum += n
    })
    util.assert(sum === 6, 'should be 6')
  }),
  tu.test('each - List', function () {
    const list = new ArrayList()
    list.add(1); list.add(2); list.add(3)
    let sum = 0
    util.each(list, function (n) {
      sum += n
    })
    util.assert(sum === 6, 'should be 6')
  }),
  tu.test('each - arguments', function () {
    var sum = 0
    function doit () {
      util.each(arguments, function (n) {
        sum += n
      })
    }
    doit(1, 2, 3)
    util.assert(sum === 6, 'should be 6')
  }),
  tu.test('each - four edge cases with null/undefined', function () {
    util.each(null, function () {})
    util.each(undefined, function () {})
    util.each([], null)
    util.each([], undefined)
  }),
  tu.test('each - unexpected collection', function () {
    tu.expectAssertionError('string as collection', function () {
      util.each('1, 2, 3', function () {})
    })
  })
))

suites.push(tu.suite('josm/util - isCollection',
  tu.test('isCollection - array', function () {
    util.assert(util.isCollection([]), 'array should be a collection')
  }),
  tu.test('isCollection - arguments', function () {
    util.assert(util.isCollection(arguments), 'arguments should be a collection')
  }),
  tu.test('isCollection - list', function () {
    util.assert(util.isCollection(new ArrayList()), 'list should be a collection')
  }),
  tu.test("isCollection - everything else isn't a collection", function () {
    util.assert(!util.isCollection(null), "null isn't a collection")
    util.assert(!util.isCollection(undefined), "undefined isn't a collection")
    util.assert(!util.isCollection({}), "an object isn't a collection")
    util.assert(!util.isCollection('foobar'), "an string isn't a collection")
  })
))

exports.run = function () {
  return suites
    .map(function (a) { return a.run() })
    .reduce(function (a, b) { return a + b })
}
