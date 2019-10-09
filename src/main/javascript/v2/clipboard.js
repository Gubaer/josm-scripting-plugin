/**
 * Provides access to the system clipboard
 *
 * @module clipboard
 */

/* global Java */

/**
 * <p>Set or get the clipboard content as text</p>
 *
 * <dl>
 *   <dt><code class="signature">get</code></dt>
 *   <dd>Replies the clipboard content as text or <code>undefined</code>,
 *   if no clipboard content is available or if it can't be converted to a
 *   string.</dd>
 *
 *   <dt><code class="signature">set</code></dt>
 *   <dd>Sets the clipboard content</dd>
 * </dl>
 *
 * @example
 * const clipboard = require('clipboard')
 * // set the clipboard content
 * clipboard.text = 'Hello World!'
 *
 * @name text
 * @field
 * @instance
 * @memberof clipboard
 * @summary Set or get the clipboard content as text
 * @type {string}
 */
Object.defineProperty(exports, 'text', {
  enumerable: true,
  get: function () {
    const Toolkit = Java.type('java.awt.Toolkit')
    const DataFlavor = Java.type('java.awt.datatransfer.DataFlavor')
    const transferable = Toolkit.getDefaultToolkit()
      .getSystemClipboard().getContents(null)
    try {
      if (transferable && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        return transferable.getTransferData(DataFlavor.stringFlavor)
      }
    } catch (e) {
    }
    return undefined
  },

  set: function (value) {
    const StringSelection = Java.type('java.awt.datatransfer.StringSelection')
    const Toolkit = Java.type('java.awt.Toolkit')
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
      new StringSelection(String(value || '')),
      null
    )
  }
})
