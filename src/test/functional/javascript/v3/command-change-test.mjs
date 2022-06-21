
import josm from 'josm'
import {buildChangeCommand, buildAddCommand} from 'josm/command'
import { NodeBuilder, WayBuilder, RelationBuilder } from 'josm/builder'
const { member } = RelationBuilder

import * as util from 'josm/util'

const layer = josm.layers.addDataLayer()

function changeNodeAttributes () {
  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()

  // add a node to a layer
  buildAddCommand(n).applyTo(layer)

  buildChangeCommand(n, { lat: 11.11 }).applyTo(layer)
  util.assert(n.lat() === 11.11, '1- unexpected lat')

  buildChangeCommand(n, { lon: 22.22 }).applyTo(layer)
  util.assert(n.lon() === 22.22, '2 -unexpected lon')

  buildChangeCommand(n, { pos: { lat: 33.33, lon: 44.44 } }).applyTo(layer)
  util.assert(n.lat() === 33.33, '3 - unexpected lat')
  util.assert(n.lon() === 44.44, '4 - unexpected on')

  buildChangeCommand(n, {
    lat: 55.55,
    tags: {
      name: 'myname'
    }
  }).applyTo(layer)
}

function changeWayNodes () {
  const nodeBuilder = NodeBuilder
    .forDataSet(layer.getDataSet())

  // create and add a new way
  const w = WayBuilder
    .forDataSet(layer.getDataSet())
    .withNodes(nodeBuilder.create(), nodeBuilder.create())
    .create(1234)

  buildAddCommand(w.getNodes(), w).applyTo(layer)

  // create and add three nodes
  const newnodes = [nodeBuilder.create(), nodeBuilder.create(), nodeBuilder.create()]
  buildAddCommand(newnodes).applyTo(layer)

  // change the ways node list
  buildChangeCommand(w, { nodes: newnodes }).applyTo(layer)
}

function changeRelationMembers () {
  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()
  const w = WayBuilder
    .forDataSet(layer.getDataSet())
    .create()

  buildAddCommand(n, w).applyTo(layer)

  const relation = RelationBuilder
    .forDataSet(layer.getDataSet())
    .withMembers(n, w)
    .create()

  buildAddCommand(relation).applyTo(layer)

  buildChangeCommand(relation, { members: [member('role.1', n)] }).applyTo(layer)

  util.assert(relation.getMembersCount() === 1, 'unexpected number of members')
}

export function run() {
  changeNodeAttributes()
  changeWayNodes()
  changeRelationMembers()
}
