

import { suite, test, expectError } from 'josm/unittest'
import * as util from 'josm/util'
import { OsmPrimitiveType, DataSet, DataSetUtil, buildId } from 'josm/ds'
import {Api, ApiConfig} from 'josm/api'

const System = Java.type('java.lang.System')
const Exception = Java.type('java.lang.Exception')

const suites = []

suites.push(suite('downloadObject',
  test('can download a node', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    
    let ds = Api.downloadObject(265618079, 'node')
    let dsutil = new DataSetUtil(ds)
    let node = dsutil.node(265618079)
    util.assert(node)


    ds = Api.downloadObject(265618079, OsmPrimitiveType.NODE)
    dsutil = new DataSetUtil(ds)
    node = dsutil.node(265618079)
    util.assert(node)

    const id = buildId(265618079, OsmPrimitiveType.NODE)
    ds = Api.downloadObject(id)
    dsutil = new DataSetUtil(ds)
    node = dsutil.node(265618079)
    util.assert(node)

  }),

  test('can download a way', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    let ds = Api.downloadObject(24439022, 'way')
    let dsutil = new DataSetUtil(ds)
    let way = dsutil.way(24439022)
    util.assert(way)

    ds = Api.downloadObject(24439022, OsmPrimitiveType.WAY)
    dsutil = new DataSetUtil(ds)
    way = dsutil.way(24439022)
    util.assert(way)


    ds = Api.downloadObject(24439022, OsmPrimitiveType.WAY)
    dsutil = new DataSetUtil(ds)
    way = dsutil.way(24439022)
    util.assert(way)

  }),

  test('can download a relation', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    let ds = Api.downloadObject(188062, 'relation')
    let dsutil = new DataSetUtil(ds)
    let relation = dsutil.relation(188062)
    util.assert(relation)

    ds = Api.downloadObject(188062, OsmPrimitiveType.RELATION)
    dsutil = new DataSetUtil(ds)
    relation = dsutil.relation(188062)
    util.assert(relation)

    ds = Api.downloadObject(188062, OsmPrimitiveType.RELATION)
    dsutil = new DataSetUtil(ds)
    relation = dsutil.relation(188062)
    util.assert(relation)
  }),

  test('can download an old version of a relation', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const ds = Api.downloadObject(188062, 'relation', {version: 1})
    const dsutil = new DataSetUtil(ds)
    const relation = dsutil.relation(188062)
    util.assert(relation)
    util.assert(relation.getVersion() === 1, "unexpected version, got {0}", relation.getVersion())
  }),

  test('can download an ful version of a relation', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const ds = Api.downloadObject(188062, 'relation', {full: true})
    const dsutil = new DataSetUtil(ds)
    const relation = dsutil.relation(188062)
    util.assert(relation)
    util.assert(relation.getMembers().size() > 0)
  }),

  test('can download an old version of a way', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const ds = Api.downloadObject(24439022, 'way', {version: 1})
    const dsutil = new DataSetUtil(ds)
    const way = dsutil.way(24439022)
    util.assert(way)
    util.assert(way.getVersion() === 1, "unexpected version, got {0}", way.getVersion())
  }),

  test('can download a full version of a way', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const ds = Api.downloadObject(24439022, 'way', {full: true})
    const dsutil = new DataSetUtil(ds)
    const way = dsutil.way(24439022)
    util.assert(way)
    util.assert(way.getNodes().size() > 2)
  }),  
))


suites.push(suite('downloadReferrers',
  test('can download referrers to a node', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    
    let ds = Api.downloadReferrer(265618072, 'node')
    let dsutil = new DataSetUtil(ds)
    let ways = dsutil.query('type:way')
    util.assert(ways.length > 0)


    ds = Api.downloadReferrer(265618072, OsmPrimitiveType.NODE)
    dsutil = new DataSetUtil(ds)
    ways = dsutil.query('type:way')
    util.assert(ways.length > 0)

    const id = buildId(265618072, OsmPrimitiveType.NODE)
    ds = Api.downloadReferrer(id)
    dsutil = new DataSetUtil(ds)
    ways = dsutil.query('type:way')
    util.assert(ways.length > 0)
  }),

  test('can download referrers to a way', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    
    let ds = Api.downloadReferrer(24439022, 'way')
    let dsutil = new DataSetUtil(ds)
    let referrers = dsutil.query('type:relation')
    util.assert(referrers.length > 0)


    ds = Api.downloadReferrer(24439022, OsmPrimitiveType.WAY)
    dsutil = new DataSetUtil(ds)
    referrers = dsutil.query('type:relation')
    util.assert(referrers.length > 0)

    const id = buildId(24439022, OsmPrimitiveType.WAY)
    ds = Api.downloadReferrer(id)
    dsutil = new DataSetUtil(ds)
    referrers = dsutil.query('type:relation')
    util.assert(referrers.length > 0)
  }),

  test('can download referrers to a node, full=true', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    
    let ds = Api.downloadReferrer(265618072, 'node', {full: true})
    let dsutil = new DataSetUtil(ds)
    let ways = dsutil.query('type:way')
    util.assert(ways.length > 0)
  }),

  test('can download referrers to a way, full=true', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    
    let ds = Api.downloadReferrer(24439022, 'way', {full: true})
    let dsutil = new DataSetUtil(ds)
    let referrers = dsutil.query('type:relation')
    util.assert(referrers.length > 0)
    referrers = dsutil.query('type:way')
    util.assert(referrers.length > 2)
  }),
))

suites.push(suite('downloadArea',
  test('can download an area - 01', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const dataset = Api.downloadArea({
        min: {lat: 46.9479186, lon: 7.4619484},
        max: {lat: 46.9497642, lon: 7.4660683}
    })
    let dsutil = new DataSetUtil(dataset)
    let nodes = dsutil.query('type:node')
    util.assert(nodes.length > 100)
    let ways = dsutil.query('type:way')
    util.assert(nodes.length > 10)
  }),

  test('can download an area - 02', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const dataset = Api.downloadArea({
        minlat: 46.9479186,
        minlon: 7.4619484,
        maxlat: 46.9497642,
        maxlon: 7.4660683
    })
    let dsutil = new DataSetUtil(dataset)
    let nodes = dsutil.query('type:node')
    util.assert(nodes.length > 100)
    let ways = dsutil.query('type:way')
    util.assert(nodes.length > 10)
  }),

  test('can download an area - 03', () => {
    ApiConfig.serverUrl = ApiConfig.defaultServerUrl
    const Bounds = Java.type('org.openstreetmap.josm.data.Bounds')
    const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')
    const dataset = Api.downloadArea(new Bounds(
      new LatLon(46.9479186,7.4619484),   // min
      new LatLon(46.9497642, 7.4660683)   // max
    ))
    let dsutil = new DataSetUtil(dataset)
    let nodes = dsutil.query('type:node')
    util.assert(nodes.length > 100)
    let ways = dsutil.query('type:way')
    util.assert(nodes.length > 10)
  }),
))

export function run () {
    const numFails = suites
      .map((suite) => suite.run())
      .reduce((acc, i) => acc + i)
  
    if (numFails > 0) {
      throw new Exception(`There are ${numFails} failing tests`)
    } else {
      System.out.println('All tests ran successfully! ')
    }
  }
  