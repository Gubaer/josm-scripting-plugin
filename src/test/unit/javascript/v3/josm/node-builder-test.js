/* global Java */

import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as tu from 'josm/unittest'
import * as util from 'josm/util'
import {NodeBuilder} from 'josm/builder'
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const suites = []

suites.push(suite('NodeBuilder test cases',
  test('local node - most simple node', function () {
    const node = NodeBuilder.create()
    util.assert(util.isSomething(node))
  }),
  test('local node - with position', function () {
    const node = NodeBuilder.withPosition(1, 2).create()
    util.assert(util.isSomething(node), 'expected node to be something')
    util.assert(node.getCoor().lat() === 1, 'lat should be 1')
    util.assert(node.getCoor().lon() === 2, 'lon should be 2')
  }),
  test('local node - with missing position', function () {
    expectError('missing position', function () {
      NodeBuilder.withPosition().create()
    })
  }),
  test('local node - with illegal lat', function () {
    expectError('illegal lat', function () {
      NodeBuilder.withPosition(-91, 2).create()
    })
  }),
  test('local node - with illegal lon', function () {
    expectError('with illegal lon', function () {
      NodeBuilder.withPosition(1, 181).create()
    })
  }),
  test('local node - with tags', function () {
    const node = NodeBuilder.withTags({ name: 'aName' }).create()
    util.assert(util.isSomething(node))
    util.assert(node.get('name') === 'aName')
  }),
  test('local node - with tags - null', function () {
    NodeBuilder.withTags(null).create()
  }),
  test('local node - with tags - undef', function () {
    NodeBuilder.withTags(undefined).create()
  }),
  test('local node - with tags - unsupported value', function () {
    expectError('with tags - unsupported value', function () {
      NodeBuilder.withTags('string value not allowed').create()
    })
  }),
  test('local node - with a dataset', function () {
    let ds = new DataSet()
    let nb = new NodeBuilder(ds)
    nb.create()
    util.assert(ds.getNodes().size() === 1, 'size should be 1')
  }),

  /* ---------------------------------------------------------------------- */
  /* tests for global nodes */
  /* ---------------------------------------------------------------------- */
  test('global node - most simple node', function () {
    const node = NodeBuilder.create(1)
    util.assert(node.getUniqueId() === 1, 'Expected id 1, got {0}',
      node.getUniqueId())
    util.assert(node.getVersion() === 1, 'Expected version 1, got {0}',
      node.getVersion())
  }),

  test('global node - with id and version', function () {
    const node = NodeBuilder.create(2, { version: 3 })
    util.assert(node.getUniqueId() === 2, 'unique id should be 2')
    util.assert(node.getVersion() === 3, 'version should be 3')
  }),

  test('global node - withId(id)', function () {
    let node = NodeBuilder.withId(2).create()
    util.assert(node.getUniqueId() === 2)
    util.assert(node.getVersion() === 1)
  }),

  test('global node - withId(id,version) ', function () {
    let node = NodeBuilder.withId(2, 3).create()
    util.assert(node.getUniqueId() === 2)
    util.assert(node.getVersion() === 3)
  }),

  test('global node - withId(id,version) - overriding ', function () {
    let node = NodeBuilder.withId(2, 3).create(5, { version: 6 })
    util.assert(node.getUniqueId() === 5)
    util.assert(node.getVersion() === 6)
  }),

  test('global node - illegal id - 0', function () {
    expectAssertionError('illegal id - 0', function () {
      NodeBuilder.create(0)
    })
  }),

  test('global node - illegal id - negative', function () {
    expectAssertionError('illegal id- negative', function () {
      NodeBuilder.create(-1)
    })
  }),

  test('global node - illegal version - 0', function () {
    expectAssertionError('illegal version - 0', function () {
      NodeBuilder.create(1, 0)
    })
  }),

  test('global node - illegal version - negative', function () {
    expectAssertionError('illegal version - negative', function () {
      NodeBuilder.create(1, -1)
    })
  }),

  test('global node - illegal version - not a number', function () {
    expectAssertionError('illegal version - not a number', function () {
      NodeBuilder.create(1, '5')
    })
  }),

  test('global node - illegal version - not a number - null', function () {
    expectAssertionError('illegal vesion not a number - null', function () {
      NodeBuilder.create(1, null)
    })
  }),
  test('global node - with a dataset', function () {
    const ds = new DataSet()
    const nb = new NodeBuilder(ds)
    nb.create(2)
    util.assert(ds.getNodes().size() === 1)
  }),

  /* ---------------------------------------------------------------------- */
  /* tests for proxy nodes */
  /* ---------------------------------------------------------------------- */
  test('proxy node - simple case', function () {
    let node = NodeBuilder.createProxy(5)
    util.assert(node.getUniqueId() === 5, 'Expected id {0}, got {1}', 5, node.getUniqueId())
    util.assert(node.getVersion() === 0,
      'Expected version 0, got {0}', node.getVersion())
    util.assert(node.isIncomplete(),
      'Expected node is incomplete, got {0}', node.isIncomplete())
  }),

  test('proxy node - no id', function () {
    expectAssertionError('proxy node - no id', function () {
      NodeBuilder.createProxy()
    })
  }),

  test('proxy node - negative id', function () {
    expectAssertionError('proxy node - negative id', function () {
      NodeBuilder.createProxy(-1)
    })
  }),
  test('proxy node - with a dataset', function () {
    const ds = new DataSet()
    const nb = new NodeBuilder(ds)
    nb.createProxy(2)
    util.assert(ds.getNodes().size() === 1)
  }),

  /* ---------------------------------------------------------------------- */
  /* create */
  /* ---------------------------------------------------------------------- */
  test('create - id - OK', function () {
    let n = NodeBuilder.create({ id: 1 })
    util.assert(n.getUniqueId() === 1, 'unexpected id, got {0}', n.getUniqueId())
  }),
  test('create - id - 0', function () {
    expectAssertionError('create - id - 0', function () {
      NodeBuilder.create({ id: 0 })
    })
  }),
  test('create - id - -1', function () {
    expectAssertionError('create - id - -1', function () {
      NodeBuilder.create({ id: -1 })
    })
  }),
  test('create - id - null', function () {
    expectAssertionError('create - id - null', function () {
      NodeBuilder.create({ id: null })
    })
  }),
  test('create - id - undefined', function () {
    expectAssertionError('create - id - undefined', function () {
      NodeBuilder.create({ id: undefined })
    })
  }),
  test('create - id - not a number', function () {
    expectAssertionError('create - id - not a number', function () {
      NodeBuilder.create({ id: 'not a number ' })
    })
  }),

  test('create - version - OK', function () {
    let n = NodeBuilder.create({ id: 1, version: 2 })
    util.assert(n.getVersion() === 2, 'unexpected version, got {0}', n.getVersion())
  }),
  test('create - version - 0', function () {
    expectAssertionError('create - version - 0', function () {
      NodeBuilder.create({ id: 1, version: 0 })
    })
  }),
  test('create - version - -1', function () {
    expectAssertionError('create - version - -1', function () {
      NodeBuilder.create({ id: 1, version: -1 })
    })
  }),
  test('create - version - null', function () {
    expectAssertionError('create - version - null', function () {
      NodeBuilder.create({ id: 1, version: null })
    })
  }),
  test('create - version - undefined', function () {
    expectAssertionError('create - version - undefined', function () {
      NodeBuilder.create({ id: 1, version: undefined })
    })
  }),
  test('create - version - not a number', function () {
    expectAssertionError('create - version - not a number', function () {
      NodeBuilder.create({ id: 1, version: 'not a number' })
    })
  })
))

suites.push(suite('forDataSet test cases',
  test('instance context - create with defined dataset', function () {
    const ds = new DataSet()
    const n = NodeBuilder.forDataSet(ds).create()
    util.assert(n.getDataSet() === ds,
      '1 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  }),
  test('static context - create with defined dataset', function () {
    const ds = new DataSet()
    const n =  NodeBuilder.forDataSet(ds).create()
    util.assert(n.getDataSet() === ds,
      '2 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  })
))

export function run() {
  return suites
    .map(function (suite) { return suite.run() })
    .reduce(function (a, b) { return a + b })
}
