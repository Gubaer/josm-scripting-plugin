import { suite, test, expectError } from 'josm/unittest'
import * as util from 'josm/util'
import { OsmPrimitiveType, DataSet, DataSetUtil, buildId } from 'josm/ds'
import {Api, ApiConfig} from 'josm/api'

const System = Java.type('java.lang.System')
const Exception = Java.type('java.lang.Exception')

const suites = []

const DEV_API_URL = "https://master.apis.dev.openstreetmap.org/api"
const DEV_API_HOST = "master.apis.dev.openstreetmap.org"
const ENV_OSM_DEV_API_PASSWORD = 'OSM_DEV_API_PASSWORD'

function getOsmDevApiPassword () {
  const password = System.getenv(ENV_OSM_DEV_API_PASSWORD)
  // password is the string literal 'null', not the value null, if 
  // the environment variable is not set
  if ( password == 'null') {
    throw new Error(`environment variable '${ENV_OSM_DEV_API_PASSWORD}' not set`)
  }
  return password
}

suites.push(suite('upload',
  test('can upload ', () => {
    ApiConfig.serverUrl = DEV_API_URL
    ApiConfig.authMethod = 'basic'
    ApiConfig.setCredentials(
      'basic',
      {
        user: 'guggis',
        password: getOsmDevApiPassword()
      },
      {host: DEV_API_HOST}
    )

    const dsUtil = new DataSetUtil(new DataSet())
    const nodes = [
      dsUtil.nodeBuilder.create(),
      dsUtil.nodeBuilder.create()
    ]
    const ways = [
      dsUtil.wayBuilder.withNodes(...nodes).create()
    ]
    let processedPrimitives = Api.upload(dsUtil.ds)
    processedPrimitives.forEach(node => {
      util.assert(node.getUniqueId() > 0)
      util.assert(node.getVersion() === 1)
    })
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
