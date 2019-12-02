/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */
/* global require */

const { suite, test, expectError } = require('josm/unittest')
const util = require('josm/util')
// const command = require('josm/command')
const { DataSetUtil, buildId } = require('josm/ds')

const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const OsmPrimitiveType = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitiveType')

const System = Java.type('java.lang.System')
const Exception = Java.type('java.lang.Exception')
const Node = Java.type('org.openstreetmap.josm.data.osm.Node')

function log (msg) {
  System.out.println(msg)
}

function josmScriptingPluginHome () {
  const homeDir = System.getenv('JOSM_SCRIPTING_PLUGIN_HOME')
  if (homeDir === null) {
    throw new Error('environment variable JOSM_SCRIPTING_PLUGIN_HOME not set')
  }
  return homeDir
}

const suites = []

suites.push(suite('constructor',
  test('accept existing ds', () => {
    const ds = new DataSet()
    const dsutil = new DataSetUtil(ds)
    util.assert(dsutil.ds === ds, 'two datasets should be equal')
  }),

  test('reject missing ds', () => {
    expectError(() => {
      const _dsutil = new DataSetUtil()
    })
    expectError(() => {
      const _dsutil = new DataSetUtil(null)
    })
    expectError(() => {
      const _dsutil = new DataSetUtil(undefined)
    })
    expectError(() => {
      const _dsutil = new DataSetUtil('some string value')
    })
  })
))

suites.push(suite('buildId',
  test('accept node types', () => {
    const id1 = buildId(12345, 'node')
    util.assert(id1.getType() === OsmPrimitiveType.NODE,
      'id1 should be of type NODE')

    const id2 = buildId(12345, 'n')
    util.assert(id2.getType() === OsmPrimitiveType.NODE,
      'id2 should be of type NODE')

    const id3 = buildId(12345, 'NoD')
    util.assert(id3.getType() === OsmPrimitiveType.NODE,
      'id3 should be of type NODE')

    const id4 = buildId(12345, OsmPrimitiveType.NODE)
    util.assert(id4.getType() === OsmPrimitiveType.NODE,
      'id4 should be of type NODE')
  }),

  test('accept way types', () => {
    const id1 = buildId(12345, 'way')
    util.assert(id1.getType() === OsmPrimitiveType.WAY)

    const id2 = buildId(12345, 'w')
    util.assert(id2.getType() === OsmPrimitiveType.WAY)

    const id3 = buildId(12345, 'WaY')
    util.assert(id3.getType() === OsmPrimitiveType.WAY)

    const id4 = buildId(12345, OsmPrimitiveType.WAY)
    util.assert(id4.getType() === OsmPrimitiveType.WAY)
  }),

  test('accept relation types', () => {
    const id1 = buildId(12345, 'relation')
    util.assert(id1.getType() === OsmPrimitiveType.RELATION)

    const id2 = buildId(12345, 'r')
    util.assert(id2.getType() === OsmPrimitiveType.RELATION)

    const id3 = buildId(12345, 'ReLaT')
    util.assert(id3.getType() === OsmPrimitiveType.RELATION)

    const id4 = buildId(12345, OsmPrimitiveType.RELATION)
    util.assert(id4.getType() === OsmPrimitiveType.RELATION)
  }),

  test('reject unsupported types', () => {
    expectError(() => {
      buildId('nosuchtype', 12345)
    })
    expectError(() => {
      buildId(null, 12345)
    })
    expectError(() => {
      buildId(undefined, 12345)
    })
  }),

  test('accept positive and negative ids, reject 0', () => {
    const id1 = buildId(1, 'node')
    util.assert(id1.getUniqueId() === 1)

    const id2 = buildId(-1, 'node')
    util.assert(id2.getUniqueId() === -1)

    expectError(() => {
      buildId(0, 'node')
    })
  }),

  test('reject wrong argument order', () => {
    expectError(() => {
      buildId('node', 1)
    })
  }),

  test('accept already built id', () => {
    const id = buildId(1, 'node')
    const otherId = buildId(id)
    util.assert(otherId.getType() === OsmPrimitiveType.NODE)
    util.assert(otherId.getUniqueId() === 1,
      `expected id 1, got ${otherId.getUniqueId()}`)
  })
))

suites.push(suite('builders',
  test('can create nodeBuilder', () => {
    const builder = new DataSetUtil(new DataSet())
      .nodeBuilder()
    util.assert(util.isSomething(builder))
  }),

  test('can create wayBuilder', () => {
    const builder = new DataSetUtil(new DataSet())
      .wayBuilder()
    util.assert(util.isSomething(builder))
  }),

  test('can create relationBuilder', () => {
    const builder = new DataSetUtil(new DataSet())
      .relationBuilder()
    util.assert(util.isSomething(builder))
  })
))

suites.push(suite('get',
  test('get node', () => {
    const { DataSet, DataSetUtil, buildId } = require('josm/ds')
    const ds = new DataSet()
    const dsutil = new DataSetUtil(ds)
    dsutil.nodeBuilder().withId(1, 1).create()
    const n1 = dsutil.get(buildId(1, 'node'))
    util.assert(n1.getUniqueId() === 1)

    // get the node using node()
    const n2 = dsutil.node(1)
    util.assert(n2.getUniqueId() === 1)

    // get non existing node
    const n3 = dsutil.get(buildId(99, 'node'))
    util.assert(n3 === null)
  }),

  test('get existing way', () => {
    const ds = new DataSet()
    const dsutil = new DataSetUtil(ds)
    dsutil.wayBuilder().withId(1, 1).create()
    const w1 = dsutil.get(buildId(1, 'way'))
    util.assert(w1.getUniqueId() === 1)

    // get the way using way()
    const w2 = dsutil.way(1)
    util.assert(w2.getUniqueId() === 1)

    // get non existing way
    const w3 = dsutil.get(buildId(99, 'way'))
    util.assert(w3 === null)
  }),

  test('get existing relation', () => {
    const ds = new DataSet()
    const dsutil = new DataSetUtil(ds)
    dsutil.relationBuilder().withId(1, 1).create()
    const r1 = dsutil.get(buildId(1, 'relation'))
    util.assert(r1.getUniqueId() === 1)

    // get the relation using relation()
    const r2 = dsutil.relation(1)
    util.assert(r2.getUniqueId() === 1)

    // get non existing relation
    const r3 = dsutil.get(buildId(99, 'relation'))
    util.assert(r3 === null)
  })
))

suites.push(suite('batch',
  test('can run a batch operation', () => {
    const dsutil = new DataSetUtil(new DataSet())
    dsutil.batch(() => {
      for (let i = 0; i < 10; i++) {
        dsutil.nodeBuilder().create()
      }
    })
  }),

  test('ignores null or undefined', () => {
    const dsutil = new DataSetUtil(new DataSet())
    dsutil.batch(null)
    dsutil.batch(undefined)
  })
))

suites.push(suite('remove',
  test('can remove a single node', () => {
    const dsutil = new DataSetUtil(new DataSet())
    dsutil.nodeBuilder().withId(1, 2).create()
    dsutil.remove(1, 'node')
    util.assert(dsutil.node(1) === null)
  }),

  test('can remove two nodes', () => {
    const dsutil = new DataSetUtil(new DataSet())
    const nodes = [
      dsutil.nodeBuilder().withId(1, 1).create(),
      dsutil.nodeBuilder().withId(2, 1).create()
    ]
    dsutil.remove(nodes[0], nodes[1])
    util.assert(dsutil.node(1) === null, 'node 1 should not be present')
    util.assert(dsutil.node(2) === null, 'node 2 should not be present')
  }),

  test('can remove an  array of nodes', () => {
    const dsutil = new DataSetUtil(new DataSet())
    const nodes = [
      dsutil.nodeBuilder().withId(1, 1).create(),
      dsutil.nodeBuilder().withId(2, 1).create()
    ]
    dsutil.remove(nodes)
    util.assert(dsutil.node(1) === null, 'node 1 should not be present')
    util.assert(dsutil.node(2) === null, 'node 2 should not be present')
  }),

  test('can remove two array of nodes', () => {
    const dsutil = new DataSetUtil(new DataSet())
    const nodes = [
      dsutil.nodeBuilder().withId(1, 1).create(),
      dsutil.nodeBuilder().withId(2, 1).create()
    ]
    dsutil.remove([nodes[0]], [nodes[1]])
    util.assert(dsutil.node(1) === null, 'node 1 should not be present')
    util.assert(dsutil.node(2) === null, 'node 2 should not be present')
  })
))

suites.push(suite('load',
  test('can load an OSM file', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osm`
    const dsutil = DataSetUtil.load(path)
    util.assert(dsutil.node(1) !== null, 'node 1 should have been loaded')
  }),

  test('can load an OSM file with a non-standard suffix', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osm-unknown-suffix`
    const dsutil = DataSetUtil.load(path, { format: 'osm' })
    util.assert(dsutil.node(1) !== null, 'node 1 should have been loaded')
  }),

  test('can load an OSC file', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osc`
    const dsutil = DataSetUtil.load(path)
    const node = dsutil.node(1)
    util.assert(node !== null, 'node 1 should be present')

    // check whether the updates in the change file have been applied
    // to the dataset
    util.assert(node.lat() === 2.0, 'lat should be 2.0')
    util.assert(node.lon() === 2.0, 'lon should be 2.0')
    util.assert(node.get('amenity') === 'restaurant',
      'should have tag amenity=restaurant')
  }),

  test('can load an OSC file with an unknown suffix', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osc-unknown-suffix`
    const dsutil = DataSetUtil.load(path, { format: 'osc' })
    const node = dsutil.node(1)
    util.assert(node !== null, 'node 1 should be present')

    // check whether the updates in the change file have been applied
    // to the dataset
    util.assert(node.lat() === 2.0, 'lat should be 2.0')
    util.assert(node.lon() === 2.0, 'lon should be 2.0')
    util.assert(node.get('amenity') === 'restaurant',
      'should have tag amenity=restaurant')
  }),

  test('can load a compressed OSM file', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osm.gz`
    const dsutil = DataSetUtil.load(path)
    util.assert(dsutil.node(1) !== null, 'node 1 should have been loaded')
  }),

  test('can load a compressed OSM file with a non-standard suffix', () => {
    const path = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-datasetutil-load.osm.gz-unknown-suffix`
    const dsutil = DataSetUtil.load(path, { format: 'osm.gz' })
    util.assert(dsutil.node(1) !== null, 'node 1 should have been loaded')
  })
))

suites.push(suite('save',
  test('can save a dataset to a file', () => {
    const File = Java.type('java.io.File')
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withId(1, 1)
      .withPosition(1.0, 1.0)
      .create()
    const outputFile = File.createTempFile('dataset', '.osm')
    dsutil.save(outputFile)

    const dsutil2 = DataSetUtil.load(outputFile)
    const node = dsutil2.node(1)
    util.assert(node !== null, 'should have read node 1')
    util.assert(node.lat() === 1.0, 'lat should be 1.0')
    util.assert(node.lon() === 1.0, 'lon should be 1.0')
  })
))

suites.push(suite('query',

  test('query - josm expression - simple', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'restaurant' })
      .create(2)
    dsutil.wayBuilder()
      .withTags({ highway: 'residential' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()
    const objs = dsutil.query('name=test')
    util.assert(objs.length === 1, 'should have found one node, found {0}',
      objs.length)
    util.assert(objs[0].getUniqueId() === 1, 'should have found node 1')
  }),

  test('query - josm expression - type', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'restaurant' })
      .create(2)
    dsutil.wayBuilder()
      .withTags({ highway: 'residential' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()
    const objs = dsutil.query('type:node')
    util.assert(objs.length === 2, 'should have found two nodes, found {0}',
      objs.length)
    util.assert(objs[0] instanceof Node, '1 - should be a node')
    util.assert(objs[1] instanceof Node, '2 - should be a node')
  }),

  test('query - josm expression - combined, with regexp', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'tttttt' })
      .create(2)
    dsutil.wayBuilder()
      .withTags({ highway: 'tasdf' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()
    const objs = dsutil.query('type:node && *=^tt.*', { withRegexp: true })
    util.assert(objs.length === 1, 'should have found two node, found {0}',
      objs.length)
    util.assert(objs[0] instanceof Node, '1 - should be a node')
  }),

  test('query - predicate - simple', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'restaurant' })
      .create(2)
    dsutil.wayBuilder()
      .withTags({ highway: 'residential' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()
    const objs = dsutil.query((obj) => {
      return obj.get('name') === 'test'
    })
    util.assert(objs.length === 1, 'should have found one node, found {0}',
      objs.length)
    util.assert(objs[0].getUniqueId() === 1, 'should have found node 1')
  }),

  test('query - predicate function - type', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'restaurant' })
      .create(2)
    dsutil.wayBuilder()
      .withTags({ highway: 'residential' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()
    const objs = dsutil.query((obj) => {
      return obj instanceof Node
    })
    util.assert(objs.length === 2, 'should have found two node, found {0}',
      objs.length)
    util.assert(objs[0] instanceof Node, '1 - should be a node')
    util.assert(objs[1] instanceof Node, '2 - should be a node')
  }),

  test('query - predicated function- custom, with regexp', () => {
    const dsutil = new DataSetUtil()
    dsutil.nodeBuilder()
      .withTags({ name: 'test' })
      .create(1)
    dsutil.nodeBuilder()
      .withTags({ amenity: 'tttttt' })
      .create(2)
    dsutil.wayBuilder().withTags({ highway: 'tasdf' })
      .withNodes(dsutil.node(1), dsutil.node(2))
      .create()

    const regexp = /^tt.*/
    const predicate = (obj) => {
      if (!(obj instanceof Node)) {
        return false
      }
      for (const it = obj.getKeys().entrySet().iterator(); it.hasNext();) {
        const entry = it.next()
        if (regexp.test(entry.getValue())) {
          return true
        }
      }
      return false
    }
    const objs = dsutil.query(predicate)
    util.assert(objs.length === 1, 'should have found two node, found {0}',
      objs.length)
    util.assert(objs[0] instanceof Node, '1 - should be a node')
  })
))

function run () {
  return suites
    .map((a) => a.run())
    .reduce((a, b) => a + b)
}

if (typeof exports === 'undefined') {
  // not loaded as module. Run the tests immediately.
  run()
} else {
  // loaded as module. Export the run function but don't
  // execute it here.
  exports.run = run
  exports.fragileRun = function () {
    const numfail = run()
    if (numfail > 0) {
      throw new Exception(`There are ${numfail} failing tests`)
    } else {
      System.out.println('All tests ran successfully! ')
    }
  }
}
