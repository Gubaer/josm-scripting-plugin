
/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */
/* global require */

const tu = require('josm/unittest')
const util = require('josm/util')
const command = require('josm/command')
const test = tu.test

const nb = require('josm/builder').NodeBuilder
const wb = require('josm/builder').WayBuilder
const OsmDataLayer = Java.type('org.openstreetmap.josm.gui.layer.OsmDataLayer')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const ArrayList = Java.type('java.util.ArrayList')
const OsmPrimitive = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitive')
const HashSet = Java.type('java.util.HashSet')

const suites = []

suites.push(tu.suite('AddCommand',
  test('constructor - with array', function () {
    const n1 = nb.create()
    const n2 = nb.create()
    const cmd = new command.AddCommand([n1, n2])
    util.assert(cmd._objs.length === 2, 'incorrect length')
  }),
  test('constructor - with list', function () {
    const n1 = nb.create()
    const n2 = nb.create()
    const list = new ArrayList()
    list.add(n1); list.add(n2)
    const cmd = new command.AddCommand(list)
    util.assert(cmd._objs.length === 2, 'incorrect length')
  }),
  test('constructor - illegal arguments', function () {
    tu.expectAssertionError('should not be null', function () {
      const _cmd = new command.AddCommand(null)
    })
    tu.expectAssertionError('should not be undefined', function () {
      const _cmd = new command.AddCommand(undefined)
    })
  })
))

suites.push(tu.suite('add',
  test('add - one node', function () {
    const n1 = nb.create()
    const cmd = command.add(n1)
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 1, 'wrong length')
    util.assert(util.isArray(cmd._objs), 'expected Array, got {0}', cmd._objs)

    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }
  }),

  test('add - array of nodes and ways', function () {
    const n1 = nb.create()
    const w1 = wb.create()
    const cmd = command.add([n1, w1])
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
    util.assert(util.isArray(cmd._objs), 'expected Array, got {0}', cmd._objs)

    for (let i = 0; i < cmd._objs.length; i++) {
      const o = cmd._objs[i]
      util.assert(o instanceof OsmPrimitive, 'expected OsmPrimitive, got {0}', o)
    }
  }),

  test('add - list of nodes and ways', function () {
    const n1 = nb.create()
    const w1 = wb.create()
    const list = new ArrayList()
    list.add(n1); list.add(w1)
    const cmd = command.add(list)
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('add - arguments with nulls, undefined, and doublettes', function () {
    const n1 = nb.create()
    const w1 = wb.create()
    const cmd = command.add(n1, null, w1, undefined, n1)
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('add - array with nulls, undefined, and doublettes', function () {
    const n1 = nb.create()
    const w1 = wb.create()
    const cmd = command.add([n1, null, w1, undefined, n1])
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 2, 'wrong length')
  }),
  test('add - mixed arguments', function () {
    const n1 = nb.create()
    const w1 = wb.create()
    const list = new ArrayList()
    list.add(nb.create()); list.add(nb.create())
    const cmd = command.add(n1, [n1, w1], null, [list, n1, undefined])
    util.assert(cmd instanceof command.AddCommand, 'wrong type')
    util.assert(cmd._objs.length === 4, 'wrong length')
  }),
  test('createJOSMCommand - one object', function () {
    const n1 = nb.create()
    const cmd = command.add(n1)
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
    const n1 = nb.create()
    const w1 = wb.create()
    const cmd = command.add([n1, w1])
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

exports.run = function () {
  return suites
    .map(function (a) { return a.run() })
    .reduce(function (a, b) { return a + b })
}
