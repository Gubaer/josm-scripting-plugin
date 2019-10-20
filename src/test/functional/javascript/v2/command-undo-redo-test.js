/*
 * Functional test for an apply/undo/redo loop
 */
const josm = require('josm')
const command = require('josm/command')
const { NodeBuilder } = require('josm/builder')
const util = require('josm/util')

exports.run = function () {
  const layer = josm.layers.addDataLayer()

  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()

  // add a node and check node is there
  command.add(n).applyTo(layer)
  util.assert(layer.getDataSet().containsNode(n), `layer could contain node ${n}`)

  // undo - node still there?
  josm.commands.undo()
  util.assert(!layer.getDataSet().containsNode(n), `layer shouldn't contain node ${n}`)

  // redo - node again there?
  josm.commands.redo()
  util.assert(layer.getDataSet().containsNode(n), `after redo, layer should contain node ${n}`)

  // clear all commands
  josm.commands.clear()
}
