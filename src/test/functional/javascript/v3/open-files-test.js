/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */

const System = Java.type('java.lang.System')
const Exception = Java.type('java.lang.Exception')

import josm from 'josm'
import * as util from 'josm/util'
import { suite, test, expectError } from 'josm/unittest'

function log (msg) {
  System.out.println(msg)
}

function josmScriptingPluginHome () {
  const homeDir = System.getenv('JOSM_SCRIPTING_PLUGIN_HOME')
  if (homeDir === null) {
    throw new Error('environment variable JOSM_SCRIPTING_PLUGIN_HOME not set')
  }
  return homeDir
}

const suites = []
const resourcesDir = `${josmScriptingPluginHome()}/src/test/resources/sample-data-files`

suites.push(suite('open files',
  test('open a data and a gpx file in two layers', () => {
    const File = Java.type('java.io.File')
    const osmFile = `${resourcesDir}/test-josm-open.osm`
    const gpxFile = `${resourcesDir}/test-josm-open.gpx`

    function clearLayers () {
      for (let i = 0; i < 2; i++) {
        josm.layers.remove(i)
      }
    }

    clearLayers()

    // open two files in two layers
    josm.open(
      osmFile,
      new File(gpxFile)
    )

    // open is asynchronous, wait 2s, then check whether the layers
    // are available
    const Thread = Java.type('java.lang.Thread')
    Thread.sleep(2000)

    util.assert(josm.layers.length === 2, 'expected two layers, got {0}', josm.layers.length)
    clearLayers()  
  })
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
