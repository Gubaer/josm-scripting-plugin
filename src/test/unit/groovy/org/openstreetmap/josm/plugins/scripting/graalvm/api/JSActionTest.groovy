package org.openstreetmap.josm.plugins.scripting.graalvm.api


import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest

class JSActionTest extends AbstractGraalVMBasedTest {

    @Test
    void "can create a JSAction"() {
        final code = """
              const {JSAction} = require('josm/ui/menu')
              const util = require('josm/util')
              const josm = require('josm')
              const JMenuItem = Java.type('javax.swing.JMenuItem')
              
              const action = new JSAction({
                 name: "My Action",
                 iconName: 'myicon',
                 toolbarId: 'myToolbarId',
                 tooltip: "This is my action",
                 onInitEnabled: function() {
                    util.println('onInitEnabled: entering ...')
                 },
                 onUpdateEnabled: function() {
                    util.println('onUpdateEnabled: entering ...')
                 },
                 onExecute: function() {
                    util.println('Action is executing ...')
                 }
              })
              const fileMenu = josm.menu.get('file')
              fileMenu.addSeparator()
              fileMenu.add(new JMenuItem(action))
            """
        facade.eval(graalJSDescriptor, code)
    }
}
