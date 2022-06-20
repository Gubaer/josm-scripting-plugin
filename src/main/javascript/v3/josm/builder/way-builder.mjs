/**
 * @module josm/builder/way
 */

/* global Java */

// -- imports
const Node = Java.type('org.openstreetmap.josm.data.osm.Node')
const Way = Java.type('org.openstreetmap.josm.data.osm.Way')
const Relation = Java.type('org.openstreetmap.josm.data.osm.Relation')
const RelationMember = Java.type('org.openstreetmap.josm.data.osm.RelationMember')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const OsmPrimitive = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitive')
const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')
const List = Java.type('java.util.List')

import * as util from 'josm/util'
import {
    assertGlobalId,
    rememberId,
    rememberTags,
    assignTags,
    rememberIdFromObject,
    rememberVersionFromObject,
    checkLat,
    checkLon,
    rememberPosFromObject,
    rememberTagsFromObject
} from './common'

function receiver (that) {
  return typeof that === 'object' ? that : new WayBuilder()
}

/**
* WayBuilder helps to create OSM
* {@class org.openstreetmap.josm.data.osm.Way}s.
*
* Methods of WayBuilder can be used in a static and in an instance context.
* It isn't necessary to create an instance of WayBuilder, unless it is
* configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
* to which created ways are added.
* @example
*  import {WayBuilder} from 'josm/builder'
*  const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
*
*  const ds = new DataSet()
*  // create a way builder without and underlying dataset ...
*  let wbuilder = new WayBuilder()
*  // ... with an underlying dataset ....
*  wbuilder =  new WayBuilder(ds)
*  // ... or using this factory method
*  wbuilder = WayBuilder.forDataSet(ds)
*
*
*  // create a new local way
*  const w1 = wbuilder.create()
*
*  // create a new global way
*  const w2 = wbuilder.withTags({highway: 'residential'}).create(1111)
*
*  // create a new proxy for a global way
*  // (an 'incomplete' node in JOSM terminology)
*  const w3 = wbuilder.createProxy(2222)
*
* @class
* @param {org.openstreetmap.josm.data.osm.DataSet} [ds]  a JOSM
*    dataset to which created ways are added. If missing, the created ways
*    aren't added to a dataset.
* @summary Helps to create OSM {@class org.openstreetmap.josm.data.osm.Way}s
* @name WayBuilder
*/

/**
 * Creates a new builder for OSM ways
 *
 * @return {module:josm/builder~WayBuilder} the way builder
 * @param {org.openstreetmap.josm.data.osm.DataSet} ds the dataset which
 *         created objects are added to
 * @summary Creates a new WayBuilder with an underlying dataset.
 * @class
 * @memberof module:josm/builder~WayBuilder
 */
export function WayBuilder(ds) {
  if (util.isSomething(ds)) {
    util.assert(ds instanceof DataSet, 'Expected a DataSet, got {0}', ds)
    this.ds = ds
  }
  this.nodes = []
}

/**
 * Creates or configures a WayBuilder which will add created nodes
 * to the dataset <code>ds</code>.
 *
 * @example
 * import {WayBuilder} from 'josm/builder'
 *
 * // create a new way builder which builds to a data set
 * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 * const ds = new DataSet()
 * let wb = WayBuilder.forDataSet(ds)
 *
 * @return {module:josm/builder~WayBuilder} the way builder
 * @param {org.openstreetmap.josm.data.osm.DataSet} ds the dataset to which
 *         created objects are added
 * @summary Creates a new WayBuilder with an underlying dataset.
 * @function
 * @name forDataSet
 * @memberof module:josm/builder~WayBuilder
 */
function forDataSet (ds) {
  const builder = receiver(this)
  util.assert(util.isSomething(ds),
    'Expected a non-null defined object, got {0}', ds)
  util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
  builder.ds = ds
  return builder
}
WayBuilder.prototype.forDataSet = forDataSet
WayBuilder.forDataSet = forDataSet

/**
 * Declares the global way id and the global way version.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {WayBuilder} from 'josm/builder'
 * // creates a global way with id 1111 an version 22
 * const way = WayBuilder.withId(1111, 22).create()
 *
 * @param {number} id  (mandatory) the global way id. A number > 0.
 * @param {number} [version] the global way version. If present,
 *    a number > 0. If missing, the version 1 is assumed.
 * @return {module:josm/builder~WayBuilder} the way builder (for method chaining)
 * @summary Declares the global way id and the global way version.
 * @function
 * @memberof module:josm/builder~WayBuilder
 * @name withId
 * @instance
 */
function withId (id, version) {
  const builder = receiver(this)
  rememberId(builder, id, version)
  return builder
}

WayBuilder.prototype.withId = withId
WayBuilder.withId = withId

/**
 * Declares the tags to be assigned to the new way.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {WayBuilder} from 'josm/builder'
 * // a new global way with the global id 1111 and tags name='Laubeggstrasse'
 * // and highway=residential
 * const w1 = WayBuilder.withTags({name:'Laubeggstrasse', highway:'residential'})
 *     .create(1111)
 *
 * // a new local way with tags name=test and highway=road
 * const tags = {
 *     name    : 'Laubeggstrasse',
 *     highway : 'residential'
 * }
 * const w2 = WayBuilder.withTags(tags).create()
 *
 * @param {object} [tags] the tags
 * @return {module:josm/builder~WayBuilder} the way builder (for method chaining)
 * @summary Declares the tags to be assigned to the new way.
 * @function
 * @memberof module:josm/builder~WayBuilder
 * @name withTags
 * @instance
 */
function withTags (tags) {
  const builder = receiver(this)
  rememberTags(builder, tags)
  return builder
}
WayBuilder.prototype.withTags = withTags
WayBuilder.withTags = withTags

/**
 * Declares the nodes of the way.
 *
 * Accepts either a vararg list of
 * {@class org.openstreetmap.josm.data.osm.Node},
 * an array of {@class org.openstreetmap.josm.data.osm.Node}s or a Java list
 * of {@class org.openstreetmap.josm.data.osm.Node}s. At least <strong>two
 * non-identical nodes</strong> have to be supplied.
 * The same node can occure more than once in the list, but a consecutive
 * sequence of the same node is collapsed to one node.
 *
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {WayBuilder, NodeBuilder} from 'josm/builder'
 * // creates a new local way with two local nodes
 * const way = WayBuilder.withNodes(
 *   NodeBuilder.create(), 
 *   NodeBuilder.create()
 * ).create()
 *
 * @param nodes  the list of nodes. See description and examples.
 * @return {module:josm/builder~WayBuilder} the way builder (for method chaining)
 * @summary Declares the nodes of the way.
 * @function
 * @memberof module:josm/builder~WayBuilder
 * @name withNodes
 * @instance
 */
function withNodes () {
  const builder = receiver(this)
  let nodes

  switch (arguments.length) {
    case 0: return builder
    case 1:
      nodes = arguments[0]
      if (util.isNothing(nodes)) return builder
      if (nodes instanceof Node) {
        nodes = [nodes]
      } else if (util.isArray(nodes)) {
        // OK
      } else if (nodes instanceof List) {
        const temp = []
        for (let it = nodes.iterator(); it.hasNext();) temp.push(it.next())
        nodes = temp
      } else {
        util.assert(false,
          'Argument 0: expected a Node or a list of nodes, got {0}',
          nodes)
      }
      break
    default:
      nodes = Array.prototype.slice.call(arguments, 0)
      break
  }

  const newnodes = []
  let last
  for (let i = 0; i < nodes.length; i++) {
    const n = nodes[i]
    if (util.isNothing(n)) continue
    util.assert(n instanceof Node,
      'Expected instances of Node only, got {0} at index {1}', n, i)
    // skip sequence of identical nodes
    if (last && last.getUniqueId() === n.getUniqueId()) continue
    newnodes.push(n)
    last = n
  }
  builder.nodes = newnodes
  return builder
}

WayBuilder.withNodes =
  WayBuilder.prototype.withNodes =
  withNodes

/**
 * Creates a new <em>proxy</em> way. A proxy way is a way for which we
 * only know its global id. In order to know more details (nodes, tags, etc.),
 * we would have to download it from the OSM server.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {WayBuilder} from 'josm/builder'
 *
 * // a new proxy way for the global way with id 1111
 * const w1 = WayBuilder.createProxy(1111)
 *
 * @return {org.openstreetmap.josm.data.osm.Way} the new proxy way
 * @summary Creates a new proxy way
 * @function
 * @memberof module:josm/builder~WayBuilder
 * @name createProxy
 * @instance
 */
function createProxy (id) {
  const builder = receiver(this)
  if (util.isDef(id)) {
    util.assert(util.isNumber(id) && id > 0,
      'Expected a number > 0, got {0}', id)
    builder.id = id
  }
  util.assert(util.isNumber(builder.id),
    'way id is not a number. Use .createProxy(id) or ' +
    '.withId(id).createProxy()')

  util.assert(builder.id > 0, 'Expected way id > 0, got {0}', builder.id)
  const way = new Way(builder.id)
  if (builder.ds) builder.ds.addPrimitive(way)
  return way
}

WayBuilder.createProxy =
  WayBuilder.prototype.createProxy =
  createProxy

function rememberNodesFromObject (builder, args) {
  if (!util.hasProp(args, 'nodes')) return
  const o = args.nodes
  if (!util.isSomething(o)) return
  util.assert(util.isArray(o) || o instanceof List,
    'Expected an array or an instance of java.util.List, got {0}', o)
  builder.withNodes(o)
}

function initFromObject (builder, args) {
  rememberIdFromObject(builder, args)
  rememberVersionFromObject(builder, args)
  rememberTagsFromObject(builder, args)
  rememberNodesFromObject(builder, args)
}

/**
 * Creates a new way.
 *
 * Can be used in an instance or in a static context.
 *
 * Optional named arguments in the parameters <code>options</code>:
 * <dl>
 *   <dt><code class='signature'>id</code>:number</dt>
 *   <dd class="param-desc">the id of a global way (number > 0)</dd>
 *
 *   <dt><code class='signature'>version</code>:number</dt>
 *   <dd class="param-desc">the version of a global way (number > 0)</dd>
 *
 *   <dt><code class='signature'>nodes</code>:array|list</dt>
 *   <dd class="param-desc">an array or a list of nodes</dd>
 *
 *   <dt><code class='signature'>tags</code>:object</dt>
 *   <dd class="param-desc">an object with tags. Null values and undefined values are ignored.
 *   Any other value is converted to a string. Leading and trailing white
 *   space in keys is removed.</dd>
 * </dl>
 *
 * @example
 * import {WayBuilder, NodeBuilder} from 'josm/builder'
 * // create a new local way
 * const w1 = WayBuilder.create()
 *
 * // create a new global way
 * const w2 = WayBuilder.create(1111)
 *
 * // create a new global way with version 3 with some nodes and with
 * // some tags
 * const w3 = WayBuilder.create(2222, {
 *    version: 3,
 *    tags: {amenity: 'restaurant'},
 *    nodes: [
 *      NodeBuilder.withPosition(1,1).create(),
 *      NodeBuilder.withPosition(2,2).create(),
 *      NodeBuilder.withPosition(3,3).create()
 *    ]
 *  })
 *
 * @param {number}  [id]  a global way id. If missing and not set
 *    before using <code>withId(..)</code>, creates a new local id.
 * @param {object} [options]  additional parameters for creating the way
 * @returns {org.openstreetmap.josm.data.osm.Way} the created way
 * @summary Creates a new way
 * @function
 * @memberof module:josm/builder~WayBuilder
 * @name create
 * @instance
 */
function create () {
  const builder = receiver(this)
  let arg
  switch (arguments.length) {
    case 0:
      break
    case 1:
      arg = arguments[0]
      util.assert(util.isSomething(arg),
        'Argument 0: must not be null or undefined')
      if (util.isNumber(arg)) {
        util.assert(arg > 0,
          'Argument 0: expected an id > 0, got {0}', arg)
        builder.id = arg
      } else if (typeof arg === 'object') {
        initFromObject(builder, arg)
      } else {
        util.assert(false,
          "Argument 0: unexpected type, got ''{0}''", arg)
      }
      break

    case 2:
      arg = arguments[0]
      util.assert(util.isSomething(arg),
        'Argument 0: must not be null or undefined')
      util.assert(util.isNumber(arg), 'Argument 0: must be a number')
      util.assert(arg > 0, 'Expected an id > 0, got {0}', arg)
      builder.id = arg

      arg = arguments[1]
      if (util.isSomething(arg)) {
        util.assert(typeof arg === 'object',
          'Argument 1: must be an object')
        initFromObject(builder, arg)
      }
      break

    default:
      util.assert(false, 'Unexpected number of arguments, got {0}',
        arguments.length)
  }

  let way
  if (util.isNumber(builder.id)) {
    if (util.isNumber(builder.version)) {
      way = new Way(builder.id, builder.version)
    } else {
      way = new Way(builder.id, 1)
    }
  } else {
    way = new Way(0) // creates a new local way
  }
  assignTags(way, builder.tags || {})
  if (builder.nodes && builder.nodes.length > 0) {
    way.setNodes(builder.nodes)
  }
  if (builder.ds) {
    if (builder.ds.getPrimitiveById(way) == null) {
      builder.ds.addPrimitive(way)
    } else {
      throw new Error(
        'Failed to add primitive, primitive already included ' +
        'in dataset. \n' +
        'primitive=' + way
      )
    }
  }
  return way
}

WayBuilder.create = create
WayBuilder.prototype.create = create

