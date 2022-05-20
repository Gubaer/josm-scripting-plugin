/* global Java */
const tu = require('josm/unittest')
const util = require('josm/util')
const test = tu.test

const wb = require('josm/builder').WayBuilder
const nb = require('josm/builder').NodeBuilder
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const ArrayList = Java.type('java.util.ArrayList')

const suites = []

suites.push(tu.suite(
  // -- with nodes
  test('local way - most simple way', function () {
    const way = wb.create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
  }),
  test('local way - with nodes - two local nodes', function () {
    const way = wb.withNodes(nb.create(), nb.create()).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected, got {0}', way.getNodes().size())
  }),
  test('local way - with nodes - two local nodes (as array)', function () {
    const way = wb.withNodes([nb.create(), nb.create()]).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - two local nodes (as list)', function () {
    const nodes = new ArrayList()
    nodes.add(nb.create())
    nodes.add(nb.create())
    const way = wb.withNodes(nodes).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - null is OK', function () {
    const way = wb.withNodes(null).create()
    util.assert(way.getNodesCount() === 0, 'no nodes expected')
  }),
  test('local way - with nodes - no nodes is OK', function () {
    const way = wb.withNodes().create()
    util.assert(way.getNodesCount() === 0, 'no nodes expected')
  }),
  test('local way - with nodes - null nodes are skipped', function () {
    const way = wb.withNodes(nb.create(), null, nb.create()).create()
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - undefined nodes are skipped', function () {
    const way = wb.withNodes([nb.create(), undefined, nb.create()]).create()
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - illegal type of arguments', function () {
    tu.expectAssertionError(function () {
      wb.withNodes('can pass in a string').create()
    })
  }),
  test('local way - with nodes - illegal types of nodes', function () {
    tu.expectAssertionError(function () {
      wb.withNodes([nb.create(), 'string not allowed'])
        .create()
    })
  }),

  test('create - id = 1', function () {
    const way = wb.create(1)
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 1, 'version should be 1')
  }),

  test('create - id = 1 (optional argument)', function () {
    const way = wb.create({ id: 1 })
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 1, 'version should be 1')
  }),

  test('create - id = 1, version=2 ', function () {
    const way = wb.create(1, { version: 2 })
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 2, 'version should be 2')
  }),
  test('create - no id, tags as optional parameters ', function () {
    const way = wb.create({ tags: { highway: 'residential' } })
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getVersion() === 0, 'version should be 0, got {0}', way.getVersion())
    util.assert(way.getNodesCount() === 0, '0 nodes expected')
    util.assert(way.get('highway') === 'residential',
      'highway=residential expected')
  }),
  test('create - global id, tags and nodes', function () {
    const way = wb.create(1234, {
      nodes: [nb.create(), nb.create()],
      tags: { highway: 'residential' }
    })
    util.assert(way.getUniqueId() === 1234, 'id = 1234 expected')
    util.assert(way.getVersion() === 1, 'version =1 expected')
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
    util.assert(way.get('highway') === 'residential',
      'highway=residential expected')
  }),
  test('create - id 0 - not allowed', function () {
    tu.expectAssertionError(function () {
      wb.create(0)
    })
  }),
  test('create - id -1 - negative id not allowed', function () {
    tu.expectAssertionError(function () {
      wb.create(-1)
    })
  }),
  test('create - id - illegal type', function () {
    tu.expectAssertionError(function () {
      wb.create('1234') // string not allowed
    })
  }),
  test('create - id - null not allowed', function () {
    tu.expectAssertionError(function () {
      wb.create(null)
    })
  }),
  test('create - id - undefined not allowed', function () {
    tu.expectAssertionError(function () {
      wb.create(undefined)
    })
  }),
  test('create - named args can be null', function () {
    wb.create(1, null)
  }),
  test('create - named args can be undefined', function () {
    wb.create(1, undefined)
  }),
  test('create - id - named args must be an object', function () {
    tu.expectAssertionError(function () {
      wb.create(1, 'can use a string here')
    })
  }),
  test('create - version - must not be 0', function () {
    tu.expectAssertionError(function () {
      wb.create(1, { version: 0 })
    })
  }),
  test('create - version - must not be negative', function () {
    tu.expectAssertionError(function () {
      wb.create(1, { version: -1 })
    })
  }),
  test('create - version - must be a number', function () {
    tu.expectAssertionError(function () {
      wb.create(1, { version: '1234' })
    })
  }),
  test('create - nodes - can be null', function () {
    wb.create(1, { nodes: null })
  }),
  test('create - nodes - can be undefined', function () {
    wb.create(1, { nodes: undefined })
  }),
  test("create - nodes - can't be a single node", function () {
    tu.expectAssertionError(function () {
      wb.create(1, { nodes: nb.create() })
    })
  }),
  test("create - nodes - can't be a string", function () {
    tu.expectAssertionError(function () {
      wb.create(1, { nodes: 'node 1' })
    })
  })
))

suites.push(tu.suite('forDataSet test cases',
  test('forDataSet - static scope', function () {
    const ds = new DataSet()
    const wb = require('josm/builder').WayBuilder.forDataSet(ds)
    const w = wb.create()
    util.assert(w.getDataSet() === ds,
      'way should belong to the dataset {0}, actually is {1}',
      ds,
      w.getDataSet()
    )
  }),
  test('forDataSet - instance scope', function () {
    const ds = new DataSet()
    const w = wb.withId(1).forDataSet(ds).create()
    util.assert(w.getDataSet() === ds,
      'way should belong to the dataset {0}, actually is {1}',
      ds,
      w.getDataSet()
    )
  })
))

exports.run = function () {
  return suites
    .map(function (a) { return a.run() })
    .reduce(function (a, b) { return a + b })
}
