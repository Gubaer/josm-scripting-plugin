/**
 * Provides access to the JOSM layers.
 *
 * @module josm/layers
 */

/* global Java */
/* global require */

// -- imports
const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
const OsmDataLayer = Java.type('org.openstreetmap.josm.gui.layer.OsmDataLayer')
const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
const Layer = Java.type('org.openstreetmap.josm.gui.layer.Layer')
const util = require('josm/util')

/**
 * Replies the number of currently open layers.
 *
 * @readOnly
 * @property {number} length the number of layers
 * @summary Replies the number of currently open layers.
 * @name length
 * @static
 */
Object.defineProperty(exports, 'length', {
  get: function () {
    return MainApplication.getLayerManager().getLayers().size()
  }
})

/**
 * Set or get the active layer.
 *
 * <dl>
 *   <dt>get</dt>
 *   <dd class="param-desc">Replies the active layer or undefined.</dd>
 *
 *   <dt>set</dt>
 *   <dd class="param-desc">Assign either an existing {@class org.openstreetmap.josm.layer.Layer},
 *   the name of a layer as string, or a layer index as number.</dd>
 * </dl>
 *
 * @property {org.openstreetmap.josm.layer.Layer} activeLayer the active layer
 * @name activeLayer
 * @summary Set or get the active layer.
 * @static
 */
Object.defineProperty(exports, 'activeLayer', {
  get: function () {
    return MainApplication.getLayerManager().getActiveLayer()
  },
  set: function (value) {
    util.assert(util.isSomething(value),
      'Value must not be null or undefined)')
    let layer = null
    if (value instanceof Layer) {
      layer = value
    } else if (util.isNumber(value) || util.isString(value)) {
      layer = exports.get(value)
    } else {
      util.assert(false, 'Unexpected type of value, got {0}', value)
    }
    util.assert(util.isSomething(layer),
      'Layer \'\'{0}\'\' doesn\'\'t exist. It can\'\'t be set as active layer.',
      value)
    MainApplication.getLayerManager().setActiveLayer(layer)
  }
})

function getLayerByName (key) {
  key = util.trim(key).toLowerCase()
  if (exports.length === 0) return undefined
  const layers = MainApplication.getLayerManager().getLayers()
  for (let it = layers.iterator(); it.hasNext();) {
    const l = it.next()
    if (l.getName().trim().toLowerCase() === key) return l
  }
  return undefined
}

function getLayerByIndex (idx) {
  if (idx < 0 || idx >= exports.length) return undefined
  const layers = MainApplication.getLayerManager().getLayers()
  return layers.get(idx)
}

/**
 * Replies one of the layers given a key.
 *
 * <ul>
 *   <li>If <code>key</code> is a number, replies the layer with index key, or
 *   undefined, if no layer for this index exists.</li>
 *    <li>If <code>key</code> is a string, replies the first layer whose name
 *    is identical to key (case insensitive, without leading/trailing
 *    whitespace), or undefined, if no layer with such a name exists.</li>
 * </ul>
 *
 * @example
 * const layers = require('josm/layers')
 *
 * // get the first layer
 * const layer1  = layers.get(0)
 *
 * // get the first layer with name "data layer"
 * const layer2 = layers.get('data layer')
 *
 * @param {number|string} key the key to retrieve the layer
 * @summary Replies one of the layers given a key.
 * @function
 * @returns {org.openstreetmap.josm.layer.Layer}
 * @name get
 * @static
 */
exports.get = function (key) {
  if (util.isNothing(key)) return undefined
  if (util.isString(key)) return getLayerByName(key)
  if (util.isNumber(key)) return getLayerByIndex(key)
  return undefined
}

/**
 * Checks whether <code>layer</code> is a currently registered layer.
 *
 * @example
 * const layers = require('josm/layers')
 *
 * // is there a layer with name "my layer"?
 * let b = layers.has('my layer')
 *
 * // is there a layer at index position 2
 * b = layers.has(2)
 *
 * // is there a specific layer?
 * let l = layers.get(0)
 * b = layers.has(l)
 *
 * @param {org.openstreetmap.josm.gui.layer.Layer|string|number} layer a layer,
 *     a layer name, or a layer index
 * @returns {boolean }true, if the layer or at least one layer with the given name exists.
 *     False, otherwise.
 * @summary Checks whether <code>layer</code> is currently registered layer.
 * @function
 * @name has
 * @static
 */
exports.has = function (layer) {
  if (util.isNothing(layer)) return false
  const layerManager = MainApplication.getLayerManager()
  if (layer instanceof Layer) {
    return layerManager.getLayers().contains(layer)
  } else if (util.isString(layer)) {
    return util.isSomething(exports.get(layer))
  } else if (util.isNumber(layer)) {
    return layer >= 0 && layer < exports.length
  } else {
    return false
  }
}

/**
 * Adds a layer.
 * <p>
 * Either pass in a layer object or a data set. In the later case, an
 * {@class org.openstreetmap.josm.gui.layer.OsmDataLayer} is
 * automatically created.
 *
 * @example
 * const layers = require('josm/layers')
 * const OsmDataLayer = Java.type('org.openstreetmap.josm.gui.layer.OsmDataLayer')
 * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 *
 * const dataLayer = new OsmDataLayer(new DataSet(), null, null);
 * // add a layer ...
 * layers.add(dataLayer)
 *
 * // or add a dataset, which will create a data layer
 * const ds = new DataSet()
 * layer.add(ds)
 *
 * @param {org.openstreetmap.josm.gui.layer.Layer
 *     |org.openstreetmap.josm.data.osm.DataSet} obj  a layer to add,
 *      or a dataset.  Ignored if null or undefined.
 * @summary Adds a layer.
 * @returns {org.openstreetmap.josm.gui.layer.Layer} the added layer
 * @name add
 * @function
 * @static
 */
exports.add = function (obj) {
  if (util.isNothing(obj)) return
  const layerManager = MainApplication.getLayerManager()
  if (obj instanceof Layer) {
    layerManager.addLayer(obj)
  } else if (obj instanceof DataSet) {
    layerManager.addLayer(new OsmDataLayer(obj, null, null))
  } else {
    util.assert(false,
      'Expected an instance of Layer or DataSet, got {0}', obj)
  }
}

function removeLayerByIndex (idx) {
  const layer = exports.get(idx)
  if (util.isNothing(layer)) return
  MainApplication.getLayerManager().removeLayer(layer)
}

function removeLayerByName (name) {
  const layer = exports.get(name)
  if (util.isNothing(layer)) return
  MainApplication.getLayerManager().removeLayer(layer)
}

/**
 * Removes a layer with the given key.
 *
 * <ul>
 *   <li>If <code>key</code> is a <code>Number</code>, removes the layer with
 *   the index key. If the index doesn't isn't a valid layer index, nothing
 *   is removed.</li>
 *   <li>If <code>key</code> is a <code>string</code>, removes the layer with
 *   the name <code>key</code>. Leading and trailing white space is removed,
 *   matching is a case-insensitive sub-string match.</li>
 * </ul>
 * @example
 * const josm = require('josm')
 *
 * // remove the first layer
 * josm.layers.remove(0)
 *
 * // remove the first layer matching with the supplied name
 * josm.layers.remove('myLayerName')
 *
 * @param {number|string} key  indicates the layer to remove
 * @summary Removes a layer.
 * @function
 * @name remove
 * @static
 */
exports.remove = function (key) {
  if (util.isNothing(key)) return
  if (util.isNumber(key)) {
    removeLayerByIndex(key)
  } else if (util.isString(key)) {
    removeLayerByName(key)
  } else {
    util.assert(false, 'Expected a number or a string, got {0}', key)
  }
}

/**
 * Creates and adds a new data layer. The new layer becomes the new edit
 * layer.
 * <p>
 *
 * <string>Signatures</string>
 * <dl>
 *   <dt><code class="signature">addDataLayer()</code></dt>
 *   <dd class="param-desc">create data layer with a new dataset and default name</dd>
 *   <dt><code class="signature">addDataLayer(ds)</code></dt>
 *   <dd class="param-desc">create data layer with dataset ds and default name</dd>
 *   <dt><code class="signature">addDataLayer(name)</code></dt>
 *   <dd class="param-desc">create data layer with a new  dataset and name <code>name</code></dd>
 *   <dt><code class="signature">addDataLayer({name: ..., ds: ...})</code></dt>
 *   <dd class="param-desc">create data layer with a new  dataset and name <code>name</code></dd>
 * </dl>
 * @example
 * const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')
 *
 * // creates a new data layer
 * const l1 = josm.layers.addDataLayer()
 *
 * // creates a new data layer with name 'test'
 * const l2 = josm.layers.addDataLayer('test')
 *
 * // creates a new data layer for the dataset ds
 * const ds = new DataSet()
 * const l3 = josm.layers.addDataLayer(ds)
 *
 * @returns {org.openstreetmap.josm.gui.layer.OsmDataLayer} the added layer
 * @summary Adds a data layer
 * @function
 * @name addDataLayer
 * @param {string | org.openstreetmap.josm.data.osm.DataSet | object } args see description
 * @static
 */
exports.addDataLayer = function () {
  let name, ds
  switch (arguments.length) {
    case 0: break
    case 1:
      if (util.isString(arguments[0])) {
        name = util.trim(arguments[0])
      } else if (arguments[0] instanceof DataSet) {
        ds = arguments[0]
      } else if (typeof arguments[0] === 'object') {
        if (util.isString(arguments[0].name)) {
          name = util.trim(arguments[0].name)
        }
        if (arguments[0].ds instanceof DataSet) {
          ds = arguments[0].ds
        }
      } else {
        util.assert(false, 'unsupported type of argument, got {0}',
          arguments[0])
      }
      break
    default:
      util.assert(false, 'Unsupported number of arguments, got {0}',
        arguments.length)
  }
  ds = ds || new DataSet()
  name = name || OsmDataLayer.createNewName()
  const layer = new OsmDataLayer(ds, name, null /* no file */)
  exports.add(layer)
  return layer
}
