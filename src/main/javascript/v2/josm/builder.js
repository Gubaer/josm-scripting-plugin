/**
 * Collection of builders for creating OSM nodes, ways and relations.
 *
 * @module josm/builder
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

const util = require('josm/util')

function assertGlobalId (id) {
  util.assertSomething(id, 'Expected a defined, non-null object id, got {0}',
    id)
  util.assertNumber(id, 'Expected a number as object id, got {0}', id)
  util.assert(id > 0, 'Expected a positive id, got {0}', id)
}

function rememberId (builder, id, version) {
  assertGlobalId(id)
  builder.id = id
  version = util.isDef(version) ? version : 1
  util.assertNumber(version, 'Expected a number for \'version\', got {0}',
    version)
  util.assert(version > 0,
    'Expected a positive number for \'version\', got {0}', version)
  builder.version = version
}

function rememberTags (builder, tags) {
  if (util.isNothing(tags)) return
  util.assert(typeof tags === 'object',
    'Expected a hash with tags, got {0}', tags)
  builder.tags = builder.tags || {}
  for (var name in tags) {
    if (!Object.proptotype.hasOwnProperty.call(tags, name)) break
    var value = tags[name]
    name = util.trim(name)
    if (util.isNothing(value)) break
    value = value + '' // convert to string
    builder.tags[name] = value
  }
}

function assignTags (primitive, tags) {
  for (var name in tags) {
    if (!Object.proptotype.hasOwnProperty.call(tags, name)) continue
    var value = tags[name]
    if (util.isNothing(value)) continue
    value = value + ''
    primitive.put(name, value)
  }
}

function rememberIdFromObject (builder, args) {
  if (!Object.proptotype.hasOwnProperty.call(args, 'id')) return
  var o = args.id
  util.assert(util.isSomething(o),
    "''{0}'': must not be null or undefined", 'id')
  util.assert(util.isNumber(o),
    "''{0}'': expected a number, got {1}", 'id', o)
  util.assert(o > 0,
    "''{0}'': expected a number > 0, got {1}", 'id', o)
  builder.id = o
}

function rememberVersionFromObject (builder, args) {
  if (!Object.proptotype.hasOwnProperty.call(args, 'version')) return
  var o = args.version
  util.assert(util.isSomething(o),
    "''{0}'': must not be null or undefined", 'version')
  util.assert(util.isNumber(o),
    "''{0}'': expected a number, got {1}", 'version', o)
  util.assert(o > 0,
    "''{0}'': expected a number > 0, got {1}", 'version', o)
  builder.version = o
}

function rememberPosFromObject (builder, args) {
  if (Object.proptotype.hasOwnProperty.call(args, 'pos')) {
    util.assert(!(args.hasOwnProperty('lat')|| args.hasOwnProperty('lon')),
      "Can''t process both properties ''pos'' and ''lat''/''lon''")
    const pos = args.pos
    util.assert(util.isSomething(pos),
      "''{0}'': must not be null or undefined", 'pos')
    if (pos instanceof LatLon) {
      builder.lat = pos.lat()
      builder.lon = pos.lon()
    } else if (util.isArray(pos)) {
      util.assert(pos.length === 2,
        "''{0}'': expected exactly two numbers in array", 'pos')
      try {
        builder.lat = checkLat(pos[0])
      } catch (e) {
        util.assert(false, "''{0}'': {1}", 'lat', e)
      }
      try {
        builder.lon = checkLon(pos[1])
      } catch (e) {
        util.assert(false, "''{0}'': {1}", 'lon', e)
      }
    } else if (util.isObject(pos)) {
      util.assert(pos.hasOwnProperty('lat'),
        "''{0}'': missing mandatory property ''lat''", 'pos')
      util.assert(pos.hasOwnProperty('lon'),
        "''{0}'': missing mandatory property ''lon''", 'pos')
      try {
        builder.lat = checkLat(pos.lat)
      } catch (e) {
        util.assert(false, "''{0}'': {1}", 'pos', e)
      }
      try {
        builder.lon = checkLon(pos.lon)
      } catch (e) {
        util.assert(false, "''{0}'': {1}", 'pos', e)
      }
    }
  }
  if (args.hasOwnProperty('lat')) {
    const o = args.lat
    util.assert(util.isSomething(o),
      "''{0}'': must not be null or undefined", 'lat')
    util.assert(util.isNumber(o),
      "''{0}'': expected a number, got {1}", 'lat', o)
    util.assert(LatLon.isValidLat(o),
      "''{0}'': expected a valid latitude, got {1}", 'lat', o)
    builder.lat = o
  }
  if (args.hasOwnProperty('lon')) {
    var o = args['lon']
    util.assert(util.isSomething(o),
      "''{0}'': must not be null or undefined", 'lon')
    util.assert(util.isNumber(o),
      "''{0}'': expected a number, got {1}", 'lon', o)
    util.assert(LatLon.isValidLon(o),
      "''{0}'': expected a valid longitude, got {1}", 'lon', o)
    builder.lon = o
  }
}

function rememberTagsFromObject (builder, args) {
  if (!args.hasOwnProperty('tags')) return
  const o = args.tags
  if (util.isNothing(o)) return
  rememberTags(builder, o)
}

// ----------------------------------------------------------------------------
// NodeBuilder
// ----------------------------------------------------------------------------
(function () { // start submodul NodeBuilder
  /**
   * <p>NodeBuilder helps to create OSM nodes.</p>
   *
   * <p>Methods of NodeBuilder can be used in a static and in an instance context.
   * It isn't necessary to create an instance of NodeBuilder, unless it is
   * configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
   * to which created nodes are added.</p>
   *
   * @example
   *  const NodeBuilder = require('josm/builder').NodeBuilder
   *  const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
   *
   *  const ds = new DataSet()
   *  // create a node builder without and underlying dataset ...
   *  let nbuilder = new NodeBuilder()
   *  // ... with an underlying dataset ....
   *  nbuilder =  new NodeBuilder(ds)
   *  // ... or using this factory method
   *  nbuilder = NodeBuilder.forDataSet(ds)
   *
   *  // create a new local node at position (0,0) without tags
   *  const n1 = nbuilder.create()
   *
   *  // create a new global node at a specific position with tags
   *  const n2 = nbuilder.withPosition(1,1).withTags({name: 'test'}).create(123456)
   *
   *  // create a new proxy for a global node
   *  // (an 'incomplete' node in JOSM terminology)
   *  const n3 = nbuilder.createProxy(123456)
   *
   * @class
   * @name NodeBuilder
   * @memberof josm/builder
   */

  /**
   * <p>Creates a new node builder.</p>
   *
   *  @param {org.openstreetmap.josm.data.osm.DataSet} ds (optional) the dataset,
   *      to which created objects are added
   *  @constructor
   *  @memberOf NodeBuilder
   *  @name NodeBuilder
   */
  exports.NodeBuilder = function (ds) {
    if (util.isSomething(ds)) {
      util.assert(ds instanceof DataSet,
        'Expected a JOSM dataset, got {0}', ds)
      this.ds = ds
    }
  }

  /**
   * <p>Creates or configures a NodeBuilder which will add created nodes
   * to the dataset <code>ds</code>.</p>
   *
   * @example
   * const builder = require('josm/builder')
   *
   * // create a new node builder building to a data set
   * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
   * const ds = new DataSet()
   * let nb = builder.NodeBuilder.forDataSet(ds)
   *
   * // configure an existing node builder
   * nb = new builder.NodeBuilder()
   * nb = nb.forDataSet(ds)
   *
   * @memberOf NodeBuilder
   * @method
   * @name forDataSet
   * @return the node builder
   * @param {org.openstreetmap.josm.data.osm.DataSet} ds the dataset which
   *         created objects are added to
   * @type NodeBuilder
   * @summary Creates a new NodeBuilder for a specific
   *         {@class org.openstreetmap.josm.data.osm.DataSet}.
   */
  function forDataSet (ds) {
    const builder = receiver(this)
    util.assert(util.isSomething(ds),
      'Expected a non-null defined object, got {0}', ds)
    util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
    builder.ds = ds
    return builder
  }
  exports.NodeBuilder.prototype.forDataSet = forDataSet
  exports.NodeBuilder.forDataSet = forDataSet

  function receiver (that) {
    return typeof that === 'object' ? that : new exports.NodeBuilder()
  }

  function checkLat (value) {
    if (!(util.isSomething(value) && util.isNumber(value) && LatLon.isValidLat(value))) {
      throw new Error(`invalid lat value, got '${value}`)
    }
    return value
  }

  function checkLon (value) {
    if (!(util.isSomething(value) && util.isNumber(value) && LatLon.isValidLon(value))) {
      throw new Error(`invalid lon value, got '${value}`)
    }
    return value
  }

  function initFromObject (builder, args) {
    rememberIdFromObject(builder, args)
    rememberVersionFromObject(builder, args)
    rememberPosFromObject(builder, args)
    rememberTagsFromObject(builder, args)
  }

  /**
   * <p>Creates a new  {@class org.openstreetmap.josm.data.osm.Node}.</p>
   *
   * <p>Can be used in an instance or in a static context.</p>.
   *
   * <strong>Optional named arguments in the parameters <code>args</code>
   * </strong>
   * <ul>
   *   <li><code>version</code> - the version of a global node (number > 0)</li>
   *   <li><code>lat</code> - a valide latitude (number in the range
   *       [-90,90])</li>
   *   <li><code>lon</code> - a valide longitude (number in the range
   *       [-180,180])</li>
   *   <li><code>pos</code> - either an array <code>[lat,lon]</code>,
   *       an object <code>{lat: ..., lon: ...}</code>,
   *   or an instance of {@class org.openstreetmap.josm.data.coor.LatLon}</li>
   *   <li><code>tags</code> - an object with tags. Null values and undefined
   *       values are ignored. Any other value is converted to a string.
   *       Leading and trailing white space in keys is removed.</li>
   * </ul>
   *
   *
   * @example
   * const nb = require('josm/builder').NodeBuilder
   * // create a new local node at position [0,0]
   * const n1 = nb.create()
   *
   * // create a new global node at position [0,0]
   * const n2 = nb.create(12345)
   *
   * // create a new global way with version 3 at a specific position
   * // and with some tags
   * const n3 = nb.create(12345, {version: 3, lat: 23.45,
   *     lon: 87.23, tags: {amenity: 'restaurant'}
   * })
   *
   * @memberOf NodeBuilder
   * @method
   * @param {number}  id (optional) a global node id. Optional. If missing and
   *     not set before using
   *    <code>withId(..)</code>, creates a new local id.
   * @param {object} args (optional) additional parameters for creating the node
   * @type org.openstreetmap.josm.data.osm.Node
   * @summary Creates a new  {@class org.openstreetmap.josm.data.osm.Node}
   *
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

  exports.NodeBuilder.create = create
  exports.NodeBuilder.prototype.create = create

  /**
   * <p>Creates a new <em>proxy</em>
   * {@class org.openstreetmap.josm.data.osm.Node}. A proxy node is a node,
   * for which we only know its global id. In order to know more details
   * (position, tags, etc.), we would have to download it from the OSM server.
   * </p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const nbuilder = require('josm/builder').NodeBuilder
   *
   * // a new proxy node for the global node with id 12345
   * cons n1 = nbuilder.createProxy(12345)
   *
   * @memberOf NodeBuilder
   * @method
   * @param {number} id  (mandatory) the node id (not null, number > 0 expected)
   * @return the new proxy node
   * @type org.openstreetmap.josm.data.osm.Node
   * @summary Creates a new <em>proxy</em>
   *         {@class org.openstreetmap.josm.data.osm.Node}
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

  exports.NodeBuilder.prototype.createProxy =
    exports.NodeBuilder.createProxy =
    createProxy

  /**
   * <p>Declares the node position.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const nbuilder = require('josm/builder').NodeBuilder
   *
   * // a new global node with the global id 12345 at position (34,45)
   * const n1 = nbuilder.withPosition(34,45).create(12345)
   *
   * // a new local node at position (23.2, 87.33)
   * const n2 = nbuilder.withPosition(23.3,87.33).create()
   *
   * @memberOf NodeBuilder
   * @method
   * @param {Number} lat  (mandatory) the latitude.
   *     A number in the range [-90..90].
   * @param {Number} lon (mandatory) the longitude.
   *     A number in the range [-180..180].
   * @return a node builder (for method chaining)
   * @type NodeBuilder
   * @summary Declares the node position.
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
  exports.NodeBuilder.prototype.withPosition = withPosition
  exports.NodeBuilder.withPosition = withPosition

  /**
   * <p>Declares the tags to be assigned to the new node.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const nbuilder = require('josm/builder').NodeBuilder
   *
   * // a new global  node with the global id 12345 and tags name=test and
   * // highway=road
   * const n1 = nbuilder.withTags({'name':'test', 'highway':'road'}).global(12345)
   *
   * // a new local node tags name=test and highway=road
   * const tags = {
   *      'name'    : 'test',
   *      'highway' : 'road'
   * }
   * const n2 = nbuilder.withTags(tags).local()
   *
   * @memberOf NodeBuilder
   * @method
   * @param {object} tags  (optional) the tags
   * @return a node builder (for method chaining)
   * @name withTags
   * @type NodeBuilder
   * @summary Declares the node tags.
   */
  function withTags (tags) {
    const builder = typeof this === 'object' ? this : new exports.NodeBuilder()
    rememberTags(builder, tags)
    return builder
  }
  exports.NodeBuilder.prototype.withTags = withTags
  exports.NodeBuilder.withTags = withTags

  /**
   * <p>Declares the global node id and the global node version.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @memberOf NodeBuilder
   * @method
   * @param {number} id  (mandatory) the global node id. A number > 0.
   * @param {number} version (optional) the global node version. If present,
   *     a number > 0. If missing,
   * the version 1 is assumed.
   * @return a node builder (for method chaining)
   * @name withId
   * @summary Declares the node id and version.
   */
  function withId (id, version) {
    const builder = typeof this === 'object' ? this : new exports.NodeBuilder()
    rememberId(builder, id, version)
    return builder
  }
  exports.NodeBuilder.prototype.withId = withId
  exports.NodeBuilder.withId = withId
}()); // end submodul NodeBuilder

// ----------------------------------------------------------------------------
// WayBuilder
// ----------------------------------------------------------------------------
(function () { // start submodul WayBuilder
  function receiver (that) {
    return typeof that === 'object' ? that : new exports.WayBuilder()
  }

  /**
  * <p>WayBuilder helps to create OSM
  * {@class org.openstreetmap.josm.data.osm.Way}s.</p>
  *
  * <p>Methods of WayBuilder can be used in a static and in an instance context.
  * It isn't necessary to create an instance of WayBuilder, unless it is
  * configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
  * to which created ways are added.</p>
  * @example
  *  const WayBuilder = require('josm/builder').WayBuilder
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
  *  const w2 = wbuilder.withTags({highway: 'residential'}).create(123456)
  *
  *  // create a new proxy for a global way
  *  // (an 'incomplete' node in JOSM terminology)
  *  const w3 = wbuilder.createProxy(123456)
  *
  * @class WayBuilder
  * @memberof josm/builder
  */

  /**
   * <p>Creates a new builder for OSM ways</p>
   *
   * @constructor
   * @memberOf WayBuilder
   * @name WayBuilder
   * @param {org.openstreetmap.josm.data.osm.DataSet} ds (optional) a JOSM
   *    dataset which created ways are added to. If missing, the created ways
   *    aren't added to a dataset.
   */
  exports.WayBuilder = function (ds) {
    if (util.isSomething(ds)) {
      util.assert(ds instanceof DataSet, 'Expected a DataSet, got {0}', ds)
      this.ds = ds
    }
    this.nodes = []
  }

  /**
   * <p>Creates or configures a WayBuilder which will add created nodes
   * to the dataset <code>ds</code>.</p>
   *
   * @example
   * const builder = require('josm/builder')
   *
   * // create a new way builder building to a data set
   * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet()')
   * const ds = new DataSet()
   * let wb = builder.WayBuilder.forDataSet(ds)
   *
   * // configure an existing way builder
   * wb = new builder.WayBuilder()
   * wb = wb.forDataSet(ds)
   *
   * @memberOf WayBuilder
   * @method
   * @name forDataSet
   * @return the way builder
   * @type WayBuilder
   * @summary Creates a new WayBuilder with an underlying dataset.
   */
  function forDataSet (ds) {
    const builder = receiver(this)
    util.assert(util.isSomething(ds),
      'Expected a non-null defined object, got {0}', ds)
    util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
    builder.ds = ds
    return builder
  }
  exports.WayBuilder.prototype.forDataSet = forDataSet
  exports.WayBuilder.forDataSet = forDataSet

  /**
   * <p>Declares the global way id and the global way version.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const wbuilder = require('josm/builder').WayBuilder
   * // creates a global way with id 12345 an version 12
   * const wb = wbuilder.withId(12345, 12).create()
   *
   * @memberof WayBuilder
   * @param {number} id  (mandatory) the global way id. A number > 0.
   * @param {number} version  (optional) the global way version. If present,
   *    a number > 0. If missing, the version 1 is assumed.
   * @return a way builder (for method chaining)
   * @type WayBuilder
   * @summary Declares the global way id and the global way version.
   */
  function withId (id, version) {
    const builder = receiver(this)
    rememberId(builder, id, version)
    return builder
  }

  exports.WayBuilder.prototype.withId = withId
  exports.WayBuilder.withId = withId

  /**
   * <p>Declares the tags to be assigned to the new way.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const wbuilder = require('josm/builder').WayBuilder
   * // a new global way with the global id 12345 and tags name='Laubeggstrasse'
   * // and highway=residential
   * const w1 = wbuilder.withTags({name:'Laubeggstrasse', highway:'residential'})
   *     .create(12345)
   *
   * // a new local node tags name=test and highway=road
   * const tags = {
   *      name    : 'Laubeggstrasse',
   *      highway : 'residential'
   * }
   * const w2 = wbuilder.withTags(tags).create()
   *
   * @memberOf WayBuilder
   * @param {object} tags  (optional) the tags
   * @return a way builder (for method chaining)
   * @type WayBuilder
   * @summary Declares the tags to be assigned to the new way.
   */
  function withTags (tags) {
    const builder = receiver(this)
    rememberTags(builder, tags)
    return builder
  }
  exports.WayBuilder.prototype.withTags = withTags
  exports.WayBuilder.withTags = withTags

  /**
   * <p>Declares the nodes of the way.</p>
   *
   * <p>Accepts either a vararg list of
   * {@class org.openstreetmap.josm.data.osm.Node},
   * an array of {@class org.openstreetmap.josm.data.osm.Node}s or a Java list
   * of {@class org.openstreetmap.josm.data.osm.Node}s. At least <strong>two
   * non-identical nodes</strong> have to be supplied.
   * The same node can occure more than once in the list, but a consecutive
   * sequence of the same node is collapsed to one node.
   * </p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * oonst wbuilder = require('josm/builder').WayBuilder
   * const nbuilder = require('josm/builder').NodeBuilder
   * // creates a new local way with two local nodes
   * const way = builder.withNodes(
   *    nbuilder.create(), nbuilder.create()
   * ).create()
   *
   * @memberOf WayBuilder
   * @param nodes  the list of nodes. See description and examples.
   * @return a way builder (for method chaining)
   * @type WayBuilder
   * @summary Declares the nodes of the way.
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
          var temp = []
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
      if (last && last.id === n.id) continue
      newnodes.push(n)
      last = n
    }
    builder.nodes = newnodes
    return builder
  }

  exports.WayBuilder.withNodes =
    exports.WayBuilder.prototype.withNodes =
    withNodes

  /**
   * <p>Creates a new <em>proxy</em> way. A proxy way is a way, for which we
   * only know its global id. In order to know more details (nodes, tags, etc.),
   * we would have to download it from the OSM server.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const wbuilder = require('josm/builder').WayBuilder
   *
   * // a new proxy way for the global way with id 12345
   * const w1 = wbuilder.createProxy(12345)
   *
   * @memberOf WayBuilder
   * @method
   * @return the new proxy way
   * @type org.openstreetmap.josm.data.osm.Way
   * @summary Creates a new proxy way
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
  exports.WayBuilder.createProxy =
    exports.WayBuilder.prototype.createProxy =
    createProxy

  function assignWayAttributes (builder, way) {
    if (util.hasProperties(builder.tags)) {
      assignTags(way, builder.tags)
    }
    if (builder.nodes.length > 0) {
      way.setNodes(builder.nodes)
    }
  }

  function rememberNodesFromObject (builder, args) {
    if (!args.hasOwnProperty('nodes')) return
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
   * <p>Creates a new way.</p>
   *
   * <p>Can be used in an instance or in a static context.</p>
   *
   * Optional named arguments in the parameters <code>options</code>:
   * <dl>
   *   <dt><code class='signature'>id</code>:number</dt>
   *   <dd>the id of a global way (number > 0)</dd>
   *
   *   <dt><code class='signature'>version</code>:number</dt>
   *   <dd>the version of a global way (number > 0)</dd>
   *
   *   <dt><code class='signature'>nodes</code>:array|list</dt>
   *   <dd>an array or a list of nodes</dd>
   *
   *   <dt><code class='signature'>tags</code>:object</dt>
   *   <dd>an object with tags. Null values and undefined values are ignored.
   *   Any other value is converted to a string. Leading and trailing white
   *   space in keys is removed.</dd>
   * </dl>
   *
   * @example
   * const wb = require('josm/builder').WayBuilder
   * // create a new local way
   * const w1 = wb.create()
   *
   * // create a new global way
   * const w2 = wb.create(12345)
   *
   * // create a new global way with version 3 at a specific position and with
   * // some tags
   * const w3 = wb.create(12345, {
   *    version: 3,
   *    tags: {amenity: 'restaurant'},
   *    nodes: [n1,n2,n3]
   *  })
   *
   * @memberOf WayBuilder
   * @method
   * @param {number}  id (optional) a global way id. If missing and not set
   *    before using <code>withId(..)</code>, creates a new local id.
   * @param {object} options (optional) additional parameters for creating the way
   * @type org.openstreetmap.josm.data.osm.Way
   * @summary Creates a new way
   */
  function create () {
    const builder = receiver(this)
    let arg
    switch (arguments.length){
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
        } else if (typeof arg == 'object') {
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

  exports.WayBuilder.create = create
  exports.WayBuilder.prototype.create = create
}()); // end submodul WayBuilder

// ----------------------------------------------------------------------------
// RelationBuilder
// ----------------------------------------------------------------------------
(function () { // start submodul Relation Builder
  function receiver (that) {
    return typeof that === 'object' ? that : new exports.RelationBuilder()
  }

  /**
  * <p>RelationBuilder helps to create OSM
  * {@class org.openstreetmap.josm.data.osm.Relation}s.</p>
  *
  * <p>Methods of RelationBuilder can be used in a static and in an instance
  * context.
  * It isn't necessary to create an instance of RelationBuilder, unless it is
  * configured with a {@class org.openstreetmap.josm.data.osm.DataSet},
  * which created ways are added to.</p>
  * @example
  * const RelationBuilder = require('josm/builder').RelationBuilder
  * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
  *
  * const ds = new DataSet()
  * // create a relation builder without and underlying dataset ...
  * const rbuilder = new RelationBuilder()
  * // ... with an underlying dataset ...
  * rbuilder =  new RelationBuilder(ds)
  * // ... or using this factory method
  * rbuilder = RelationBuilder.forDataSet(ds)
  *
  * // create a new local relation
  * const r1 = rbuilder.create()
  *
  * // create a new global way
  * const r2 = rbuilder.withTags({route: 'bicycle'}).create(123456)
  *
  * // create a new proxy for a global relation
  * // (an 'incomplete' node in JOSM terminology)
  * const r3 = rbuilder.createProxy(123456)
  *
  * @class RelationBuilder
  * @memberof josm/builder
  */

  /**
   * <p>Creates a new builder for OSM relations</p>
   *
   * @constructor
   * @name RelationBuilder
   * @memberOf RelationBuilder
   * @param {org.openstreetmap.josm.data.osm.DataSet} ds (optional) a JOSM
   *     dataset which created ways are added to. If missing, the created ways
   *     aren't added to a dataset.
   */
  exports.RelationBuilder = function (ds) {
    if (util.isSomething(ds)) {
      util.assert(ds instanceof DataSet, 'Expected a DataSet, got {0}', ds)
      this.ds = ds
    }
    this.members = []
  }

  /**
   * <p>Creates or configures a RelationBuilder which will add created nodes
   * to the dataset <code>ds</code>.</p>
   *
   * @example
   * const builder = require('josm/builder')
   *
   * // create a new relation builder building to a data set
   * const DataSet = Java.type('org.openstreetmap.josm.data.osm')
   * const ds = new DataSet()
   * const rb = builder.RelationBuilder.forDataSet(ds)
   *
   * // configure an existing relation builder
   * let wb = new builder.RelationBuilder()
   * wb = wb.forDataSet(ds)
   *
   * @memberOf RelationBuilder
   * @method
   * @name forDataSet
   * @return the relation builder
   * @type RelationBuilder
   * @summary Creates a new RelationBuilder which adds created relations to a
   *     dataset
   */
  function forDataSet (ds) {
    const builder = receiver(this)
    util.assert(util.isSomething(ds),
      'Expected a non-null defined object, got {0}', ds)
    util.assert(ds instanceof DataSet, 'Expected a JOSM dataset, got {0}', ds)
    builder.ds = ds
    return builder
  }
  exports.RelationBuilder.prototype.forDataSet = forDataSet
  exports.RelationBuilder.forDataSet = forDataSet

  /**
   * <p>Create a RelationMember</p>
   *
   * <dl>
   *   <dt>member(role, obj)</dt>
   *   <dd>Create a relation member with role <var>role</var> and member object
   *   <var>obj</var>. <var>role</var> can be null or undefined, obj must neither
   *   be null nor undefinde. <var>role</var> is a string, <var>obj</var> is an
   *   OSM node, a way, or a relation.
   *   </dd>
   *   <dt>member(obj)</dt>
   *  <dd>Create a relation member for the member object <var>obj</var>.
   *   <var>obj</var> must neither be null nor undefinde. <var>obj</var> is an
   *   OSM node, a way, or a relation. The created relation member has no role.
   *   </dd>
   * </dl>
   *
   * @example
   * const member = require('josm/builder').RelationBuilder.member
   * const nb = require('josm/builder').NodeBuilder
   *
   * // create a new RelationMember with role 'house' for a new node
   * const m1 = member('house', nb.create())
   * // create a new RelationMember with an empty role for a new node
   * const m2 = member(nb.create())
   *
   * @static
   * @name member
   * @memberOf RelationMember
   * @method
   * @type org.openstreetmap.josm.data.osm.RelationMember
   * @summary Utility function - creates a relation member
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

  exports.RelationBuilder.member = member

  /**
   * <p>Declares the global relation id and the global relation version.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const rbuilder = require('josm/builder').RelationBuilder
   * // creates a global relation with id 12345 an version 12
   * const r = rbuilder.withId(12345, 12).create()
   *
   * @memberof RelationBuilder
   * @param {number} id  (mandatory) the global relation id. A number > 0.
   * @param {number} version  (optional) the global relation version. If present,
   *    a number > 0. If missing, the version 1 is assumed.
   * @return the relation builder (for method chaining)
   * @type RelationBuilder
   * @summary Declares the relation id and version.
   */
  function withId (id, version) {
    const builder = receiver(this)
    rememberId(builder, id, version)
    return builder
  }
  exports.RelationBuilder.prototype.withId = withId
  exports.RelationBuilder.withId = withId

  /**
   * <p>Declares the tags to be assigned to the new relation.</p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const rbuilder = require('josm/builder').RelationBuilder
   * // a new global relation with the global id 12345 and tags route='bicycle'
   * //and name='n8'
   * const r1 = rbuilder.withTags({name:'n8', route:'bicycle'}).create(12345)
   *
   * // a new local node tags name=test and highway=road
   * const tags = {
   *      name  : 'n8',
   *      route : 'bicycle'
   * }
   * const r2 = rbuilder.withTags(tags).create()
   *
   * @memberOf RelationBuilder
   * @param {object} tags  (optional) the tags
   * @return a relation builder (for method chaining)
   * @type RelationBuilder
   * @summary Declares the tags to be assigned to the new relation.
   */
  function withTags (tags) {
    const builder = receiver(this)
    rememberTags(builder, tags)
    return builder
  }
  exports.RelationBuilder.prototype.withTags = withTags
  exports.RelationBuilder.withTags = withTags

  /**
   * <p>Creates a new <em>proxy</em> relation. A proxy relation is a relation,
   * for which we only know its global id. In order to know more details
   * (members, tags, etc.), we would have to download it from the OSM server.
   * </p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const rbuilder = require('josm/builder').RelationBuilder
   *
   * // a new proxy relation for the global way with id 12345
   * const r1 = rbuilder.createProxy(12345)
   *
   * @memberOf RelationBuilder
   * @method
   * @return the new proxy relation
   * @type org.openstreetmap.josm.data.osm.Relation
   * @summary Creates a new <em>proxy</em> relation.
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
  exports.RelationBuilder.createProxy = createProxy
  exports.RelationBuilder.prototype.createProxy = createProxy

  /**
   * <p>Declares the members of a relation.</p>
   *
   * <p>Accepts either a vararg list of relation members, nodes, ways or
   * relations, an array of relation members, nodes ways or relations, or a
   * Java list of members, nodes, ways or relation.
   * </p>
   *
   * <p>The method can be used in a static and in an instance context.</p>
   *
   * @example
   * const rbuilder = require('josm/builder').RelationBuilder
   * const nbuilder = require('josm/builder').NodeBuilder
   * const wbuilder = require('josm/builder').WayBuilder
   * const member = require('josm/builder').RelationBuilder.member
   *
   * const r1 = rbuilder.withMembers(
   *   member('house', nbuilder.create()),
   *   member('house', nbuilder.create()),
   *   member('street', wbuilder.create())
   * ).create()
   *
   *
   * @memberOf RelationBuilder
   * @param nodes  the list of members. See description and examples.
   * @return the relation builder (for method chaining)
   * @type RelationBuilder
   * @summary Declares the members of a relation.
   */
  function withMembers () {
    const builder = receiver(this)
    const members = []

    function remember (obj) {
      if (util.isNothing(obj)) return
      if (obj instanceof OsmPrimitive) {
        members.push(new RelationMember(null, obj))
      } else if (obj instanceof RelationMember)  {
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

  exports.RelationBuilder.withMembers = withMembers
  exports.RelationBuilder.prototype.withMembers = withMembers

  function rememberMembersFromObject (builder, args) {
    if (!args.hasOwnProperty('members')) return
    const o = args.members
    if (!util.isSomething(o)) return
    util.assert(util.isArray(o) || o instanceof List,
      'members: Expected an array or an instance of java.util.List, got {0}',
      o)
    builder.withMembers(o)
  }

  function initFromObject (builder, args) {
    rememberIdFromObject(builder, args)
    rememberVersionFromObject(builder,args)
    rememberTagsFromObject(builder, args)
    rememberMembersFromObject(builder, args)
  }

  /**
   * <p>Creates a new relation.</p>
   *
   * <p>Can be used in an instance or in a static context.</p>
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
   * const rb = require('josm/builder').RelationBuilder
   * const nb = require('josm/builder').NodeBuilder
   * const member = rb.member
   * // create a new local relation
   * const r1 = rb.create()
   *
   * // create a new global relation
   * const r2 = rb.create(12345)
   *
   * // create a new global relation with version 3 with some tags and two
   * // members
   * const r3 = rb.create(12345, {
   *    version: 3,
   *    tags: {type: 'route'},
   *    members: [member('house', nb.create()), member(nb.create())]
   *  })
   *
   * @memberOf RelationBuilder
   * @method
   * @param {number}  id (optional) a global way id. If missing and not set
   *     before using <code>withId(..)</code>, creates a new local id.
   * @param {object} args (optional) additional parameters for creating the way
   * @type org.openstreetmap.josm.data.osm.Relation
   * @summary Creates a new relation.
   */
  function create () {
    const builder = receiver(this)
    let arg
    switch (arguments.length){
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
  exports.RelationBuilder.create = create
  exports.RelationBuilder.prototype.create = create
}()) // end submodul Relation Builder
