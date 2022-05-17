/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */
/* global require */

const tu = require('josm/unittest')
const util = require('josm/util')
const {buildId, OsmPrimitiveType} = require('josm/ds')
const test = tu.test

const suites = []

suites.push(tu.suite('build id test cases',
  test('can build a primitive id with a numeric id and a type as string', function () {
    let id
    id = buildId(1, 'node')
    util.assert(id.getType() === OsmPrimitiveType.NODE)
    util.assert(id.getUniqueId() === 1)

    id = buildId(1, 'way')
    util.assert(id.getType() === OsmPrimitiveType.WAY)
    util.assert(id.getUniqueId() === 1)

    id = buildId(1, 'relation')
    util.assert(id.getType() === OsmPrimitiveType.RELATION)
    util.assert(id.getUniqueId() === 1)
  }),

  test('can build a an primitive id with a numeric id and a type as OsmPrimitiveType', function () {
    let id
    id = buildId(1,  OsmPrimitiveType.NODE)
    util.assert(id.getType() === OsmPrimitiveType.NODE)
    util.assert(id.getUniqueId() === 1)

    id = buildId(1, OsmPrimitiveType.WAY)
    util.assert(id.getType() === OsmPrimitiveType.WAY)
    util.assert(id.getUniqueId() === 1)

    id = buildId(1, OsmPrimitiveType.RELATION)
    util.assert(id.getType() === OsmPrimitiveType.RELATION)
    util.assert(id.getUniqueId() === 1)
  }),

  test('can build a an primitive id from an object', function () {
    let id
    id = buildId({id: 1, type: 'node'})
    util.assert(id.getType() === OsmPrimitiveType.NODE)
    util.assert(id.getUniqueId() === 1)

    id = buildId({id: 1, type: 'way'})
    util.assert(id.getType() === OsmPrimitiveType.WAY)
    util.assert(id.getUniqueId() === 1)

    id = buildId({id: 1, type: 'relation'})
    util.assert(id.getType() === OsmPrimitiveType.RELATION)
    util.assert(id.getUniqueId() === 1)
  })
))

exports.suites = suites
exports.run = function () {
    return suites
      .map(function (a) { return a.run() })
      .reduce(function (a, b) { return a + b })
  }

