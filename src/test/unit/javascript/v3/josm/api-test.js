/* global Java */

import {test, suite, expectError, expectAssertionError} from 'josm/unittest'
import * as util from 'josm/util'
import {buildBounds, buildLatLon, ApiConfig} from 'josm/api'

const URL = Java.type('java.net.URL')

const suites = []

suites.push(suite('buildLatLon',
  test('OK case with object - should work', function() {
    const pos = buildLatLon({lat: 1.0, lon: 2.0})
    util.assert(pos.lat() === 1.0)
    util.assert(pos.lon() === 2.0)
  }),
  test('with missing parameter - should fail', function() {
    expectAssertionError(() => {
        buildLatLon()
    })
  }),
  test('with null parameter - should fail', function() {
    expectAssertionError(() => {
        buildLatLon(null)
    })
  })
))


suites.push(suite('buildBounds',
  test('OK case with object minlat, minlon, etc. - should work', function() {
    const bounds = buildBounds({
        minlat: 1.0,
        minlon: 2.0,
        maxlat: 3.0,
        maxlon: 4.0
    })
    util.assert(bounds.getMinLat() === 1.0)
    util.assert(bounds.getMinLon() === 2.0)
    util.assert(bounds.getMaxLat() === 3.0)
    util.assert(bounds.getMaxLon() === 4.0)
  }),
  test('OK case with object minlat, minlon, etc. - should work', function() {
    const bounds = buildBounds({
        min: {
            lat: 1.0,
            lon: 2.0
        },
        max: {
            lat: 3.0,
            lon: 4.0
        }
    })
    util.assert(bounds.getMinLat() === 1.0)
    util.assert(bounds.getMinLon() === 2.0)
    util.assert(bounds.getMaxLat() === 3.0)
    util.assert(bounds.getMaxLon() === 4.0)
  }),
  test('with missing parameter - should fail', function() {
    expectAssertionError(() => {
        buildBounds()
    })
  }),
  test('with null parameter - should fail', function() {
    expectAssertionError(() => {
        buildBounds(null)
    })
  })
))

const DEV_API_URL = 'http://api06.dev.openstreetmap.org'

suites.push(suite('login to dev API',
  test('can set serverUrl with an URL as string', () => {
    ApiConfig.serverUrl = DEV_API_URL
    util.assert(ApiConfig.serverUrl === DEV_API_URL)
  }),

  test('can set serverUrl with an URL as URL', () => {
    const url = new URL(DEV_API_URL)
    ApiConfig.serverUrl = url
    util.assert(ApiConfig.serverUrl === DEV_API_URL)
  }),

  test('can set a valid auth method', () => {
    ApiConfig.authMethod = 'basic'
    util.assert(ApiConfig.authMethod === 'basic')

    ApiConfig.authMethod = 'oauth'
    util.assert(ApiConfig.authMethod === 'oauth')

    expectAssertionError(() => {
      ApiConfig.authMethod = 'no-such-method'
    })
  }),

  test('can set and get basic credentials for dev server', () => {
    ApiConfig.setCredentials(
      'basic',
      {user: 'test-user', password: 'test-password'}, 
      {host: 'api06.dev.openstreetmap.org'}
    )
    const credentials = ApiConfig.getCredentials(
      'basic', 
      {host: 'api06.dev.openstreetmap.org'}
    )
    util.assert(credentials.host === 'api06.dev.openstreetmap.org',
      "unexpected host, got ''{0}''", credentials.host)
    util.assert(credentials.user === 'test-user',
      "unexpected user, got ''{0}''", credentials.user)
    util.assert(credentials.password === 'test-password',
      "unexpected password, got ''{0}''", credentials.password)
  })
))

export function run() {
    return suites
      .map(function (suite) { return suite.run() })
      .reduce(function (a, b) { return a + b })
  }

