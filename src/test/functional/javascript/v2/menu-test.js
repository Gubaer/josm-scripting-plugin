/*
 * Functional test for manipulating menu items
 */

/* eslint no-unused-vars: ["error", { "varsIgnorePattern": "^_" }] */

/* global Java */

const util = require('josm/util')
const ut = require('josm/unittest')
const menu = require('josm/ui/menu')
const System = Java.type('java.lang.System')
const IllegalArgumentException = Java.type('java.lang.IllegalArgumentException')

function createActionTests () {
  // we can create a minimal action
  // see #78: wrapped java.lang.nullpointerexception for JSaction
  const _a1 = new menu.JSAction({
    name: 'My Action'
  })

  // we can create a more elaborate action
  const _a2 = new menu.JSAction({
    name: 'My Action',
    iconName: 'script-engine',
    onInitEnabled: function () {
      this.enabled = false
    }
  })
}

function changeActionNameTests () {
  const a1 = new menu.JSAction({
    name: 'My Action'
  })

  a1.setName('Another name')
  util.assert(a1.getName() === 'Another name', '1 - Unexpected name')
  a1.setName(null)
  util.assert(a1.getName() === 'Another name', '2 - Unexpected name')
  a1.setName(undefined)
  util.assert(a1.getName() === 'Another name', '3 - Unexpected name')
  a1.setName({ foo: 'bar' })
  util.assert(util.isSomething(a1.getName()), '4 - Unexpected name')
}

function changeActionTooltipTests () {
  const a1 = new menu.JSAction({
    name: 'My Action'
  })

  a1.setTooltip('Another tooltip')
  util.assert(a1.getTooltip().includes('Another tooltip'), '1 - Unexpected tooltip')
  a1.setTooltip({ foo: 'bar' })
  util.assert(!a1.getTooltip().includes('Another tooltip'), '4 - Unexpected tooltip')
}

function changeOnExecuteTests () {
  const a1 = new menu.JSAction({
    name: 'My Action'
  })

  function onExecute () {
    util.println('Action is executing ...')
  }

  a1.setOnExecute(onExecute)
  a1.setOnExecute(null)
  util.assert(!util.isDef(a1.onExecute), '1 - should not be defined')
  a1.setOnExecute(onExecute)
  a1.setOnExecute(undefined)
  util.assert(!util.isDef(a1.onExecute), '2 - should not be defined')
  try {
    a1.setOnExecute("println 'hello world!")
    throw new Error('expected IllegalArgumentException')
  } catch (e) {
    if (e instanceof IllegalArgumentException) {
      // OK - expected an IllegalArgumentException
    } else {
      throw e
    }
  }
  a1.setOnExecute(onExecute)
}

function changeOnInitEnabledTests () {
  const a1 = new menu.JSAction({
    name: 'My Action'
  })
  function onInitEnabled () {
    util.println("Action ''{0}'': Initializing enabled state ...", this.getName())
  }
  a1.setOnInitEnabled(onInitEnabled)
  a1.setOnInitEnabled(null)
  System.out.println(`menu-test.js: onInitEnabled - 1 - ${a1.getOnInitEnabled()}`)
  util.assert(a1.getOnInitEnabled() === null, '1 - should not be defined')

  a1.setOnInitEnabled(onInitEnabled)
  a1.setOnInitEnabled(undefined)
  util.assert(a1.getOnInitEnabled() === null, '2 - should not be defined')
  try {
    a1.setOnInitEnabled("println 'hello world!")
    throw new Error('expected IllegalArgumentException')
  } catch (e) {
    if (e instanceof IllegalArgumentException) {
      // OK - expected an IllegalArgumentException
    } else {
      throw e
    }
  }
}

function changeOnUpdateEnabledTests () {
  const a1 = new menu.JSAction({
    name: 'My Action'
  })
  function onUpdateEnabled (selection) {
    if (selection) {
      util.println("Action ''{0}'': Update enabled state - " +
        'selection has changed', this.getName())
      this.setEnabled(!selection.isEmpty())
    } else {
      util.println("Action ''{0}'': Update enabled state - " +
        'layers have changed', this.getName())
      this.setEnabled(true)
    }
  }
  a1.setOnUpdateEnabled(onUpdateEnabled)
  a1.setOnUpdateEnabled(null)
  util.assert(a1.getOnUpdateEnabled() === null, '1 - should not  be defined')
  a1.setOnUpdateEnabled(onUpdateEnabled)
  a1.setOnUpdateEnabled(undefined)
  util.assert(a1.getOnUpdateEnabled() === null, '2 - should not be defined')
  try {
    a1.setOnUpdateEnabled("println 'hello world!")
    throw new Error('expected IllegalArgumentException')
  } catch (e) {
    if (e instanceof IllegalArgumentException) {
      // OK - expected an IllegalArgumentException
    } else {
      throw e
    }
  }
}

exports.run = function () {
  createActionTests()
  changeActionNameTests()
  changeActionTooltipTests()
  changeOnExecuteTests()
  changeOnInitEnabledTests()
  changeOnUpdateEnabledTests()
}

// --------------- tests for adding the action to a menu  -----------------
// add the action to the menu
// a1.name = 'My Action'
// a1.addToMenu(josm.menu.get('edit'))

// --------------- tests for adding the action to the toolbar --------------
// action.addToToolbar({at: 'start'})
// action.addToToolbar({at: 'end'})
// action.addToToolbar({at: 5})
// action.addToToolbar({after: 'open'})
// action.addToToolbar({before: 'upload'})
// action.addToToolbar()

// continue manually:
//
// 1.   create a layer  -> should trigger onUpdateEnabled
// 2.   add objects and change the selection  -> should trigger onUpdateEnabled
// 3.   select the menu item 'Edit->My Action' -> should trigger onExecute
