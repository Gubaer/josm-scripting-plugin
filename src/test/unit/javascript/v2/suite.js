/*
 * Suite of unit tests for the API V2.
 *
 * How to run
 * ==========
 * - Add the path
 *       $JOSM_SCRIPTING_PLUGIN_ROOT/test/script-api
 *   to the list of module repositories
 *        o  launch JOSM
 *        o  Scripting -> Configure ...
 *        o  Select Tab 'Embedded Rhino Engine'
 *        o  Add the module repository for unit tests
 *
 * - Launch JOSM, open the scripting console and enter
 *      require('suite').run()
 *
 * - Run the unit test
 */

/* global Java */
const Exception = Java.type('java.lang.Exception')
const System = Java.type('java.lang.System')

const tests = [
  'josm/util-test',
  'josm/node-builder-test.js',
  'josm/way-builder-test.js',
  'josm/relation-builder-test.js',
  'josm/command-test.js',
  'josm/ds-test.js'
]

function run () {
  return tests
    .map(function (a) { return require(a).run() })
    .reduce(function (a, b) { return a + b })
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
      throw new Exception('There are ' + numfail + ' failing tests')
    } else {
      System.out.println('All tests ran successfully! ')
    }
  }
}
