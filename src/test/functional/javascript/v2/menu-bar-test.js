/*
 * Functional test for accessing the menu bar
 */

const josm = require('josm')
const util = require('josm/util')
const ut = require('josm/unittest')

exports.run = function () {
  util.println('Number of menues in the menu bar: {0}', josm.menu.length)

  const editMenu = josm.menu.get(1)
  util.println('Got the edit menu: {0}', editMenu)

  util.each(josm.menu.menuNames, function (name) {
    const menu = josm.menu.get(name)
    util.println("Menu ''{0}'' -> got menu object {1}", name, menu)
  })

  ut.expectAssertionError(function () {
    // negative index -> fail
    josm.menu.get(-1)
  })

  ut.expectAssertionError(function () {
    // index out of range -> fail
    josm.menu.get(josm.menu.length)
  })

  ut.expectAssertionError(function () {
    // unknown menu name -> fail
    josm.menu.get('no such menu')
  })

  ut.expectAssertionError(function () {
    // index is neither a string nor a number -> fail
    josm.menu.get({})
  })
}
