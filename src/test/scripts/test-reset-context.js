/**
 * Test script for v0.4.1 features:
 *  - non-modal JDialog with a "Hello" button
 *  - LayerChangeListener that logs events to the scripting console
 *  - context reset callback that disposes the dialog and removes the listener
 *
 * Run via: Scripting → Run Script… → select this file (GraalJS engine)
 * Test:    open/close layers, then click "Reset Context" — dialog must close
 *          and no further layer events must appear in the console.
 */

import josm from 'josm'
import { addResetCallback } from 'josm/context'
import { print } from 'josm/scriptingconsole'

// ── Java types ────────────────────────────────────────────────────────────────

const MainApplication     = Java.type('org.openstreetmap.josm.gui.MainApplication')
const LayerChangeListener = Java.extend(
    Java.type('org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener')
)
const JDialog        = Java.type('javax.swing.JDialog')
const JPanel         = Java.type('javax.swing.JPanel')
const JButton        = Java.type('javax.swing.JButton')
const BorderFactory  = Java.type('javax.swing.BorderFactory')
const FlowLayout     = Java.type('java.awt.FlowLayout')
const Dimension      = Java.type('java.awt.Dimension')

// ── State ─────────────────────────────────────────────────────────────────────

let dialog        = null
let layerListener = null

// ── Layer change listener ─────────────────────────────────────────────────────

layerListener = new LayerChangeListener({
    layerAdded: (e) => {
        print('[test-reset-context] layer added: ' + e.getAddedLayer().getName())
    },
    layerRemoving: (e) => {
        print('[test-reset-context] layer removing: ' + e.getRemovedLayer().getName())
    },
    layerOrderChanged: (_e) => {
        print('[test-reset-context] layer order changed')
    }
})
MainApplication.getLayerManager().addLayerChangeListener(layerListener)
print('[test-reset-context] LayerChangeListener registered')

// ── Dialog ────────────────────────────────────────────────────────────────────

const panel = new JPanel(new FlowLayout())
panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16))

const btnHello = new JButton('Hello')
btnHello.setPreferredSize(new Dimension(120, 32))
btnHello.addActionListener(() => {
    josm.alert('Hello')
})
panel.add(btnHello)

dialog = new JDialog(MainApplication.getMainFrame(), 'Test v0.4.1 – Reset Context', false)
dialog.setContentPane(panel)
dialog.pack()
dialog.setLocationRelativeTo(MainApplication.getMainFrame())
dialog.setVisible(true)
print('[test-reset-context] dialog opened')

// ── Context reset callback (v0.4.1) ───────────────────────────────────────────

addResetCallback(() => {
    if (layerListener !== null) {
        MainApplication.getLayerManager().removeLayerChangeListener(layerListener)
        layerListener = null
        print('[test-reset-context] LayerChangeListener removed')
    }
    if (dialog !== null) {
        dialog.dispose()
        dialog = null
        print('[test-reset-context] dialog disposed')
    }
})
