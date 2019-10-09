/* global Java */

const tu = require('josm/unittest')
const util = require('josm/util')
const test = tu.test

const nb = require('josm/builder').NodeBuilder
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const System = Java.type('java.lang.System')
const suites = []

suites.push(tu.suite('NodeBuilder test cases',
  test('local node - most simple node', function () {
    const node = nb.create()
    util.assert(util.isSomething(node))
  }),
  test('local node - with position', function () {
    const node = nb.withPosition(1, 2).create()
    util.assert(util.isSomething(node), 'expected node to be something')
    util.assert(node.getCoor().lat() === 1, 'lat should be 1')
    util.assert(node.getCoor().lon() === 2, 'lon should be 2')
  }),
  test('local node - with missing position', function () {
    tu.expectError('missing position', function () {
      nb.withPosition().create()
    })
  }),
  test('local node - with illegal lat', function () {
    tu.expectError('illegal lat', function () {
      nb.withPosition(-91, 2).create()
    })
  }),
  test('local node - with illegal lon', function () {
    tu.expectError('with illegal lon', function () {
      nb.withPosition(1, 181).create()
    })
  }),
  test('local node - with tags', function () {
    const node = nb.withTags({ name: 'aName' }).create()
    util.assert(util.isSomething(node))
    util.assert(node.get('name') === 'aName')
  }),
  test('local node - with tags - null', function () {
    nb.withTags(null).create()
  }),
  test('local node - with tags - undef', function () {
    nb.withTags(undefined).create()
  }),
  test('local node - with tags - unsupported value', function () {
    tu.expectError('with tags - unsupported value', function () {
      nb.withTags('string value not allowed').create()
    })
  }),
  test('local node - with a dataset', function () {
    var ds = new DataSet()
    var NodeBuilder = require('josm/builder').NodeBuilder
    var nb = new NodeBuilder(ds)
    nb.create()
    util.assert(ds.getNodes().size() === 1, 'size should be 1')
  }),

  /* ---------------------------------------------------------------------- */
  /* tests for global nodes */
  /* ---------------------------------------------------------------------- */
  test('global node - most simple node', function () {
    const node = nb.create(1)
    util.assert(node.getUniqueId() === 1, 'Expected id 1, got {0}',
      node.getUniqueId())
    util.assert(node.getVersion() === 1, 'Expected version 1, got {0}',
      node.getVersion())
  }),

  test('global node - with id and version', function () {
    const node = nb.create(2, { version: 3 })
    util.assert(node.getUniqueId() === 2, 'unique id should be 2')
    util.assert(node.getVersion() === 3, 'version should be 3')
  }),

  test('global node - withId(id)', function () {
    var node = nb.withId(2).create()
    util.assert(node.getUniqueId() === 2)
    util.assert(node.getVersion() === 1)
  }),

  test('global node - withId(id,version) ', function () {
    var node = nb.withId(2, 3).create()
    util.assert(node.getUniqueId() === 2)
    util.assert(node.getVersion() === 3)
  }),

  test('global node - withId(id,version) - overriding ', function () {
    var node = nb.withId(2, 3).create(5, { version: 6 })
    util.assert(node.getUniqueId() === 5)
    util.assert(node.getVersion() === 6)
  }),

  test('global node - illegal id - 0', function () {
    tu.expectAssertionError('illegal id - 0', function () {
      nb.create(0)
    })
  }),

  test('global node - illegal id - negative', function () {
    tu.expectAssertionError('illegal id- negative', function () {
      nb.create(-1)
    })
  }),

  test('global node - illegal version - 0', function () {
    tu.expectAssertionError('illegal version - 0', function () {
      nb.create(1, 0)
    })
  }),

  test('global node - illegal version - negative', function () {
    tu.expectAssertionError('illegal version - negative', function () {
      nb.create(1, -1)
    })
  }),

  test('global node - illegal version - not a number', function () {
    tu.expectAssertionError('illegal version - not a number', function () {
      nb.create(1, '5')
    })
  }),

  test('global node - illegal version - not a number - null', function () {
    tu.expectAssertionError('illegal vesion not a number - null', function () {
      nb.create(1, null)
    })
  }),
  test('global node - with a dataset', function () {
    const ds = new DataSet()
    const NodeBuilder = require('josm/builder').NodeBuilder
    const nb = new NodeBuilder(ds)
    nb.create(2)
    util.assert(ds.getNodes().size() === 1)
  }),

  /* ---------------------------------------------------------------------- */
  /* tests for proxy nodes */
  /* ---------------------------------------------------------------------- */
  test('proxy node - simple case', function () {
    var node = nb.createProxy(5)
    util.assert(node.getUniqueId() === 5, 'Expected id {0}, got {1}', 5, node.getUniqueId())
    util.assert(node.getVersion() === 0,
      'Expected version 0, got {0}', node.getVersion())
    util.assert(node.isIncomplete(),
      'Expected node is incomplete, got {0}', node.isIncomplete())
  }),

  test('proxy node - no id', function () {
    tu.expectAssertionError('proxy node - no id', function () {
      nb.createProxy()
    })
  }),

  test('proxy node - negative id', function () {
    tu.expectAssertionError('proxy node - negative id', function () {
      nb.createProxy(-1)
    })
  }),
  test('proxy node - with a dataset', function () {
    const ds = new DataSet()
    const NodeBuilder = require('josm/builder').NodeBuilder
    const nb = new NodeBuilder(ds)
    nb.createProxy(2)
    util.assert(ds.getNodes().size() === 1)
  }),

  /* ---------------------------------------------------------------------- */
  /* create */
  /* ---------------------------------------------------------------------- */
  test('create - id - OK', function () {
    var n = nb.create({ id: 1 })
    util.assert(n.getUniqueId() === 1, 'unexpected id, got {0}', n.getUniqueId())
  }),
  test('create - id - 0', function () {
    tu.expectAssertionError('create - id - 0', function () {
      nb.create({ id: 0 })
    })
  }),
  test('create - id - -1', function () {
    tu.expectAssertionError('create - id - -1', function () {
      nb.create({ id: -1 })
    })
  }),
  test('create - id - null', function () {
    tu.expectAssertionError('create - id - null', function () {
      nb.create({ id: null })
    })
  }),
  test('create - id - undefined', function () {
    tu.expectAssertionError('create - id - undefined', function () {
      nb.create({ id: undefined })
    })
  }),
  test('create - id - not a number', function () {
    tu.expectAssertionError('create - id - not a number', function () {
      nb.create({ id: 'not a number ' })
    })
  }),

  test('create - version - OK', function () {
    var n = nb.create({ id: 1, version: 2 })
    util.assert(n.getVersion() === 2, 'unexpected version, got {0}', n.getVersion())
  }),
  test('create - version - 0', function () {
    tu.expectAssertionError('create - version - 0', function () {
      nb.create({ id: 1, version: 0 })
    })
  }),
  test('create - version - -1', function () {
    tu.expectAssertionError('create - version - -1', function () {
      nb.create({ id: 1, version: -1 })
    })
  }),
  test('create - version - null', function () {
    tu.expectAssertionError('create - version - null', function () {
      nb.create({ id: 1, version: null })
    })
  }),
  test('create - version - undefined', function () {
    tu.expectAssertionError('create - version - undefined', function () {
      nb.create({ id: 1, version: undefined })
    })
  }),
  test('create - version - not a number', function () {
    tu.expectAssertionError('create - version - not a number', function () {
      nb.create({ id: 1, version: 'not a number' })
    })
  })
))

suites.push(tu.suite('forDataSet test cases',
  test('instance context - create with defined dataset', function () {
    const ds = new DataSet()
    const builder = require('josm/builder')
    let nb = new builder.NodeBuilder()
    nb = nb.forDataSet(ds)
    const n = nb.create()
    util.assert(n.getDataSet() === ds,
      '1 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  }),
  test('static context - create with defined dataset', function () {
    const ds = new DataSet()
    const builder = require('josm/builder')
    const nb = builder.NodeBuilder.forDataSet(ds)
    const n = nb.create()
    util.assert(n.getDataSet() === ds,
      '2 - node should belong to the dataset {0}, actually is {1}',
      ds,
      n.getDataSet()
    )
  })
))

exports.run = function () {
  return suites
    .map(function (a) { return a.run() })
    .reduce(function (a, b) { return a + b })
}
