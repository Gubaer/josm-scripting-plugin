/**
 * Provides access to the system clipboard
 *
 * @module clipboard
 */

/**
 * Set or get the clipboard content as text
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
 * var clipboard = require("clipboard");
 * // set the clipboard content
 * clipboard.text = "Hello World!";
 *
 * @property {string} text clipboard content as text
 * @name text
 * @summary Set or get the clipboard content as text
 * @static
 */
Object.defineProperty(exports, "text",     {
    enumerable: true,
    get: function() {
        var Toolkit = java.awt.Toolkit;
        var DataFlavor = java.awt.datatransfer.DataFlavor;
        var transferable = Toolkit.getDefaultToolkit()
                .getSystemClipboard().getContents(null);
        try {
            if (transferable &&  transferable.isDataFlavorSupported(
                DataFlavor.stringFlavor)) {
                return transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (e) {
        }
        return undefined;
    },

    set: function(value) {
        var StringSelection = java.awt.datatransfer.StringSelection;
        var Toolkit = java.awt.Toolkit;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(String(value || "")),
                null
        );
    }
});
