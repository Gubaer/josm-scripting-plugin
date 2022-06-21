
import josm from 'josm'
import {buildAddCommand} from 'josm/command'
import {NodeBuilder} from 'josm/builder'
import * as util from 'josm/util'


function addASingleNode() {
  const layer = josm.layers.addDataLayer()
  const node = NodeBuilder.create()

  // add a node to a layer
  buildAddCommand(node).applyTo(layer)
  const ds = layer.getDataSet()
  util.assert(ds.containsNode(node), `ds doesn't include node ${node}`)
}

export function run() {
  addASingleNode()
}
