/*
 * Functional test for changing objects with a undoable/redoable command
 * to a data layer.
 *
 * Load in the JOSM scripting console and run.
 */
const josm = require('josm')
const command = require('josm/command')
const { NodeBuilder, WayBuilder, RelationBuilder } = require('josm/builder')
const { member } = RelationBuilder

const util = require('josm/util')
const layer = josm.layers.addDataLayer()

function changeNodeAttributes () {
  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()

  // add a node to a layer
  command.add(n).applyTo(layer)

  command.change(n, { lat: 11.11 }).applyTo(layer)
  util.assert(n.lat() === 11.11, '1- unexpected lat')

  command.change(n, { lon: 22.22 }).applyTo(layer)
  util.assert(n.lon() === 22.22, '2 -unexpected lon')

  command.change(n, { pos: { lat: 33.33, lon: 44.44 } }).applyTo(layer)
  util.assert(n.lat() === 33.33, '3 - unexpected lat')
  util.assert(n.lon() === 44.44, '4 - unexpected on')

  command.change(n, {
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

  command.add(w.getNodes(), w).applyTo(layer)

  // create and add three nodes
  const newnodes = [nodeBuilder.create(), nodeBuilder.create(), nodeBuilder.create()]
  command.add(newnodes).applyTo(layer)

  // change the ways node list
  command.change(w, { nodes: newnodes }).applyTo(layer)
}

function changeRelationMembers () {
  const n = NodeBuilder
    .forDataSet(layer.getDataSet())
    .create()
  const w = WayBuilder
    .forDataSet(layer.getDataSet())
    .create()

  command.add(n, w).applyTo(layer)

  const relation = RelationBuilder
    .forDataSet(layer.getDataSet())
    .withMembers(n, w)
    .create()

  command.add(relation).applyTo(layer)

  command.change(relation, { members: [member('role.1', n)] }).applyTo(layer)

  util.assert(relation.getMembersCount() === 1, 'unexpected number of members')
}

exports.run = function () {
  changeNodeAttributes()
  changeWayNodes()
  changeRelationMembers()
}
