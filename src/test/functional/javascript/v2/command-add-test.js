/*
 * Functional test for adding objects with a undoable/redoable command
 * to a data layer.
 *
 * Load in the JOSM scripting console and run.
 */

const josm = require('josm')
const command = require('josm/command')
const nb = require('josm/builder').NodeBuilder
const util = require('josm/util')

exports.run = function () {
  const layer = josm.layers.addDataLayer()
  const node = nb.create()

  // add a node to a layer
  command.add(node).applyTo(layer)
  const ds = layer.getDataSet()
  util.assert(ds.containsNode(node), `ds doesn't include node ${node}`)
}
