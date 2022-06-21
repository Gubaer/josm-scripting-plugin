/**
 * @module josm/builder/node
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

/**
 * NodeBuilder helps to create OSM nodes.
 *
 * Methods of NodeBuilder can be used in a static and in an instance context.
 * It isn't necessary to create an instance of NodeBuilder, unless it is
 * configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
 * to which created nodes are added.
 *
 * @example
 * import {NodeBuilder} from 'josm/builder'
 * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 *
 * const ds = new DataSet()
 * // create a node builder without and underlying dataset ...
 * let nbuilder = new NodeBuilder()
 * // ... with an underlying dataset ....
 * nbuilder =  new NodeBuilder(ds)
 * // ... or using this factory method
 * nbuilder = NodeBuilder.forDataSet(ds)
 *
 * // create a new local node at position (0,0) without tags
 * const n1 = NodeBuilder.create()
 *
 * // create a new global node at a specific position with tags
 * const n2 = NodeBuilder.withPosition(1,1).withTags({name: 'test'}).create(1)
 *
 * // create a new proxy for a global node
 * // (an 'incomplete' node in JOSM terminology)
 * const n3 = NodeBuilder.createProxy(2)
 *
 * @class
 * @summary NodeBuilder helps to create OSM nodes
 * @name NodeBuilder
 * @param {org.openstreetmap.josm.data.osm.DataSet} [ds]  the dataset
 *      which created objects are added to
 */

/**
 * Creates a new node builder.
 *
 *  @param {org.openstreetmap.josm.data.osm.DataSet} [ds] the dataset
 *      to which created objects are added
 *  @constructor
 *  @memberOf NodeBuilder
 */
export function NodeBuilder(ds) {
  if (util.isSomething(ds)) {
    util.assert(ds instanceof DataSet,
      'Expected a JOSM dataset, got {0}', ds)
    this.ds = ds
  }
}

/**
 * Creates or configures a NodeBuilder which will add created nodes
 * to the dataset <code>ds</code>.
 *
 * @example
 *  import { NodeBuilder } from 'josm/builder'
 *
 *  // create a new node builder building to a data set
 *  const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 *  const ds = new DataSet()
 *
 *  // ... using a static method ...
 *  const nb1 = NodeBuilder.forDataSet(ds)
 *  // ... or the instance method
 *  const nb2 = new NodeBuilder.forDataSet(ds)
 *
 * @returns {module:josm/builder/node~NodeBuilder} the node builder
 * @param {org.openstreetmap.josm.data.osm.DataSet} ds the dataset which
 *         created objects are added to
 * @summary Creates a new NodeBuilder for a specific
 *         {@class org.openstreetmap.josm.data.osm.DataSet}.
 * @memberof module:josm/builder/node~NodeBuilder
 * @instance
 */
function forDataSet (ds) {
  const builder = receiver(this)
  util.assert(util.isSomething(ds),
    'Expected a non-null defined object, got {0}', ds)
  util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
  builder.ds = ds
  return builder
}
NodeBuilder.prototype.forDataSet = forDataSet
NodeBuilder.forDataSet = forDataSet

function receiver (that) {
  return typeof that === 'object' ? that : new  NodeBuilder()
}

function initFromObject (builder, args) {
  rememberIdFromObject(builder, args)
  rememberVersionFromObject(builder, args)
  rememberPosFromObject(builder, args)
  rememberTagsFromObject(builder, args)
}

/**
 * Named options for {@link module:josm/builder/node~NodeBuilder#create create}
 * 
 * @typedef NodeBuilderOptions
 * @property {number} [version=1] the version (&gt 0) of the node. Default: 1.
 * @property {number} [lat=0.0] a valide latitude (number in the range [-90,90]. Default: 0.0
 * @property {number} [lon=0.0] a valide longitude (number in the range[-180,180]. Default: 0.0
 * @property {number[] | {lat: number, lon: number }} [pos=null] a position, either an array with
 *   two numbers or as an object
 * @property {object} [tags] an object with tags. Null values and undefined
 *       values are ignored. Any other value is converted to a string.
 *       Leading and trailing white space in keys is removed.
 * @memberOf module:josm/builder/node~NodeBuilder
 * @example
 * // options to create a node at position (1.0, 2.0) with some tags
 * const options = {
 *   lat: 1.0,
 *   lon: 2.0,
 *   tags: {
 *     amenity: 'restaurant'
 *   }  
 * }
 */

/**
 * Creates a new  {@class org.openstreetmap.josm.data.osm.Node}.
 *
 * Can be used in an instance or in a static context.
 *
 * @example
 * import { NodeBuilder } from 'josm/builder'
 * // create a new local node at position [0,0]
 * const n1 = NodeBuilder.create()
 *
 * // create a new global node with id 1111 at position [0,0]
 * const n2 = NodeBuilder.create(1111)
 *
 * // create a new global node with version 3 at a specific position
 * // and with some tags
 * const n3 = NodeBuilder.create(2222, {
 *     version: 3,
 *     lat: 23.45,
 *     lon: 87.23,
 *     tags: {amenity: 'restaurant'}
 * })
 *
 * @param {number} [id]  a global node id. If missing and
 *     not set before using <code>withId(..)</code>, creates a new local id.
 * @param {module:josm/builder/node~NodeBuilderOptions} [options] additional options for creating the node
 * @returns {org.openstreetmap.josm.data.osm.Node}  the created node
 * @summary Creates a new  {@class org.openstreetmap.josm.data.osm.Node}
 * @memberof module:josm/builder/node~NodeBuilder
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
        util.assert(false, "Argument 0: unexpected type, got ''{0}''", arg)
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
        util.assert(typeof arg === 'object', 'Argument 1: must be an object')
        initFromObject(builder, arg)
      }
      break

    default:
      util.assert(false,
        'Unexpected number of arguments, got {0}', arguments.length)
  }

  let node
  if (util.isNumber(builder.id)) {
    if (util.isNumber(builder.version)) {
      node = new Node(builder.id, builder.version)
    } else {
      node = new Node(builder.id, 1)
    }
    const coor = new LatLon(builder.lat || 0, builder.lon || 0)
    node.setCoor(coor)
  } else {
    node = new Node(new LatLon(builder.lat || 0, builder.lon || 0))
  }
  assignTags(node, builder.tags || {})
  if (builder.ds) {
    if (builder.ds.getPrimitiveById(node) == null) {
      builder.ds.addPrimitive(node)
    } else {
      throw new Error(
        'Failed to add primitive, primitive already included ' +
        'in dataset. \n' +
        'primitive=' + node
      )
    }
  }
  return node
}

NodeBuilder.create = create
NodeBuilder.prototype.create = create

/**
 * Creates a new <em>proxy</em>
 * {@class org.openstreetmap.josm.data.osm.Node}. A proxy node is a node,
 * for which we only know its global id. In order to know more details
 * (position, tags, etc.), we would have to download it from the OSM server.
 *
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import { NodeBuilder } from 'josm/builder'
 *
 * // a new proxy node for the global node with id 1111
 * const n1 = NodeBuilder.createProxy(1111)
 *
 * @param {number} id  the node id (not null, number > 0 expected)
 * @return {org.openstreetmap.josm.data.osm.Node} the new proxy node
 * @memberof module:josm/builder/node~NodeBuilder
 * @instance
 */
function createProxy (id) {
  const builder = receiver(this)
  util.assert(util.isSomething(id),
    'Argument 0: must not be null or undefined')
  util.assert(util.isNumber(id),
    'Argument 0: expected a number, got {0}', id)
  util.assert(id > 0, 'Argument 0: id > 0 expected, got {0}', id)

  const node = new Node(id)
  if (builder.ds) builder.ds.addPrimitive(node)
  return node
}

NodeBuilder.prototype.createProxy =
  NodeBuilder.createProxy =
  createProxy

/**
 * Declares the node position.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import { NodeBuilder } from 'josm/builder'
 *
 * // a new global node with the global id 1111 at position (34,45)
 * const n1 = NodeBuilder.withPosition(34,45).create(1111)
 *
 * // a new local node at position (23.2, 87.33)
 * const n2 = NodeBuilder.withPosition(23.3,87.33).create()
 *
* @param {Number} lat  the latitude. A number in the range [-90..90].
* @param {Number} lon the longitude. A number in the range [-180..180].
* @returns {module:josm/builder/node~NodeBuilder} a node builder (for method chaining)
* @memberof module:josm/builder/node~NodeBuilder
* @instance
*/
function withPosition (lat, lon) {
  const builder = receiver(this)
  util.assert(util.isNumber(lat), 'Expected a number for lat, got {0}', lat)
  util.assert(util.isNumber(lon), 'Expected a number for lon, got {0}', lon)
  util.assert(LatLon.isValidLat(lat), 'Invalid lat, got {0}', lat)
  util.assert(LatLon.isValidLon(lon), 'Invalid lon, got {0}', lon)
  builder.lat = lat
  builder.lon = lon
  return builder
}
NodeBuilder.prototype.withPosition = withPosition
NodeBuilder.withPosition = withPosition

/**
 * Declares the tags to be assigned to the new node.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import { NodeBuilder } from 'josm/builder'
 *
 * // a new global node with the global id 1111 and tags name=test and
 * // highway=road
 * const n1 = NodeBuilder.withTags({'name':'test', 'highway':'road'}).create(1111)
 *
 * // a new local node with tags name=test and highway=road
 * const tags = {
 *      'name'    : 'test',
 *      'highway' : 'road'
 * }
 * const n2 = NodeBuilder.withTags(tags).create()
 *
 * @param {object} [tags]  the tags
 * @returns {module:josm/builder/node~NodeBuilder} the node builder (for method chaining)
 * @memberof module:josm/builder/node~NodeBuilder
 * @instance
 */
function withTags (tags) {
  const builder = receiver(this)
  rememberTags(builder, tags)
  return builder
}
NodeBuilder.prototype.withTags = withTags
NodeBuilder.withTags = withTags

/**
 * Declares the global node id and the global node version.
 *
 * The method can be used in a static and in an instance context.
 *
 * @param {number} id  the global node id. A number &gt; 0.
 * @param {number} [version=1] optional the global node version. If present,
 *     a number &gt; 0. 
 * @returns {module:josm/builder/node~NodeBuilder} the node builder (for method chaining)
 * @memberof module:josm/builder/node~NodeBuilder
 * @instance
 */
function withId (id, version) {
  const builder = receiver(this)
  rememberId(builder, id, version)
  return builder
}
NodeBuilder.prototype.withId = withId
NodeBuilder.withId = withId