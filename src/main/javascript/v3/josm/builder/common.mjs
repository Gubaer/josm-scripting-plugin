
import * as util from 'josm/util'

const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')

export function assertGlobalId (id) {
  util.assertSomething(id, 'Expected a defined, non-null object id, got {0}',
    id)
  util.assertNumber(id, 'Expected a number as object id, got {0}', id)
  util.assert(id > 0, 'Expected a positive id, got {0}', id)
}

export function rememberId (builder, id, version) {
  assertGlobalId(id)
  builder.id = id
  version = util.isDef(version) ? version : 1
  util.assertNumber(version, 'Expected a number for \'version\', got {0}',
    version)
  util.assert(version > 0,
    'Expected a positive number for \'version\', got {0}', version)
  builder.version = version
}

export function rememberTags (builder, tags) {
  if (util.isNothing(tags)) return
  util.assert(typeof tags === 'object',
    'Expected a hash with tags, got {0}', tags)
  builder.tags = builder.tags || {}
  for (let name in tags) {
    if (!util.hasProp(tags, name)) break
    let value = tags[name]
    name = util.trim(name)
    if (util.isNothing(value)) break
    value = value + '' // convert to string
    builder.tags[name] = value
  }
}

export function assignTags (primitive, tags) {
  for (const name in tags) {
    if (!util.hasProp(tags, name)) continue
    let value = tags[name]
    if (util.isNothing(value)) continue
    value = value + ''
    primitive.put(name, value)
  }
}

export function rememberIdFromObject (builder, args) {
  if (!util.hasProp(args, 'id')) return
  const o = args.id
  util.assert(util.isSomething(o),
    "''{0}'': must not be null or undefined", 'id')
  util.assert(util.isNumber(o),
    "''{0}'': expected a number, got {1}", 'id', o)
  util.assert(o > 0,
    "''{0}'': expected a number > 0, got {1}", 'id', o)
  builder.id = o
}

export function rememberVersionFromObject (builder, args) {
  if (!util.hasProp(args, 'version')) return
  const o = args.version
  util.assert(util.isSomething(o),
    "''{0}'': must not be null or undefined", 'version')
  util.assert(util.isNumber(o),
    "''{0}'': expected a number, got {1}", 'version', o)
  util.assert(o > 0,
    "''{0}'': expected a number > 0, got {1}", 'version', o)
  builder.version = o
}

export function checkLat (value) {
  if (!(util.isSomething(value) && util.isNumber(value) && LatLon.isValidLat(value))) {
    throw new Error(`invalid lat value, got '${value}`)
  }
  return value
}

export function checkLon (value) {
  if (!(util.isSomething(value) && util.isNumber(value) && LatLon.isValidLon(value))) {
    throw new Error(`invalid lon value, got '${value}`)
  }
  return value
}

export function rememberPosFromObject (builder, args) {
  if (util.hasProp(args, 'pos')) {
    util.assert(!(util.hasProp(args, 'lat') || util.hasProp(args, 'lon')),
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
      util.assert(util.hasProp(pos, 'lat'),
        "''{0}'': missing mandatory property ''lat''", 'pos')
      util.assert(util.hasProp(pos, 'lon'),
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
  if (util.hasProp(args, 'lat')) {
    const o = args.lat
    util.assert(util.isSomething(o),
      "''{0}'': must not be null or undefined", 'lat')
    util.assert(util.isNumber(o),
      "''{0}'': expected a number, got {1}", 'lat', o)
    util.assert(LatLon.isValidLat(o),
      "''{0}'': expected a valid latitude, got {1}", 'lat', o)
    builder.lat = o
  }
  if (util.hasProp(args, 'lon')) {
    const o = args.lon
    util.assert(util.isSomething(o),
      "''{0}'': must not be null or undefined", 'lon')
    util.assert(util.isNumber(o),
      "''{0}'': expected a number, got {1}", 'lon', o)
    util.assert(LatLon.isValidLon(o),
      "''{0}'': expected a valid longitude, got {1}", 'lon', o)
    builder.lon = o
  }
}

export function rememberTagsFromObject (builder, args) {
  if (!util.hasProp(args, 'tags')) return
  const o = args.tags
  if (util.isNothing(o)) return
  rememberTags(builder, o)
}
