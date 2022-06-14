
/* global Java */
const Exception = Java.type('java.lang.Exception')
const System = Java.type('java.lang.System')

import * as node_builder_tests from 'josm/node-builder-test'
import * as way_builder_tests from 'josm/way-builder-test'
import * as relation_builder_test from 'josm/relation-builder-test'
import * as util_test from 'josm/util-test'
import * as command_test from 'josm/command-test'

const tests = [
  node_builder_tests,
  way_builder_tests,
  relation_builder_test,
  util_test,
  command_test
  // 'josm/util-test',
  // 'josm/node-builder-test.js',
  // 'josm/way-builder-test.js',
  // 'josm/relation-builder-test.js',
  // 'josm/command-test.js',
  // 'josm/ds-test.js'
]

export function run () {
  return tests
    .map(function (tests) { return tests.run() })
    .reduce(function (acc, numTests) { return acc + numTests })
}

export function fragileRun() {
  const numfail = run()
  if (numfail > 0) {
    throw new Exception('There are ' + numfail + ' failing tests')
  } else {
    System.out.println('All tests ran successfully! ')
  }
}
