package org.openstreetmap.josm.plugins.scripting.graalvm.api

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.graalvm.AbstractGraalVMBasedTest
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory
import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMFacade

class JSActionTest extends AbstractGraalVMBasedTest {

    private IGraalVMFacade facade

    @BeforeEach
    void initGraalVMFacade() {
        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
    }

    @AfterEach
    void resetGraalVMFacade() {
        if (facade != null) {
            facade.resetContext()
        }
    }

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
