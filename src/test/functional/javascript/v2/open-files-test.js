/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */
/* global require */
const System = Java.type('java.lang.System')
const Exception = Java.type('java.lang.Exception')

const josm = require('josm')
const util = require('josm/util')
const { suite, test, expectError } = require('josm/unittest')

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

suites.push(suite('open files',
  test('open a data and a gpx file in two layers', () => {
    const File = Java.type('java.io.File')
    const osmFile = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-josm-open.osm`
    const gpxFile = `${josmScriptingPluginHome()}/test/data/sample-data-files/test-josm-open.gpx`

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
    util.assert(josm.layers.length === 2, 'expected two layers, got {0}', josm.layers.length)

    clearLayers()
  })
))

function run () {
  return suites
    .map((a) => a.run())
    .reduce((a, b) => a + b)
}

if (typeof exports === 'undefined') {
  // not loaded as module. Run the tests immediately.
  run()
} else {
  // loaded as module. Export the run function but don't
  // execute it here.
  exports.run = run
  exports.fragileRun = function () {
    const numfail = run()
    if (numfail > 0) {
      throw new Exception(`There are ${numfail} failing tests`)
    } else {
      System.out.println('All tests ran successfully! ')
    }
  }
}