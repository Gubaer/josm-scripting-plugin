/* global Java */


import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as tu from 'josm/unittest'
import * as util from 'josm/util'
import {WayBuilder, NodeBuilder} from 'josm/builder'

const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const ArrayList = Java.type('java.util.ArrayList')

const suites = []

suites.push(suite(
  // -- with nodes
  test('local way - most simple way', function () {
    const way = WayBuilder.create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
  }),
  test('local way - with nodes - two local nodes', function () {
    const way = WayBuilder.withNodes(NodeBuilder.create(), NodeBuilder.create()).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected, got {0}', way.getNodes().size())
  }),
  test('local way - with nodes - two local nodes (as array)', function () {
    const way = WayBuilder.withNodes([NodeBuilder.create(), NodeBuilder.create()]).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - two local nodes (as list)', function () {
    const nodes = new ArrayList()
    nodes.add(NodeBuilder.create())
    nodes.add(NodeBuilder.create())
    const way = WayBuilder.withNodes(nodes).create()
    util.assert(util.isSomething(way), 'expected a way object')
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getNodes().size() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - null is OK', function () {
    const way = WayBuilder.withNodes(null).create()
    util.assert(way.getNodesCount() === 0, 'no nodes expected')
  }),
  test('local way - with nodes - no nodes is OK', function () {
    const way = WayBuilder.withNodes().create()
    util.assert(way.getNodesCount() === 0, 'no nodes expected')
  }),
  test('local way - with nodes - null nodes are skipped', function () {
    const way = WayBuilder.withNodes(NodeBuilder.create(), null, NodeBuilder.create()).create()
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - undefined nodes are skipped', function () {
    const way = WayBuilder.withNodes([NodeBuilder.create(), undefined, NodeBuilder.create()]).create()
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
  }),
  test('local way - with nodes - illegal type of arguments', function () {
    expectAssertionError(function () {
      WayBuilder.withNodes('can pass in a string').create()
    })
  }),
  test('local way - with nodes - illegal types of nodes', function () {
    expectAssertionError(function () {
      WayBuilder.withNodes([NodeBuilder.create(), 'string not allowed'])
        .create()
    })
  }),

  test('create - id = 1', function () {
    const way = WayBuilder.create(1)
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 1, 'version should be 1')
  }),

  test('create - id = 1 (optional argument)', function () {
    const way = WayBuilder.create({ id: 1 })
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 1, 'version should be 1')
  }),

  test('create - id = 1, version=2 ', function () {
    const way = WayBuilder.create(1, { version: 2 })
    util.assert(way.getUniqueId() === 1, 'id should be 1')
    util.assert(way.getVersion() === 2, 'version should be 2')
  }),
  test('create - no id, tags as optional parameters ', function () {
    const way = WayBuilder.create({ tags: { highway: 'residential' } })
    util.assert(way.getUniqueId() < 0, 'id should be negative')
    util.assert(way.getVersion() === 0, 'version should be 0, got {0}', way.getVersion())
    util.assert(way.getNodesCount() === 0, '0 nodes expected')
    util.assert(way.get('highway') === 'residential',
      'highway=residential expected')
  }),
  test('create - global id, tags and nodes', function () {
    const way = WayBuilder.create(1234, {
      nodes: [NodeBuilder.create(), NodeBuilder.create()],
      tags: { highway: 'residential' }
    })
    util.assert(way.getUniqueId() === 1234, 'id = 1234 expected')
    util.assert(way.getVersion() === 1, 'version =1 expected')
    util.assert(way.getNodesCount() === 2, '2 nodes expected')
    util.assert(way.get('highway') === 'residential',
      'highway=residential expected')
  }),
  test('create - id 0 - not allowed', function () {
    expectAssertionError(function () {
      WayBuilder.create(0)
    })
  }),
  test('create - id -1 - negative id not allowed', function () {
    expectAssertionError(function () {
      WayBuilder.create(-1)
    })
  }),
  test('create - id - illegal type', function () {
    expectAssertionError(function () {
      WayBuilder.create('1234') // string not allowed
    })
  }),
  test('create - id - null not allowed', function () {
    expectAssertionError(function () {
      WayBuilder.create(null)
    })
  }),
  test('create - id - undefined not allowed', function () {
    expectAssertionError(function () {
      WayBuilder.create(undefined)
    })
  }),
  test('create - named args can be null', function () {
    WayBuilder.create(1, null)
  }),
  test('create - named args can be undefined', function () {
    WayBuilder.create(1, undefined)
  }),
  test('create - id - named args must be an object', function () {
    expectAssertionError(function () {
      WayBuilder.create(1, 'can use a string here')
    })
  }),
  test('create - version - must not be 0', function () {
    expectAssertionError(function () {
      WayBuilder.create(1, { version: 0 })
    })
  }),
  test('create - version - must not be negative', function () {
    expectAssertionError(function () {
      WayBuilder.create(1, { version: -1 })
    })
  }),
  test('create - version - must be a number', function () {
    expectAssertionError(function () {
      WayBuilder.create(1, { version: '1234' })
    })
  }),
  test('create - nodes - can be null', function () {
    WayBuilder.create(1, { nodes: null })
  }),
  test('create - nodes - can be undefined', function () {
    WayBuilder.create(1, { nodes: undefined })
  }),
  test("create - nodes - can't be a single node", function () {
    expectAssertionError(function () {
      WayBuilder.create(1, { nodes: NodeBuilder.create() })
    })
  }),
  test("create - nodes - can't be a string", function () {
    expectAssertionError(function () {
      WayBuilder.create(1, { nodes: 'node 1' })
    })
  })
))

suites.push(suite('forDataSet test cases',
  test('forDataSet - static scope', function () {
    const ds = new DataSet()
    const w = WayBuilder.forDataSet(ds).create()
    util.assert(w.getDataSet() === ds,
      'way should belong to the dataset {0}, actually is {1}',
      ds,
      w.getDataSet()
    )
  }),
  test('forDataSet - instance scope', function () {
    const ds = new DataSet()
    const w =  WayBuilder.forDataSet(ds).withId(1).create()
    util.assert(w.getDataSet() === ds,
      'way should belong to the dataset {0}, actually is {1}',
      ds,
      w.getDataSet()
    )
  })
))


export function run() {
  return suites
    .map(function (suite) { return suite.run() })
    .reduce(function (a, b) { return a + b })
}