/**
 * Provides access to the system clipboard
 *
 * @module clipboard
 *
 * @example
 *   import clipboard from 'clipboard'
 *
 */

/* global Java */

export default class Clipboard {
  /**
   * Set or get the clipboard content as text
   *
   * <dl>
   *   <dt><code class="signature">get</code></dt>
   *   <dd class="param-desc">Replies the clipboard content as text or <code>undefined</code>,
   *   if no clipboard content is available or if it can't be converted to a
   *   string.</dd>
   *
   *   <dt><code class="signature">set</code></dt>
   *   <dd class="param-desc">Sets the clipboard content</dd>
   * </dl>
   *
   * @example
   * import clipboard from 'clipboard'
   * // set the clipboard content
   * clipboard.text = 'Hello World!'
   *
   * @property {string} text clipboard content as text
   */
  static get text() {
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
  }

  static set text(value) {
    const StringSelection = Java.type('java.awt.datatransfer.StringSelection')
    const Toolkit = Java.type('java.awt.Toolkit')
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
      new StringSelection(String(value || '')),
      null
    )
  }
}
