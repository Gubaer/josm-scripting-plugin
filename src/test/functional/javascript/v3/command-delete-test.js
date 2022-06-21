import josm from 'josm'
import {buildDeleteCommand} from 'josm/command'
import * as util from 'josm/util'
import { NodeBuilder, WayBuilder, RelationBuilder } from 'josm/builder'
const layer = josm.layers.addDataLayer()
const ds = layer.getDataSet()

export function run() {
  const n1 = NodeBuilder
    .forDataSet(ds)
    .create(1234)
  const n2 = NodeBuilder
    .forDataSet(ds)
    .withPosition(3, 4)
    .create()

  const w1 = WayBuilder
    .forDataSet(ds)
    .withTags({ highway: 'residential' })
    .withNodes(n1, n2)
    .create(777)

  const r1 = RelationBuilder
    .forDataSet(ds)
    .withMembers(n1, w1)
    .create()

  // delete three objects in the layer
  buildDeleteCommand(n1, w1, n2, r1).applyTo(layer)
  util.assert(ds.getPrimitiveById(n1).isDeleted(), 'node n1 should be deleted')
  util.assert(ds.getPrimitiveById(n2).isDeleted(), 'node n2 should be deleted')
  util.assert(ds.getPrimitiveById(w1).isDeleted(), 'way w1 should be deleted')
  util.assert(ds.getPrimitiveById(r1).isDeleted(), 'relation r1 should be deleted')
}
