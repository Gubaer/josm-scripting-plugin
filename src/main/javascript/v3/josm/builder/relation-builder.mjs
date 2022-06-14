/**
 * @module josm/builder/relation
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
  return typeof that === 'object' ? that : new RelationBuilder()
}

/**
* RelationBuilder helps to create OSM
* {@class org.openstreetmap.josm.data.osm.Relation}s.
*
* Methods of RelationBuilder can be used in a static and in an instance
* context.
* It isn't necessary to create an instance of RelationBuilder, unless it is
* configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
* which created ways are added to.
* @example
* import  {RelationBuilder} from 'josm/builder'
* const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
*
* const ds = new DataSet()
* // create a relation builder without and underlying dataset ...
* let rbuilder = new RelationBuilder()
* // ... with an underlying dataset ...
* rbuilder =  new RelationBuilder(ds)
* // ... or using this factory method
* rbuilder = RelationBuilder.forDataSet(ds)
*
* // create a new local relation
* const r1 = rbuilder.create()
*
* // create a new global way
* const r2 = rbuilder.withTags({route: 'bicycle'}).create(1111)
*
* // create a new proxy for a global relation
* // (an 'incomplete' node in JOSM terminology)
* const r3 = rbuilder.createProxy(2222)
*
* @class
* @param {org.openstreetmap.josm.data.osm.DataSet} ds (optional) a JOSM
*     dataset which created ways are added to. If missing, the created ways
*     aren't added to a dataset.
* @name RelationBuilder
* @summary Helps to create {@class org.openstreetmap.josm.data.osm.Relation}s
*/
export function RelationBuilder(ds) {
  if (util.isSomething(ds)) {
    util.assert(ds instanceof DataSet, 'Expected a DataSet, got {0}', ds)
    this.ds = ds
  }
  this.members = []
}

/**
 * Creates or configures a RelationBuilder which will add created nodes
 * to the dataset <code>ds</code>.
 *
 * @example
 * import {RelationBuilder} = 'josm/builder'
 *
 * // create a new relation builder building to a data set
 * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 * const ds = new DataSet()
 * const rb1 = RelationBuilder.forDataSet(ds)
 *
 * // configure an existing relation builder
 * let rb2 = new RelationBuilder()
 * rb2 = rb2.forDataSet(ds)
 *
 * @return {module:josm/builder.RelationBuilder} the relation builder
 * @summary Creates a new RelationBuilder which adds created relations to a
 *     dataset
 * @param {org.openstreetmap.josm.data.osm.DataSet} ds  a JOSM
 *     dataset which created ways are added to.
 * @function
 * @name forDataSet
 * @memberof module:josm/builder~RelationBuilder
 */
function forDataSet (ds) {
  const builder = receiver(this)
  util.assert(util.isSomething(ds),
    'Expected a non-null defined object, got {0}', ds)
  util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
  builder.ds = ds
  return builder
}
RelationBuilder.prototype.forDataSet = forDataSet
RelationBuilder.forDataSet = forDataSet

/**
 * Create a RelationMember
 *
 * <dl>
 *   <dt>member(role, obj)</dt>
 *   <dd class="param-desc">Create a relation member with role <var>role</var> and member object
 *   <var>obj</var>. <var>role</var> can be null or undefined, obj must neither
 *   be null nor undefinde. <var>role</var> is a string, <var>obj</var> is an
 *   OSM node, a way, or a relation.
 *   </dd>
 *   <dt>member(obj)</dt>
 *  <dd class="param-desc">Create a relation member for the member object <var>obj</var>.
 *   <var>obj</var> must neither be null nor undefinde. <var>obj</var> is an
 *   OSM node, a way, or a relation. The created relation member has no role.
 *   </dd>
 * </dl>
 *
 * @example
 * import {RelationBuilder, NodeBuilder} from 'josm/builder'
 *
 * // create a new RelationMember with role 'house' for a new node
 * const m1 = RelationBuilder.member('house', NodeBuilder.create())
 * // create a new RelationMember with an empty role for a new node
 * const m2 = RelationBuilder.member(NodeBuilder.create())
 *
 * @static
 * @returns {org.openstreetmap.josm.data.osm.RelationMember} the relation member
 * @summary Utility function - creates a relation member
 * @memberof module:josm/builder~RelationBuilder
 * @name member
 * @function
 * @param {string} [role] the member role
 * @param {primitive} primitive the member primitive
 */
function member () {
  function normalizeObj (obj) {
    util.assert(util.isSomething(obj),
      'obj: must not be null or undefined')
    util.assert(obj instanceof OsmPrimitive,
      'obj: expected an OsmPrimitive, got {0}', obj)
    return obj
  }
  function normalizeRole (role) {
    if (util.isNothing(role)) return null
    util.assert(util.isString(role),
      'role: expected a string, got {0}', role)
    return role
  }
  let obj
  let role
  switch (arguments.length) {
    case 0: util.assert(false,
      'Expected arguments (object) or (role, object), got 0 arguments')
      break

    case 1:
      obj = normalizeObj(arguments[0])
      return new RelationMember(null /* no role */, obj)

    case 2:
      role = normalizeRole(arguments[0])
      obj = normalizeObj(arguments[1])
      return new RelationMember(role, obj)

    default:
      util.assert(false,
        'Expected arguments (object) or (role, object), got {0} arguments',
        arguments.length)
  }
}

RelationBuilder.member = member

/**
 * Declares the global relation id and the global relation version.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {RelationBuilder} from 'josm/builder'
 * // creates a global relation with id 1111 an version 22
 * const r = RelationBuilder.withId(1111, 22).create()
 *
 * @param {number} id  (mandatory) the global relation id. A number &gt; 0.
 * @param {number} version  (optional) the global relation version. If present,
 *    a number &gt; 0. If missing, the version 1 is assumed.
 * @returns {module:josm/builder~RelationBuilder} the relation builder (for method chaining)
 * @summary Declares the relation id and version.
 * @memberof module:josm/builder~RelationBuilder
 * @name withId
 * @function
 * @instance
 */
function withId (id, version) {
  const builder = receiver(this)
  rememberId(builder, id, version)
  return builder
}
RelationBuilder.prototype.withId = withId
RelationBuilder.withId = withId

/**
 * Declares the tags to be assigned to the new relation.
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {RelationBuilder} from 'josm/builder'
 * // a new global relation with the global id 1111 and tags route='bicycle'
 * //and name='n8'
 * const r1 = RelationBuilder.withTags({name:'n8', route:'bicycle'}).create(1111)
 *
 * // a new local relation with tags name=test and highway=road
 * const tags = {
 *      name  : 'n8',
 *      route : 'bicycle'
 * }
 * const r2 = RelationBuilder.withTags(tags).create()
 *
 * @param {object} [tags]  the tags
 * @returns {module:josm/builder~RelationBuilder} a relation builder (for method chaining)
 * @summary Declares the tags to be assigned to the new relation.
 * @memberof module:josm/builder~RelationBuilder
 * @name withTags
 * @function
 * @instance
 */
function withTags (tags) {
  const builder = receiver(this)
  rememberTags(builder, tags)
  return builder
}
RelationBuilder.prototype.withTags = withTags
RelationBuilder.withTags = withTags

/**
 * Creates a new <em>proxy</em> relation. A proxy relation is a relation,
 * for which we only know its global id. In order to know more details
 * (members, tags, etc.), we would have to download it from the OSM server.
 *
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {RelationBuilder} from 'josm/builder'
 *
 * // a new proxy relation for the global way with id 1111
 * const r1 = RelationBuilder.createProxy(1111)
 *
 * @returns {org.openstreetmap.josm.data.osm.Relation} the new proxy relation
 * @summary Creates a new <em>proxy</em> relation.
 * @memberof module:josm/builder~RelationBuilder
 * @function
 * @name createProxy
 * @instance
 * @param {number} id the id for the proxy relation
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

  util.assert(builder.id > 0, 'Expected id > 0, got {0}', builder.id)
  const relation = new Relation(builder.id)
  if (builder.ds) builder.ds.addPrimitive(relation)
  return relation
}
RelationBuilder.createProxy = createProxy
RelationBuilder.prototype.createProxy = createProxy

/**
 * Declares the members of a relation.
 *
 * Accepts either a vararg list of relation members, nodes, ways or
 * relations, an array of relation members, nodes ways or relations, or a
 * Java list of members, nodes, ways or relation.
 *
 *
 * The method can be used in a static and in an instance context.
 *
 * @example
 * import {RelationBuilder, NodeBuilder} from 'josm/builder'
 *
 * const r1 = RelationBuilder.withMembers(
 *   RelationBuilder.member('house', NodeBuilder.create()),
 *   RelationBuilder.member('house', NodeBuilder.create())
 * ).create()
 *
 * @param nodes  the list of members. See description and examples.
 * @returns {module:josm/builder~RelationBuilder} the relation builder (for method chaining)
 * @summary Declares the members of a relation.
 * @memberof module:josm/builder~RelationBuilder
 * @function
 * @name withMembers
 * @instance
 */
function withMembers () {
  const builder = receiver(this)
  const members = []

  function remember (obj) {
    if (util.isNothing(obj)) return
    if (obj instanceof OsmPrimitive) {
      members.push(new RelationMember(null, obj))
    } else if (obj instanceof RelationMember) {
      members.push(obj)
    } else if (util.isArray(obj)) {
      for (let i = 0; i < obj.length; i++) remember(obj[i])
    } else if (obj instanceof List) {
      for (let it = obj.iterator(); it.hasNext();) remember(it.next())
    } else {
      util.assert(false,
        "Can''t add object ''{0}'' as relation member", obj)
    }
  }
  for (let i = 0; i < arguments.length; i++) {
    remember(arguments[i])
  }
  builder.members = members
  return builder
}

RelationBuilder.withMembers = withMembers
RelationBuilder.prototype.withMembers = withMembers

function rememberMembersFromObject (builder, args) {
  if (!util.hasProp(args, 'members')) return
  const o = args.members
  if (!util.isSomething(o)) return
  util.assert(util.isArray(o) || o instanceof List,
    'members: Expected an array or an instance of java.util.List, got {0}',
    o)
  builder.withMembers(o)
}

function initFromObject (builder, args) {
  rememberIdFromObject(builder, args)
  rememberVersionFromObject(builder, args)
  rememberTagsFromObject(builder, args)
  rememberMembersFromObject(builder, args)
}

/**
 * Creates a new relation.
 *
 * Can be used in an instance or in a static context.
 *
 * <strong>Optional named arguments in the parameters <code>args</code>
 * </strong>
 * <ul>
 *   <li><var>id</var> - the id of a global relation (number > 0)</li>
 *   <li><var>version</var> - the version of a global relation (number > 0)
 *   </li>
 *   <li><var>members</var> - an array or a list of relation members, nodes,
 *   ways, or relation</li>
 *   <li><var>tags</var> - an object with tags. Null values and undefined
 *   values are ignored. Any other value
 *   is converted to a string. Leading and trailing white space in keys is
 *   removed.</li>
 * </ul>
 *
 *
 * @example
 * import { NodeBuilder, RelationBuilder } from 'josm/builder'
 * const member = RelationBuilder.member
 * // create a new local relation
 * const r1 = RelationBuilder.create()
 *
 * // create a new global relation
 * const r2 = RelationBuilder.create(1111)
 *
 * // create a new global relation with version 3 with some tags and two
 * // members
 * const r3 = RelationBuilder.create(2222, {
 *    version: 3,
 *    tags: {type: 'route'},
 *    members: [
 *        member('house', NodeBuilder.create()),
 *        member(NodeBuilder.create())
 *    ]
 *  })
 *
 * @param {number}  [id]  a global way id. If missing and not set
 *     before using <code>withId(..)</code>, creates a new local id.
 * @param {object} [args]  additional parameters for creating the relation
 * @returns {org.openstreetmap.josm.data.osm.Relation} the relation
 * @summary Creates a new relation.
 * @memberof module:josm/builder~RelationBuilder
 * @function
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
        util.assert(arg > 0, 'Argument 0: expected an id > 0, got {0}', arg)
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
      util.assert(arg > 0,
        'Expected an id > 0, got {0}', arg)
      builder.id = arg

      arg = arguments[1]
      if (util.isSomething(arg)) {
        util.assert(typeof arg === 'object', 'Argument 1: must be an object')
        initFromObject(builder, arg)
      }
      break

    default:
      util.assert(false, 'Unexpected number of arguments, got {0}',
        arguments.length)
  }

  let relation
  if (util.isNumber(builder.id)) {
    if (util.isNumber(builder.version)) {
      relation = new Relation(builder.id, builder.version)
    } else {
      relation = new Relation(builder.id, 1)
    }
  } else {
    relation = new Relation(0) // creates a new local reöatopm
  }
  assignTags(relation, builder.tags || {})
  if (builder.members && builder.members.length > 0) {
    relation.setMembers(builder.members)
  }
  if (builder.ds) {
    if (builder.ds.getPrimitiveById(relation) == null) {
      builder.ds.addPrimitive(relation)
    } else {
      throw new Error(
        'Failed to add primitive, primitive already included ' +
        'in dataset. \n' +
        'primitive=' + relation
      )
    }
  }
  return relation
}
RelationBuilder.create = create
RelationBuilder.prototype.create = create
