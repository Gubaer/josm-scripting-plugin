/**
 * Provides lifecycle hooks for the GraalJS scripting context.
 *
 * @module josm/context
 */

/* global __josmContextResetHooks__ */

/**
 * Registers a callback to be invoked when the GraalJS context is reset
 * (e.g. via the "Reset Context" button in the scripting console).
 *
 * Use this to clean up global resources — AWT event listeners, layer change
 * listeners, open dialogs — that would otherwise survive the reset and
 * attempt to execute code against a dead context.
 *
 * @example
 * import { addResetCallback } from 'josm/context'
 *
 * const Toolkit = Java.type('java.awt.Toolkit')
 * const AWTEvent = Java.type('java.awt.AWTEvent')
 *
 * const listener = (event) => { ... }
 * Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK)
 *
 * addResetCallback(() => {
 *   Toolkit.getDefaultToolkit().removeAWTEventListener(listener)
 * })
 *
 * @example
 * import { addResetCallback } from 'josm/context'
 *
 * const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
 *
 * const layerChangeListener = {
 *   layerAdded(e) { ... },
 *   layerRemoving(e) { ... },
 *   activeOrEditLayerChanged(e) { ... }
 * }
 * MainApplication.getLayerManager().addLayerChangeListener(layerChangeListener)
 *
 * addResetCallback(() => {
 *   MainApplication.getLayerManager().removeLayerChangeListener(layerChangeListener)
 * })
 *
 * @example
 * // Non-modal dialog: dispose it and remove any listeners it registered
 * import { addResetCallback } from 'josm/context'
 *
 * const JDialog = Java.type('javax.swing.JDialog')
 * const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')
 *
 * const dialog = new JDialog(MainApplication.getMainFrame(), 'My Tool', false)
 * // ... populate and show the dialog ...
 * dialog.setVisible(true)
 *
 * const layerListener = {
 *   layerAdded(e) { ... },
 *   layerRemoving(e) { ... },
 *   activeOrEditLayerChanged(e) { ... }
 * }
 * MainApplication.getLayerManager().addLayerChangeListener(layerListener)
 *
 * addResetCallback(() => {
 *   MainApplication.getLayerManager().removeLayerChangeListener(layerListener)
 *   dialog.dispose()
 * })
 *
 * @param {function} callback  zero-argument function invoked on context reset
 */
export function addResetCallback(callback) {
  __josmContextResetHooks__.register(callback)
}
