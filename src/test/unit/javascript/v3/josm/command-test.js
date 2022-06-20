
/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */


import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as tu from 'josm/unittest'
import * as util from 'josm/util'
import {NodeBuilder, WayBuilder} from 'josm/builder'
import {AddCommand, buildAddCommand} from 'josm/command'

const OsmDataLayer = Java.type('org.openstreetmap.josm.gui.layer.OsmDataLayer')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const ArrayList = Java.type('java.util.ArrayList')
const OsmPrimitive = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitive')
const HashSet = Java.type('java.util.HashSet')

const suites = []

suites.push(suite('AddCommand',
  test('constructor - with empty array', function() {
    const cmd = new AddCommand([])
  }),
  test('constructor - with array', function () {
    const n1 = NodeBuilder.create()
    const n2 = NodeBuilder.create()
    const cmd = new AddCommand([n1, n2])
    util.assert(cmd._objs.length === 2, 'incorrect length')
  }),
  test('constructor - with list', function () {
    const n1 = NodeBuilder.create()
    const n2 = NodeBuilder.create()
    const list = new ArrayList()
    list.add(n1); list.add(n2)
    const cmd = new AddCommand(list)
    util.assert(cmd._objs.length === 2, 'incorrect length')
  }),
  test('constructor - illegal arguments', function () {
    tu.expectAssertionError('should not be null', function () {
      const _cmd = new AddCommand(null)
    })
    tu.expectAssertionError('should not be undefined', function () {
      const _cmd = new AddCommand(undefined)
    })
  })
))

suites.push(suite('buildAddCommand',
  test('buildAddCommand - one node', function () {
    const n1 = NodeBuilder.create()
    const cmd = buildAddCommand(n1)
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 1, 'wrong length')
    util.assert(util.isArray(cmd._objs), 'expected Array, got {0}', cmd._objs)

    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }
  }),

  test('buildAddCommand - array of nodes and ways', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const cmd = buildAddCommand([n1, w1])
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
    util.assert(util.isArray(cmd._objs), 'expected Array, got {0}', cmd._objs)

    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }
  }),

  test('buildAddCommand - list of nodes and ways', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const list = new ArrayList()
    list.add(n1)
    list.add(w1)
    const cmd = buildAddCommand(list)
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('buildAddCommand - arguments with nulls, undefined, and doublettes', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const cmd = buildAddCommand(n1, null, w1, undefined, n1)
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('buildAddCommand - array with nulls, undefined, and doublettes', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const cmd = buildAddCommand([n1, null, w1, undefined, n1])
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('buildAddCommand - mixed arguments', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const list = new ArrayList()
    list.add(NodeBuilder.create()) 
    list.add(NodeBuilder.create())
    const cmd = buildAddCommand(n1, [n1, w1], null, [list, n1, undefined])
    util.assert(cmd instanceof AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 4, 'wrong length')
  }),
  test('createJOSMCommand - one object', function () {
    const n1 = NodeBuilder.create()
    const cmd = buildAddCommand(n1)
    util.assert(cmd._objs.length === 1, 'expected 1 obj, got {0}', cmd._objs.length)
    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }

    const layer = new OsmDataLayer(new DataSet(), null, null)
    const jcmd = cmd.createJOSMCommand(layer)
    util.assert(jcmd, 'should exist')
  }),
  test('createJOSMCommand - multiple objects', function () {
    const n1 = NodeBuilder.create()
    const w1 = WayBuilder.create()
    const cmd = buildAddCommand([n1, w1])
    util.assert(cmd._objs.length === 2, 'expected 2 obj, got {0}', cmd._objs.length)
    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }

    const layer = new OsmDataLayer(new DataSet(), null, null)
    const jcmd = cmd.createJOSMCommand(layer)
    util.assert(jcmd, 'should exist')
  })
))


export function run() {
  return suites
    .map(function (suite) { return suite.run() })
    .reduce(function (a, b) { return a + b })
}
