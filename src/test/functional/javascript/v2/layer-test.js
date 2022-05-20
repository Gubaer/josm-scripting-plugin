/*
 * Functional test for adding objects with a undoable/redoable command
 * to a data layer.
 *
 * Load in the JOSM scripting console and run.
 */

/* global Java */

const josm = require('josm')
const util = require('josm/util')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')

function testGetLayer () {
  josm.layers.addDataLayer('my data layer')
  let layer = josm.layers.get('my data layer')
  util.assert(layer.getName() === 'my data layer', 'did not get correct layer')

  layer = josm.layers.get(0)
  util.assert(layer.getName() === 'my data layer', 'did not get correct layer')
}

function testRemoveLayer () {
  josm.layers.addDataLayer('my data layer')
  josm.layers.remove(0)
  util.assert(josm.layers.length === 0, `should have 0 layer, got ${josm.layers.length}`)
}

function testHasLayer () {
  const layer = josm.layers.addDataLayer('my data layer')
  util.assert(josm.layers.has(layer), 'should have layer with layer as parameter')
  util.assert(josm.layers.has(0), 'should have layer with layer index as parameter')
  util.assert(josm.layers.has('my data layer'), 'should have layer with layer name as parameter')
}

function testAddDataLayer () {
  josm.layers.addDataLayer()
  util.assert(josm.layers.length === 1, `should have exactly one layer, got ${josm.layers.length}`)
  removeAllLayers()

  const l2 = josm.layers.addDataLayer('test')
  util.assert(l2.getName() === 'test', `expected name 'test', got '${l2.getName()}`)
  removeAllLayers()

  const ds = new DataSet()

  const l3 = josm.layers.addDataLayer(ds)
  util.assert(l3.getDataSet() === ds, 'unexpected dataset')
  removeAllLayers()

  const l4 = josm.layers.addDataLayer({ name: 'test', ds: ds })
  util.assert(l4.getName() === 'test', `expected name 'test', got '${l4.getName()}`)
  util.assert(l4.getDataSet() === ds, 'unexpected dataset')
}

function removeAllLayers () {
  const numLayers = josm.layers.length
  for (let i = 0; i < numLayers; i++) {
    josm.layers.remove(0)
  }
}

exports.run = function () {
  removeAllLayers()
  testGetLayer()

  removeAllLayers()
  testRemoveLayer()

  removeAllLayers()
  testHasLayer()

  removeAllLayers()
  testAddDataLayer()
}
