/**
 * Provides utitly methods for data sets
 *
 * @module josm/ds
 */

/* global Java */
/* global require */

const util = require('josm/util')
const { NodeBuilder, WayBuilder, RelationBuilder } = require('josm/builder')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const SimplePrimitiveId = Java.type('org.openstreetmap.josm.data.osm.SimplePrimitiveId')
const PrimitiveId = Java.type('org.openstreetmap.josm.data.osm.PrimitiveId')
const OsmPrimitiveType = Java.type('org.openstreetmap.josm.data.osm.OsmPrimitiveType')
const Collection = Java.type('java.util.Collection')
const HashSet = Java.type('java.util.HashSet')
const File = Java.type('java.io.File')
const FileWriter = Java.type('java.io.FileWriter')
const PrintWriter = Java.type('java.io.PrintWriter')
const FileInputStream = Java.type('java.io.FileInputStream')
const OsmImporter = Java.type('org.openstreetmap.josm.gui.io.importexport.OsmImporter')
const OsmChangeImporter = Java.type('org.openstreetmap.josm.gui.io.importexport.OsmChangeImporter')
const OsmReader = Java.type('org.openstreetmap.josm.io.OsmReader')
const OsmChangeReader = Java.type('org.openstreetmap.josm.io.OsmChangeReader')
const Utils = Java.type('org.openstreetmap.josm.tools.Utils')
const GZIPInputStream = Java.type('java.util.zip.GZIPInputStream')
const OsmWriterFactory = Java.type('org.openstreetmap.josm.io.OsmWriterFactory')
const Changeset = Java.type('org.openstreetmap.josm.data.osm.Changeset')
const System = Java.type('java.lang.System')

function log (msg) {
  System.out.println(msg)
}

function normalizeType (type) {
  if (util.isString(type)) {
    type = type.trim().toLowerCase()
    if ('node'.startsWith(type)) {
      return OsmPrimitiveType.NODE
    } else if ('way'.startsWith(type)) {
      return OsmPrimitiveType.WAY
    } else if ('relation'.startsWith(type)) {
      return OsmPrimitiveType.RELATION
    } else {
      util.assert(false,
        'expected type as string, i.e. "node", "way", or "relation", got "{0}"',
        type)
    }
  } else if (type instanceof OsmPrimitiveType) {
    return type
  } else {
    util.assert(false, 'expected String or OsmPrimitiveType, got "{0}", type')
  }
}

function normalizeId (id) {
  if (util.isNumber(id)) {
    if (Number.isInteger(id)) {
      return id
    } else {
      util.assert(false, 'expected integer , got "{0}"', id)
    }
  } else if (util.isString(id)) {
    const idSaved = id
    id = parseInt(id.trim())
    if (isNaN(id)) {
      util.assert(false, 'expected integer as string, got "{0}"', idSaved)
    }
  } else {
    util.assert(false, 'expected an integer or a string, got "{0}"', id)
  }
}

/**
* Creates an ID for an OSM primitive.
*
* <strong>Signatures</strong>
* <dl>
*   <dt><code class="signature">buildId(id, type)</code></dt>
*   <dd class="param-desc">Replies an object given by its unique numeric id and a type.
*   The type is either a string <code>node</code>, <code>way</code>, or
*   <code>relation</code>, or one of the symbols
*   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.NODE,
*   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.WAY, or
*   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.RELATION.</dd>
*
*   <dt><code class="signature">buildId(id)</code></dt>
*   <dd class="param-desc">Replies an object given an ID. <code>id</code> is either an instance
*   of
*   {@class org.openstreetmap.josm.data.osm.PrimitiveId} or an object with
*   the properties <code>id</code> and <code>type</code>, i.e.
*   <code>{id: 1234, type: 'node'}</code>.</dd>
* </dl>
*
* @example
* const { buildId, OsmPrimitiveType} = require('josm/ds')
*
* // build a node id
* const id1 = buildId(1234, 'node')
*
* // build a way id
* const id2 = buildId(3333, OsmPrimitiveType.WAY)
*
* // build a relation id
* const id3 = buildId({id: 5423, type: 'relation'})
*
*
* @param args see description
*/
function buildId (id, type) {
  function buildId2 (id, type) {
    id = normalizeId(id)
    type = normalizeType(type)
    if (id === 0) {
      util.assert(false, 'expected id != 0, got 0')
    }
    return new SimplePrimitiveId(id, type)
  }

  function buildId1 (id) {
    if (id instanceof PrimitiveId) {
      return id
    }
    if (util.hasProp(id, 'id') && util.hasProp(id, 'type')) {
      return buildId2(id.id, id.type)
    }
    util.assert(false, 'expected PrimitiveId or {id: ..., type: ...}, got "{0}"', id)
  }

  util.assert(arguments.length > 0, 'expected at least 1 argument, got 0')
  switch (arguments.length) {
    case 1:
      return buildId1(...arguments)
    case 2:
      return buildId2(...arguments)
    default:
      util.assert(false, 'expected 1 or 2 arguments, got {0}', arguments.length)
  }
}

function each (collection, delegate) {
  if (util.isArray(collection) || util.isArguments(collection)) {
    for (let i = 0; i < collection.length; i++) {
      delegate(collection[i])
    }
  } else if (collection instanceof Collection) {
    for (let it = collection.iterator(); it.hasNext();) {
      delegate(it.next())
    }
  } else {
    util.assert(false, 'Expected list or collection, got {0}', collection)
  }
}

function collect (collection, predicate) {
  const ret = []
  each(collection, (obj) => {
    if (predicate(obj)) ret.push(obj)
  })
  return ret
}

function isCollection (collection) {
  return util.isArray(collection) ||
    util.isArguments(collection) ||
    collection instanceof Collection
}

function normalizeIds () {
  function walk (set, ids) {
    if (util.isNothing(ids)) return
    if (ids instanceof PrimitiveId) {
      set.add(ids)
    } else if (isCollection(ids)) {
      each(ids, (that) => walk(set, that))
    } else {
      util.assert(false,
        'PrimitiveId or collection required, got {0}', ids)
    }
  }
  const set = new HashSet()
  for (let i = 0; i < arguments.length; i++) {
    walk(set, arguments[i])
  }
  return set
}

/**
 * <code>DataSetUtil</code> provides methods to build OSM primitive IDs and to
 * manipulate data in a {@class org.openstreetmap.josm.data.osm.DataSet}.
 *
 */
class DataSetUtil {
  /**
   * Creates an instane of <code>DataSetUtil</code> for a given {@class org.openstreetmap.josm.data.osm.DataSet}
   *
   * @example
   * const { DataSetUtil, DataSet } = require('josm/ds')
   * const dsutil = new DataSetUtil(new DataSet())
   *
   * @summary Build an utility object wrapping the dataset <code>ds</code>
   * @param {org.openstreetmap.josm.data.osm.DataSet} [ds] the dataset. Creates a new dataset if missing
   */
  constructor (ds) {
    ds = ds || new DataSet()
    this.ds = ds
  }

  /**
   * Replies an OSM object from the dataset, or undefined, if no such object
   * exists.
   *
   * <strong>Signatures</strong>
   * <dl>
   *   <dt><code class="signature">get(id, type)</code></dt>
   *   <dd class="param-desc">Replies an object given by its unique numeric id and a type.
   *   The type is either a string  "node", "way", or "relation", or one of
   *   the symbols
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.NODE,
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.WAY, or
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.RELATION.</dd>
   *
   *   <dt><code class="signature">get(id)</code></dt>
   *   <dd class="param-desc">Replies an object given an ID. <code>id</code> is either an instance
   *   of
   *   {@class org.openstreetmap.josm.data.osm.PrimitiveId} or an object with
   *   the properties <code>id</code> and <code>type</code>, i.e.
   *   <code>{id: 1234, type: "node"}</code>.</dd>
   * </dl>
   *
   * @example
   * const { buildId , DataSetUtil, DataSet, OsmPrimitiveType} = require('josm/ds')
   *
   * const dsutil = new DataSetUtil(new DataSet())
   * // get a node
   * const n1  = dsutil.get(1234, 'node')
   *
   * // get a way
   * const w1 =  dsutil.get(3333, OsmPrimitiveType.WAY)
   *
   * // get a relation
   * const r1 = dsutil.get({id: 5423, type: 'relation'})
   *
   * // pass in a SimplePrimitiveId
   * const id = buildId(-5, OsmPrimitiveType.NODE)
   * const n2 = dsutil.get(id)
   *
   * // pass in a primitive to get it
   * const w2 = dsutil.wayBuilder().create(987)
   * const w3 = dsutil.get(w2)
   *
   * @param args see description
   */
  get () {
    const id = buildId(...arguments)
    return this.ds.getPrimitiveById(id)
  }

  /**
  * Replies the node with id <code>id</code>, or null.
  *
  * @example
  * const { DataSet, DataSetUtil } = require('josm/ds')
  *
  * const dsutil = new DataSetUtil(new DataSet())
  * // get a node
  * const n = dsutil.node(1234)
  *
  * @param {number} id  the unique numeric id. Must not be 0.
  * @returns {org.openstreetmap.josm.data.osm.Node} the node
  */
  node (id) {
    util.assert(util.isSomething(id), 'expected defined id, got "{0}"', id)
    return this.get(id, 'node')
  }

  /**
  * Replies the way with id <code>id</code>, or null
  *
  * @example
  * const { DataSet, DataSetUtil } = require('josm/ds')
  *
  * const dsutil = new DataSetUtil(new DataSet())
  * // get a way
  * const w  = dsutil.way(1234)
  * @param {number} id  the unique numeric id. Must not be 0.
  * @returns {org.openstreetmap.josm.data.osm.Way} the way
  */
  way (id) {
    util.assert(util.isSomething(id), 'expected defined id, got "{0}"', id)
    return this.get(id, 'way')
  }

  /**
  * Replies the relation with id <code>id</code>.
  *
  * @example
  * const { DataSet, DataSetUtil } = require('josm/ds')
  *
  * const dsutil = new DataSetUtil(new DataSet())
  * // get a relation
  * const r  = dsutil.relation(1234)
  *
  * @param {number} id  the unique numeric id. Must not be 0.
  * @returns {org.openstreetmap.josm.data.osm.Relation} the relation
  */
  relation (id) {
    util.assert(util.isSomething(id), 'expected defined id, got "{0}"', id)
    return this.get(id, 'relation')
  }

  /**
   * Run a sequence of operations against the dataset in "batch mode".
   *
   * Listeners to data set events are only notified at the end of the batch.
   *
   * @example
   * const { DataSet, DataSetUtil } = require('josm/ds')
   * const dsutil = new DataSetUtil(new DataSet())
   * // creates and adds two nodes and a way in batch operation
   * // to the dataset
   * dsutil.batch(() => {
   *    const n1 = dsutil.nodeBuilder().create()
   *    const n2 = dsutil.nodeBuilder().create()
   *    dsutil.wayBuilder().withNodes(n1,n2).create()
   * })
   *
   * @param {function} delegate  the function implementing the batch process.
   *     Ignored if null or undefined.
   */
  batch (delegate) {
    if (!(util.isSomething(delegate))) {
      return
    }
    util.assert(util.isFunction(delegate), 'expected a function, got "{0}"',
      delegate)
    this.ds.beginUpdate()
    try {
      delegate()
    } finally {
      this.ds.endUpdate()
    }
  }

  /**
   * Removes objects from the dataset
   *
   * <strong>Signatures</strong>
   * <dl>
   *   <dt><code class="signature">remove(id, type)</code></dt>
   *   <dd class="param-desc">Removes a single object given by its unique numeric ID (nid) and a
   *   type. The type is either a string  "node", "way", or "relation", or one
   *   of the symbols
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.NODE,
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.WAY, or
   *   {@class org.openstreetmap.josm.data.osm.OsmPrimitiveType}.RELATION.</dd>
   *
   *   <dt><code class="signature">remove(id, id, ...)</code></dt>
   *   <dd class="param-desc">Removes a collection of objects given by the ids. <code>id</code> is
   *   either an instance of
   *   {@class org.openstreetmap.josm.data.osm.PrimitiveId} or an object with
   *   the properties <code>id</code> and <code>type</code>, i.e.
   *   <code>{id: 1234, type: "node"}</code>.
   *   null and undefined are ignored.</dd>
   *
   *   <dt><code class="signature">remove(array|collection)</code></dt>
   *   <dd class="param-desc">Removes a collection of objects given by the an array or a
   *   java.util.Collection of ids.
   *   The collection elemeents are either instances of
   *   {@class org.openstreetmap.josm.data.osm.PrimitiveId} or an object with
   *   the properties <code>id</code> and <code>type</code>, i.e.
   *   <code>{id: 1234, type: "node"}</code>.
   *   null or undefined elements are ignored.
   *   </dd>
   * </dl>
   *
   * @example
   * const { DataSet, DataSetUtil, OsmPrimitiveType, buildId} = require('josm/ds')
   * const HashSet = Java.type('java.util.HashSet')
   * const dsutil = new DataSetUtil(new DataSet())
   *
   * // remove a node with a global id
   * dsutil.remove(1234, ''ode')
   *
   * // remove a node and a way
   * const id1 = buildId(1234, 'node')
   * const id2 = buildId(3333, OsmPrimitiveType.WAY)
   * dsutil.remove(id1, id2)
   *
   * // remove a relation and a node
   * dsutil.remove({id: 1234, type: 'relation'}, id1)
   *
   * // remove an array of nodes
   * dsutil.remove([id1,id2])
   *
   * // remove a set of primitives
   * const ids = new HashSet()
   * ids.add(id1)
   * ids.add(id1)
   * dsutil.remove(ids)
   *
   * @param args see description
   */
  remove () {
    // we have exactly two arguments, id and type. If we succeed
    // to convert them to a primitive id, then we are done
    if (arguments.length === 2) {
      let id
      try {
        id = buildId(normalizeId(arguments[0]), normalizeType(arguments[1]))
      } catch (e) {
        id = null
      }
      if (id) {
        this.ds.removePrimitive(id)
        return
      }
    }

    // we have a list of ids or collections of ids to remove.
    // First build a flat list of the ids, then remove them
    // in a batch operation from the dataset
    const ids = normalizeIds(...arguments)
    const ds = this.ds
    this.batch(() => {
      each(ids, (id) => {
        ds.removePrimitive(id)
      })
    })
  }

  /**
   * Replies a node builder to create {@class org.openstreetmap.josm.data.osm.Node}s in this dataset.
   *
   * @example
   * const { DataSet, DataSetUtil } = require('josm/ds')
   * const dsutil = new DataSetUtil(new DataSet())
   * const n = dsutil.nodeBuilder
   *    .withId(1234,4567)
   *    .withTags({amenity: 'restaurant'})
   *    .create()
   * dsutil.has(n)
   *
   * @property {module:josm/builder~NodeBuilder} nodeBuilder
   * @readOnly
   */
  get nodeBuilder () {
    return NodeBuilder.forDataSet(this.ds)
  }

  /**
   * Replies a way builder to create ways in this dataset.
   *
   * @example
   * const { DataSet, DataSetUtil } = require('josm/ds')
   *
   * const dsutil = new DataSetUtil(new DataSet())
   * const nb = dsutil.nodeBuilder()
   * const w = dsutil.wayBuilder()
   *   .withNodes(nb.create(), nb.create())
   *   .create(1234, {tags: {highway: "residential"}})
   * dsutil.has(w)
   *
   * @property {module:josm/builder~WayBuilder} wayBuilder
   * @readOnly
   */
  get wayBuilder () {
    return WayBuilder.forDataSet(this.ds)
  }

  /**
   * Replies a relation builder to create relations in this dataset.
   *
   * @example
   * const { DataSet, DataSetUtil } = require('josm/ds')
   *
   * const dsutil = new DataSetUtil(new DataSet())
   * const r = dsutil.relationBuilder()
   *    .withId(8765,1234)
   *    .create({tags: {type: 'network'}})
   * ds.has(r)  // --> true
   *
   * @property  {module:josm/builder~RelationBuilder} relationBuilder
   * @readOnly
   */
  get relationBuilder () {
    return RelationBuilder.forDataSet(this.ds)
  }

  /**
   * Loads a dataset from a file.
   * <p>
   * Derives the format of the file from the file suffix, unless the named
   * option <code>options.format</code> is set.
   * <p>
   * <code>options</code> can contain the following named options:
   * <dl>
   *   <dt><code class="signature">format</code></dt>
   *   <dd class="param-desc">one of the strings <code>osm</code> (Open Street Map XML data),
   *   <code>osc</code> (Open Street Map change format), or
   *   <code>osm.gz</code> (Open Street Map XML data,
   *   compressed with gzip).  format is normalized by removing leading and
   *   trailing whitespace and conversion to lower case.</dd>
   * </dl>
   *
   * @example
   * const { DataSetUtil } = require('josm/ds')
   *
   * // loads an OSM file
   * DataSetUtil.load('/path/to/my/file.osm')
   *
   * // loads an OSM file, explicity passing in the format
   * DataSetUtil.load('/path/to/my/file.any-suffix', { format 'osm' })
   *
   * @param {string|java.io.File}  source  the data source
   * @param {object} [options]  optional named parameters
   */
  static load (source, options) {
    function normalizeFile (source) {
      if (source instanceof File) {
        return source
      } else if (util.isString(source)) {
        return new File(source)
      } else {
        util.assert(false,
          'source: illegal value, expected string or File, got {0}',
          source)
      }
    }

    function normalizeFormat (source, options) {
      const FORMATS = {
        osm: true,
        osc: true,
        'osm.gz': true
      }
      if (util.isSomething(options.format)) {
        // convert to string
        const format = util.trim(options.format + '').toLowerCase()
        if (FORMATS[format]) {
          return format
        }
        util.assert(false,
          `options.format: unknown format '${format}'`)
      } else {
        if (source.getPath().endsWith('.osm.gz')) {
          return 'osm.gz'
        }
        if (new OsmImporter().acceptFile(source)) {
          return 'osm'
        }
        if (new OsmChangeImporter().acceptFile(source)) {
          return 'osc'
        }
        util.assert(false,
          `Failed to derive format from file name. file is '${source}'`)
      }
    }

    util.assert(util.isSomething(source),
      'source: must not be null or undefined')
    options = options || {}
    source = normalizeFile(source)
    const format = normalizeFormat(source, options)
    log(`format: ${format}`)
    let is
    try {
      switch (format) {
        // load an OSM file
        case 'osm': {
          is = new FileInputStream(source)
          const other = OsmReader.parseDataSet(is,
            null /* null progress monitor */)
          return new DataSetUtil(other)
        }

        // load an OSC file
        case 'osc': {
          is = new FileInputStream(source)
          const other = OsmChangeReader.parseDataSet(is,
            null /* null progress monitor */)
          return new DataSetUtil(other)
        }

        // load a compressed OSM file
        case 'osm.gz': {
          is = new GZIPInputStream(new FileInputStream(source))
          const other = OsmReader.parseDataSet(is,
            null /* null progress monitor */)
          return new DataSetUtil(other)
        }

        default:
          util.assert(false,
            `unknown format '${format}'. Failed to load from ${source}`)
      }
    } finally {
      is && Utils.close(is)
    }
  }

  /**
   * Saves the dataset to a file (in OSM XML format).
   * <p>
   *
   * <code>options</code> can contain the following named options:
   * <dl>
   *   <dt><code class="signature">version</code>: string</dt>
   *   <dd class="param-desc">the value of the attribute <code>version</code> in the OSM file
   *   header. Default: "0.6"</dd>
   *
   *   <dt><codeclass="signature">changeset</code>: Changeset</dt>
   *   <dd class="param-desc">the changeset whose id is included in the attribute
   *   <code>changeset</code> on every OSM object. If undefined, includes the
   *   individual <code>changeset</code> attribute of the OSM object.
   *   Default: undefined</dd>
   *   <dt><codeclass="signature">osmConform</code>: bool</dt>
   *   <dd class="param-desc">if true, prevents modification attributes to be written
   *   Default: true</dd>
   * </dl>
   *
   * @example
   * const { DataSetUtil } = require('josm/ds')
   *
   * const dsutil = new DataSetUtil()
   * // create a node in the dataset
   * dsutil.nodeBuilder()
   *  .withId(1, 1)
   *  .withPosition({ lat: 1.0, lon: 1.0 })
   *  .create()
   *
   * // save the dataset
   * dsutil.save('/tmp/my-dataset.osm')
   *
   * @param {string|java.io.File}  target  the target file
   * @param {object} [options] optional named parameters
   * @instance
   */
  save (target, options) {
    function normalizeTarget (target) {
      util.assert(util.isSomething(target),
        'target: must not be null or undefined')
      if (util.isString(target)) {
        return new File(target)
      } else if (target instanceof File) {
        return target
      } else {
        util.assert(false,
          'target: unexpected type of value, got {0}', target)
      }
    }

    function normalizeOptions (options) {
      options = options || {}
      util.assert(
        !util.isDef(options.version) || util.isString(options.version),
        'options.version: expected a string, got {0}', options.version)
      options.version = options.version
        ? util.trim(options.version)
        : null /* default version */

      /// true, if not explicity set to false
      options.osmConform = options.osmConform !== false

      const changeset = options.changeset
      util.assert(
        !util.isDef(changeset) || changeset instanceof Changeset,
        'options.changeset: expected a changeset, got {0}', changeset)
      return options
    }

    target = normalizeTarget(target)
    options = normalizeOptions(options)
    let pw
    try {
      pw = new PrintWriter(new FileWriter(target))
      const writer = OsmWriterFactory.createOsmWriter(
        pw,
        options.osmConform,
        options.version)
      if (options.changeset) {
        writer.setChangeset(options.changeset)
      }
      try {
        this.ds.getReadLock().lock()
        writer.header()
        writer.writeContent(this.ds)
        writer.footer()
      } finally {
        this.ds.getReadLock().unlock()
      }
    } finally {
      pw && pw.close()
    }
  }

 /**
 * Queries the dataset
 * <p>
 * <strong>Signatures</strong>
 * <dl>
 *   <dt><code class="signature">query(josmSearchExpression,?options)</code>
 *   </dt>
 *   <dd class="param-desc">Queries the dataset using the JOSM search expression
 *   <code>josmSearchExpression</code>.
 *   <code>josmSearchExpression</code> is a string as you would enter it in
 *   the JOSM search dialog. <code>options</code> is an (optional) object
 *   with named parameters, see below.</dd>
 *
 *   <dt><code class="signature">query(predicate,?options)</code></dt>
 *   <dd class="param-desc">Queries the dataset using a javascript predicate function
 *   <code>predicate</code>.  <code>predicate</code> is a javascript
 *   function which accepts a object as parameter and replies
 *   true, when it matches for the object ans false otherwise.
 *   <code>options</code> is an (optional) object with named parameters,
 *   see below.</dd>
 * </dl>
 *
 * The parameter <code>options</code> consist of the following (optional)
 * named parameters:
 * <dl>
 *   <dt><code class="signature">allElements</code> : boolean
 *   (Deprecated parameter names:
 *       <code class="signature">all</code>)</dt>
 *   <dd class="param-desc">If true, searches <em>all</em> objects in the dataset. If false,
 *   ignores incomplete or deleted
 *   objects. Default: false.</dd>
 *
 *   <dt><code class="signature">caseSensitive</code> : boolean</dt>
 *   <dd class="param-desc"><strong>Only applicable for searches with a JOSM search
 *   expression</strong>. If true,  searches case sensitive. If false,
 *   searches case insensitive. Default: false.</dd>
 *
 *   <dt><code class="signature">regexSearch</code> : boolean (Deprecated
 *       parameter names:
 *        <code class="signature">withRegexp</code>,
 *       <code class="signature">regexpSearch</code>)</dt>
 *   <dd class="param-desc"><strong>Only applicable for searches with a JOSM search
 *   expression</strong>. If true,  the search expression contains regular
 *   expressions. If false, it includes only plain strings for searching.
 *   Default: false.</dd>
 *
 *   <dt><code class="signature">mapCSSSearch</code></dt>
 *   <dd class="param-desc"><strong>Only applies for searches with a JOSM search
 *   expression</strong>.
 *    Default: false.</dd>
 * </dl>
 *
 * @example
 * const { DataSetUtil } = require('josm/ds')
 * const dsutil = new DataSetUtil()
 * // add or load primitives to query
 * // ...
 *
 * // query restaurants
 * const result1 = dsutil.query('amenity=restaurant')
 *
 * // query all nodes with a type query
 * const result2 = dsutil.query('type:node')
 *
 * // query using a custom predicate - all primitives
 * // with exactly two tags
 * const result3 = dsutil.query((primitive) => {
  *  primitive.getKeys().size() === 2
 * })
 *
 * @param {string|function} expression  the match expression
 * @param {object} [options] additional named parameters
 * @instance
 */
  query (expression, options) {
    const SearchSetting = Java.type('org.openstreetmap.josm.data.osm.search.SearchSetting')
    const SearchCompiler = Java.type('org.openstreetmap.josm.data.osm.search.SearchCompiler')
    options = options || {}

    switch (arguments.length) {
      case 0: return []
      case 1:
      case 2:
        if (util.isString(expression)) {
          const ss = new SearchSetting()
          ss.caseSensitive = Boolean(options.caseSensitive)
          ss.regexSearch =
            Boolean(options.regexSearch) ||
            Boolean(options.regexpSearch) ||
            Boolean(options.withRegexp)
          ss.allElements =
            Boolean(options.all) ||
            Boolean(options.allElements)

          ss.mapCSSSearch = Boolean(options.mapCSSSearch)
          ss.text = expression
          const matcher = SearchCompiler.compile(ss)
          let predicate
          if (ss.allElements) {
            predicate = (matcher) => (obj) => {
              return matcher.match(obj)
            }
          } else {
            predicate = (matcher) => (obj) => {
              return obj.isUsable() && matcher.match(obj)
            }
          }
          return collect(this.ds.allPrimitives(), predicate(matcher))
        } else if (util.isFunction(expression)) {
          const all =
            Boolean(options.all) ||
            Boolean(options.allElements)
          let predicate = expression
          if (!all) {
            predicate = (obj) => {
              return obj.isUsable() && expression(obj)
            }
          }
          return collect(this.ds.allPrimitives(), predicate)
        } else {
          util.assert(false,
            'expression: Unexpected type of argument, got {0}',
            arguments[0])
        }
        break
      default:
        util.assert(false,
          'Expected a predicate, got {0} arguments', arguments.length)
    }
  }
}

module.exports = {
  DataSetUtil: DataSetUtil,
  buildId: buildId,
  DataSet: DataSet,
  OsmPrimitiveType: OsmPrimitiveType
}
