/**
 * A collection of functions to create commands which can be applied, undone
 * and redone on {@class org.openstreetmap.josm.gui.layer.OsmDataLayer}s.
 *
 * @module josm/command
 */

// org.openstreetmap.josm.plugins.scripting.js.api.AddMultiCommand is populated
// in the context
/* global AddMultiCommand */

// org.openstreetmap.josm.plugins.scripting.js.api.ChangeMultiCommand is populated
// in the context
/* global ChangeMultiCommand */

// org.openstreetmap.josm.plugins.scripting.js.api.Change is populated in the
// context

/* global Change */
/* global Java */
/* global require */

const util = require('josm/util')
const layers = require('josm/layers')

const OsmPrimitive = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitive')
const OsmDataLayer = Java.type('org.openstreetmap.josm.gui.layer.OsmDataLayer')
const Layer = Java.type('org.openstreetmap.josm.gui.layer.Layer')
const UndoRedoHandler = Java.type('org.openstreetmap.josm.data.UndoRedoHandler')
const CombineWayAction = Java.type('org.openstreetmap.josm.actions.CombineWayAction')
const DeleteCommand = Java.type('org.openstreetmap.josm.command.DeleteCommand')
const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')
const RelationMember = Java.type('org.openstreetmap.josm.data.osm.RelationMember')
const ArrayList = Java.type('java.util.ArrayList')
const Map = Java.type('java.util.Map')
const HashMap = Java.type('java.util.HashMap')
const HashSet = Java.type('java.util.HashSet')
const Collection = Java.type('java.util.Collection')
const Command = Java.type('org.openstreetmap.josm.command.Command')

const System = Java.type('java.lang.System')

function checkAndFlatten (primitives) {
  const ret = new HashSet()
  function visit (value) {
    if (util.isNothing(value)) return
    if (util.isCollection(value)) {
      util.each(value, visit)
    } else if (value instanceof OsmPrimitive) {
      ret.add(value)
    } else {
      util.assert(false,
        'Unexpected object to add as OSM primitive, got {0}', value)
    }
  }
  visit(primitives)
  return ret
}

function applyTo (layer) {
  util.assert(util.isSomething(layer),
    'layer: must not be null or undefined')
  util.assert(layer instanceof OsmDataLayer,
    'layer: expected OsmDataLayer, got {0}', layer)
  const cmd = this.createJOSMCommand(layer)
  try {
    layer.getDataSet().beginUpdate()
    UndoRedoHandler.getInstance().add(cmd)
  } finally {
    layer.getDataSet().endUpdate()
  }
}

function toArray (collection) {
  if (util.isArray(collection)) return collection
  if (collection instanceof Collection) {
    const ret = []
    for (const it = collection.iterator(); it.hasNext();) ret.push(it.next())
    return ret
  }
}

/**
 * A command to add a collection of objects to a data layer.
 *
 * @class
 * @name AddCommand
 * @summary A command to add a collection of objects to a data layer
 * @param { java.util.Collection| array } objs the objects to add
 */
exports.AddCommand = function (objs) {
  util.assert(objs, 'objs: mandatory parameter missing')
  this._objs = toArray(checkAndFlatten(objs))
}

/**
 * Applies the command to a layer.
 *
 * @function
 * @summary Applies the command to a layer.
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @instance
 * @memberof module:josm/command.AddCommand
 * @name applyTo
 */
exports.AddCommand.prototype.applyTo = applyTo

/**
 * Creates the internal JOSM command for this command
 *
  * @function
 * @summary Creates the internal JOSM command for this command
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @returns {org.openstreetmap.josm.command.Command} the command
 * @memberof module:josm/command.AddCommand
 * @name createJOSMCommand
 * @instance
*/
exports.AddCommand.prototype.createJOSMCommand = function (layer) {
  util.assert(util.isSomething(layer),
    'layer: must not be null or undefined')
  util.assert(layer instanceof OsmDataLayer,
    'layer: expected OsmDataLayer, got {0}', layer)
  const list = new ArrayList()
  for (let i = 0; i < this._objs.length; i++) {
    const o = this._objs[i]
    list.add(o)
  }
  return new AddMultiCommand(layer, list)
}

/**
 * Creates a command to add a collection of objects to a data layer.
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">add(obj, obj, ...)</code> </dt>
 *   <dd class="param-desc"><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relations}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 *
 * @example
 * const cmd = require('josm/command')
 * const layers = require('josm/layer')
 * const layer  = layers.get('Data Layer 1')
 *
 * // add two nodes
 * cmd.add(n1,n1).applyTo(layer)
 *
 * // add an array of two nodes and a way
 * layer.apply(
 *    cmd.add([n1,n2,w2])
 * )
 *
 * @function
 * @summary Creates a command to add a collection of objects
 * @param {...(primitive | primitive[] | java.lang.Collection )} primitives the primitives to add
 * @name add
 * @static
 * @returns {module:josm/command.AddCommand} the command object
 */
exports.add = function () {
  const objs = checkAndFlatten(arguments)
  return new exports.AddCommand(objs)
}

/**
 * A command to delete a collection of objects in a data layer.
 *
 * @class
 * @param {java.util.Collection|array} objs the objects to add
 * @summary A command to delete a collection of objects in a data layer.
 * @param {java.util.Collection|array} objs the objects to add
 */
exports.DeleteCommand = function (objs) {
  this._objs = checkAndFlatten(objs)
}

/**
 * Applies the command to a layer.
 *
 * @function
 * @summary   Applies the command to a layer.
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @memberof module:josm/command.DeleteCommand
 * @instance
 * @name applyTo
*/
exports.DeleteCommand.prototype.applyTo = applyTo

/**
 * Creates the internal JOSM command for this command
 *
 * @function
 * @summary Creates the internal JOSM command for this command
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @returns {org.openstreetmap.josm.command.Command} the command object
 * @instance
 * @name createJOSMCommand
 * @memberof module:josm/command.DeleteCommand
 */
exports.DeleteCommand.prototype.createJOSMCommand = function (layer) {
  util.assert(util.isSomething(layer),
    'layer: must not be null or undefined')
  util.assert(layer instanceof OsmDataLayer,
    'layer: expected OsmDataLayer, got {0}', layer)
  return DeleteCommand.delete(this._objs, true /* alsoDeleteNodesInWay */, true /* silent */)
}

/**
 * Creates a command to delete a collection of objects in  a data layer.
 *
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">delete(obj,obj,..., ?options)</code> </dt>
 *   <dd class="param-desc"><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relations}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 *
 * @example
 * const cmd = require('josm/command')
 * const layers= require('josm/layer')
 * const layer = layers.get('Data Layer 1')
 *
 * // delete two nodes
 * cmd.delete(n1,n1).applyTo(layer)
 *
 * // delete an array of two nodes and a way
 * layer.apply(
 *    cmd.delete([n1,n2,w2])
 * )
 *
 * @function
 * @summary Creates a command to delete a collection of objects
 * @param {...(primitive | primitive[] | java.lang.Collection )} primitives the primitives to delete
 * @static
 * @name delete
 * @returns {module:josm/command.DeleteCommand} the command object
 */
exports.delete = function () {
  return new exports.DeleteCommand(checkAndFlatten(arguments))
}

function scheduleLatChangeFromPara (para, change) {
  if (!para || !util.isDef(para.lat)) return
  util.assert(util.isNumber(para.lat),
    'lat: lat must be a number, got {0}', para.lat)
  util.assert(LatLon.isValidLat(para.lat),
    'lat: expected a valid lat, got {0}', para.lat)
  change.withLatChange(para.lat)
}

function scheduleLonChangeFromPara (para, change) {
  if (!para || !util.isDef(para.lon)) return
  util.assert(util.isNumber(para.lon),
    'lon: lon must be a number, got {0}', para.lon)
  util.assert(LatLon.isValidLon(para.lon),
    'lon: expected a valid lon, got {0}', para.lon)
  change.withLonChange(para.lon)
}

function buildLatLon (obj) {
  util.assert(util.isSomething(obj), 'obj: must not be null or undefined')
  util.assert(typeof obj === 'object',
    'obj: expected an object, got {0}', obj)
  util.assert(util.isNumber(obj.lat),
    'obj.lat: expected a number, got {0}', obj.lat)
  util.assert(util.isNumber(obj.lon),
    'obj.lon: expected a number, got {0}', obj.lon)
  util.assert(LatLon.isValidLat(obj.lat),
    'obj.lat: expected a valid lat in the range [-90,90], got {0}',
    obj.lat)
  util.assert(LatLon.isValidLon(obj.lon),
    'obj.lon: expected a valid lon in the range [-180,180], got {0}',
    obj.lon)
  return new LatLon(obj.lat, obj.lon)
}

function schedulePosChangeFromPara (para, change) {
  if (!para || !util.isDef(para.pos)) return
  util.assert(para.pos, 'pos must no be null')
  let pos = para.pos
  if (pos instanceof LatLon) {
    // OK
  } else if (typeof pos === 'object') {
    pos = buildLatLon(pos)
  } else {
    util.assert(false,
      'pos: unexpected value, expected LatLon or object, got {0}', pos)
  }
  change.withPosChange(pos)
}

function scheduleNodeChangeFromPara (para, change) {
  if (!para || !util.isDef(para.nodes)) return
  // convert to a Java List ...
  const l = new ArrayList()
  for (let i = 0; i < para.nodes.length; i++) {
    l.add(para.nodes[i])
  }
  // ... and pass it to the change command
  change.withNodeChange(l)
}

function scheduleMemberChangeFromPara (para, change) {
  if (!para || !util.isDef(para.members)) return
  const l = new ArrayList()
  if (para.members instanceof RelationMember) {
    l.add(para.members)
  } else if (para.members instanceof Collection) {
    l.addAll(para.members)
  } else if (util.isArray(para.members)) {
    for (let i = 0; i < para.members.length; i++) {
      l.add(para.members[i])
    }
  } else {
    util.assert(false, 'Expected RelationMember, array or collection ' +
      'of RelationMembers, got {0}', para.members)
  }
  change.withMemberChange(l)
}

function scheduleTagsChangeFromPara (para, change) {
  if (!para || !util.isDef(para.tags)) return
  util.assert(para.tags, 'tags must no be null')
  let tags = para.tags
  if (tags instanceof Map) {
    // OK
  } else if (typeof tags === 'object') {
    const map = new HashMap()
    for (let key in tags) {
      if (!util.hasProp(tags, key)) continue
      const value = tags[key]
      key = util.trim(key)
      map.put(key, value)
    }
    tags = map
  } else {
    util.assert(false,
      'tags: unexpected value, expected Map or object, got {0}', tags)
  }
  change.withTagsChange(tags)
}

function changeFromParameters (para) {
  const change = new Change()
  scheduleLatChangeFromPara(para, change)
  scheduleLonChangeFromPara(para, change)
  schedulePosChangeFromPara(para, change)
  scheduleTagsChangeFromPara(para, change)
  scheduleNodeChangeFromPara(para, change)
  scheduleMemberChangeFromPara(para, change)
  return change
}

/**
 * A command to change a collection of objects in a data layer.
 *
 * @class
 * @param {java.util.Collection|array}  objs  the objects to change
 * @param {org.openstreetmap.josm.plugins.scripting.js.api.Change} change the change specification
 * @summary A command to change a collection of objects in a data layer.
 */
exports.ChangeCommand = function (objs, change) {
  this._objs = checkAndFlatten(objs)
  this._change = change
}

/**
 * Applies the command to a layer.
 *
 * @function
 * @summary Applies the command to a layer.
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @memberof module:josm/command.ChangeCommand
 * @instance
 * @name applyTo
 */
exports.ChangeCommand.prototype.applyTo = applyTo

/**
 * Creates the internal JOSM command for this command
 *
 * @summary Creates the internal JOSM command for this command
 * @param {org.openstreetmap.josm.gui.layer.OsmDataLayer} layer the data layer
 * @returns {org.openstreetmap.josm.command.Command} the command object
 * @instance
 * @function
 * @name createJOSMCommand
 * @memberof module:josm/command.ChangeCommand
 */
exports.ChangeCommand.prototype.createJOSMCommand = function (layer) {
  util.assert(util.isSomething(layer),
    'layer: must not be null or undefined')
  util.assert(layer instanceof OsmDataLayer,
    'layer: expected OsmDataLayer, got {0}', layer)
  return new ChangeMultiCommand(layer, this._objs, this._change)
}

/**
 * Creates a command to change a collection of objects in  a data layer.
 *
 * <strong>Signatures</strong>
 *
 * <dl>
 *   <dt><code class="signature">change(obj,obj,..., options)</code> </dt>
 *   <dd class="param-desc"><code>obj</code> are {@class org.openstreetmap.josm.data.osm.Node}s,
 *   {@class org.openstreetmap.josm.data.osm.Way}s, or
 *   {@class org.openstreetmap.josm.data.osm.Relation}s. Or javascript array
 *   or Java collections thereof.</dd>
 * </dl>
 *
 * The mandatory last argument is an object with named parameters. It accepts
 * the following named parameters:
 * <dl>
 *   <dt><code class="signature">lat:number</code></dt>
 *   <dd class="param-desc">Changes the latitude of the target nodes to <code>lat</code>.</dd>
 *
 *   <dt><code class="signature">lon:number</code></dt>
 *   <dd class="param-desc">Changes the longitude of the target nodes to <code>lon</code>.</dd>
 *
 *   <dt><code class="signature">pos:
 *       {@class org.openstreetmap.josm.data.coor.LatLon}|object</code></dt>
 *   <dd class="param-desc">Changes the position of the target nodes to <code>pos</code>.
 *   pos is either a {@class org.openstreetmap.josm.data.coor.LatLon} or an
 *   object <code>{lat:..., lon:...}</code>
 *   </dd>
 *
 *   <dt><code class="signature">tags:{@class java.util.Map}|object</code></dt>
 *   <dd class="param-desc">Changes the tags of the target objects to <code>tags</code>.</dd>
 *
 *   <dt><code class="signature">nodes:{@class java.util.List}|array</code></dt>
 *   <dd class="param-desc">Changes the nodes of the target way sto <code>nodes</code>.</dd>
 *
 *   <dt><code class="signature">members:{@class java.util.List}|array</code>
 *   </dt>
 *   <dd class="param-desc">Changes the nodes of the target relations to <code>members</code>.</dd>
 * </dl>
 *
 * @example
 * const {change} = require("josm/command")
 * const layers = require("josm/layers")
 * const my_layer = layers.get("Data Layer 1")
 *
 * // change the position of a node
 * change(n1, {lat: 123.45, lon: 44.234}).applyTo(my_layer)
 *
 * // change the nodes of a way
 * my_layer.apply(
 *    change(w2, {nodes: [n1,n2,3]})
 * )
 *
 * // change the tags of a collection of primitives
 * change(n1, n3, w1, r1, {
 *    tags: {'mycustomtag': 'value'}
 * }).applyTo(my_layer)
 *
 * @function
 * @summary Creates a command to change a collection of objects
 * @name change
 * @returns {module:josm/command.ChangeCommand} the change command object
 * @static
 */
exports.change = function () {
  let objs = []
  let change
  switch (arguments.length) {
    case 0:
      util.assert(false,
        'Unexpected number of arguments, got {0} arguments',
        arguments.length)
      break

    default: {
      const a = arguments[arguments.length - 1]
      if (a instanceof OsmPrimitive) {
        util.assert(false,
          'Argument {0}: unexpected last argument, expected named ' +
          'parameters, got {0}', a)
      } else if (typeof a === 'object') {
        // last argument is an object with named parameters
        objs = Array.prototype.slice.call(arguments, 0, -1)
        change = changeFromParameters(a)
      } else {
        util.assert(false,
          'Argument {0}: unexpected type of value, got {1}',
          arguments.length - 1, a)
      }
    }
  }

  const tochange = checkAndFlatten(objs)
  return new exports.ChangeCommand(tochange, change)
}

/**
 * Accessor to the global command history.
 * <p>
 * Provides static methods to redo and undo commands.
 *
 * @class
 * @summary Accessor to the global command history
 */
exports.CommandHistory = function() {}

/**
 * Undoes the last <code>depth</code> commands.
 *
 * @static
 * @summary Undoes the last <code>depth</code> commands.
 * @param {number} [depth=1]  the number of commands to be undone
 * @memberof module:josm/command.CommandHistory
 * @function
 * @name undo
 */
exports.CommandHistory.undo = function (depth) {
  if (util.isDef(depth)) {
    util.assert(util.isNumber(depth), 'depth: expected a number, got {0}',
      depth)
    util.assert(depth > 0, 'depth: expected number > 0, got {0}', depth)
  }
  const undoRedoHandler = UndoRedoHandler.getInstance()
  if (depth) {
    undoRedoHandler.undo(depth)
  } else {
    undoRedoHandler.undo()
  }
}

/**
 * Redoes the last <code>depth</code> commands.
 *
 * @static
 * @summary Redoes the last <code>depth</code> commands.
 * @param {number} [depth=1]  the number of commands to be redone.
 * @memberof module:josm/command.CommandHistory
 * @function
 * @name redo
 */
exports.CommandHistory.redo = function (depth) {
  if (util.isDef(depth)) {
    util.assert(util.isNumber(depth), 'depth: expected a number, got {0}',
      depth)
    util.assert(depth > 0, 'depth: expected number > 0, got {0}', depth)
  }
  const undoRedoHandler = UndoRedoHandler.getInstance()
  if (depth) {
    undoRedoHandler.redo(depth)
  } else {
    undoRedoHandler.redo()
  }
}

/**
 * Removes commands in the command history, either all commands, or only the
 * commands  applied to a specific layer.
 *
 * @static
 * @summary Removes commands in the command history
 * @param {org.openstreetmap.josm.gui.layer.Layer} [layer]  the
 * reference layer. Only commands applied to this layer are removed. Default
 * if missing: <strong>all</strong> commands are removed.
 * @memberof module:josm/command.CommandHistory
 * @function
 * @name clear
 */
exports.CommandHistory.clear = function (layer) {
  const undoRedoHandler = UndoRedoHandler.getInstance()

  function clearAll () {
    undoRedoHandler.clean()
  }

  function clearForLayer (layer) {
    undoRedoHandler.clean(layer)
  }

  switch (arguments.length) {
    case 0: clearAll(); break
    case 1: {
      const layer = arguments[0]
      util.assert(layer instanceof Layer, 'Expected a Layer, got {0}', layer)
      clearForLayer(layer)
      break
    }
    default:
      util.assert(false, 'Unexpected number of arguments')
  }
}

/**
* Combines two or more ways into one resulting way.
* <p>
* Reuses the logic behind the JOSM standard menu entry Tools-&gt;Combine Ways.
* If invoked from a script, this may trigger modal dialogs which are presented
* to the user, in particular if the direction of the ways has to be reversed
* because otherwise they could not be combined.
*
* @param ways the ways to be combined
* @example
* const cmd = require('josm/command')
* const layers = require('josm/layer')
* const ds = layers.activeLayer.data
* const ways  = [ds.way(1), ds.way(2), ds.way(3)]
*
* // pass in an array ...
* cmd.combineWays(ways)
* // ... or the individual ways ...
* cmd.combineWays(ds.way(1), ds.way(2), ds.way(3))
* // ... or any combination thereof.
*
* @function
* @summary Combines two or more ways into one resulting way.
* @param {...org.openstreetmap.josm.data.osm.Way | array} ways the ways to be combined
* @static
* @name combineWays
*/
exports.combineWays = function () {
  // ways becomes a java.util.HashSet
  const ways = checkAndFlatten(arguments)

  // remove any primitives which are not nodes from the arguments
  const it = ways.iterator()
  while (it.hasNext()) {
    const primitive = it.next()
    if (primitive == null || !primitive.isWay) {
      it.remove()
    }
  }
  // at least two remaining ways required to combine them. If less, just
  // return, don't throw
  if (ways.size() <= 1) return

  const activeLayer = layers.activeLayer
  if (activeLayer == null) return

  const ret = CombineWayAction.combineWaysWorker(ways)
  // happens, if combineWayWorkers presents a modal dialog and the user
  // aborts it
  if (ret == null) return
  // ret.b is the SequenceCommand which combines the ways into one
  // resulting ways. Apply this command to the active layer.
  activeLayer.apply(ret.b)
}

/**
* Combines the currently selected ways in the active layer into one resulting
* way.
*
* Returns without effect if
* <ul>
*   <li>there is no active layer</li>
*   <li>the active layer is not a data layer</li>
*   <li>there are less than two selected ways in the active layer</li>
* </ul>
*
* Reuses the logic behind the JOSM standard menu entry Tools-&gt;Combine Ways.
* If invoked from a script, this may trigger modal dialogs which are presented
* to the user, in particular if the direction of the ways has to be reversed
* because otherwise they could not be combined.
*
* @example
* const cmd = require('josm/command')
* const layers = require('josm/layer')
* const ds = layers.activeLayer.data
* const ways  = [ds.way(1), ds.way(2), ds.way(3)]
* cmd.combineWays(ways)
*
* @summary Combines the currently selected ways.
* @static
* @name combineSelectedWays
* @function
*/
exports.combineSelectedWays = function () {
  const activeLayer = layers.activeLayer
  if (activeLayer == null) return
  const ways = activeLayer.data.selection.ways
  if (ways == null || ways.length <= 1) return
  exports.combineWays(ways)
}
