/**
 * Collection of builders for creating OSM nodes, ways and relations.
 *
 * @module josm/builder
 * @example
 * // josm/builder exports the NodeBuilder, WayBuilder, and RelationBuilder
 * // from its sub modules
 * import {
 *   NodeBuilder, 
 *   WayBuilder,
 *   RelationBuilder
 * } from 'josm/builder'
 * 
 * @see module:josm/builder/node~NodeBuilder
 * @see module:josm/builder/way~WayBuilder
 * @see module:josm/builder/relation~RelationBuilder
 */

export const DataSet = Java.type('org.openstreetmap.josm.data.osm.DataSet')

export * from './builder/node-builder'
export * from './builder/way-builder'
export * from './builder/relation-builder'