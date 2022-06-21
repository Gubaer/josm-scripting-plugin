/*
 * Functional test for an apply/undo/redo loop
 */
import josm from 'josm'
import {buildAddCommand} from 'josm/command'
import { NodeBuilder } from 'josm/builder'
import * as util from 'josm/util'

export function run () {
  const layer = josm.layers.addDataLayer()

  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()

  // add a node and check node is there
  buildAddCommand(n).applyTo(layer)
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
