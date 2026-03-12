---
layout: page
title: Managing the scripting context
parent: API V3
nav_order: 5
---

# Managing the scripting context
{: .no_toc }

1. TOC
{: toc }

## What is the scripting context?

When you run a GraalJS script in JOSM, it executes inside a **scripting context** &ndash; a
sandboxed JavaScript environment that holds all global variables, imported modules, and any
state your script creates.

The scripting context persists between script executions. That means a variable or listener you
set up in one run is still alive when you run the next script.

## Resetting the context

Sometimes a script leaves behind global side effects: an
<code class="inline">AWTEventListener</code> registered on the system toolkit, a
<code class="inline">LayerChangeListener</code> attached to JOSM's layer manager, or a dialog
window that stays open. If you then run another script (or re-run the same one) those leftovers
can interfere or cause errors.

The **Reset Context** button in the scripting console and the **Reset Context** checkbox in the
Run Script dialog discard the current context and start a fresh one. All module caches are
cleared and all previously declared variables are gone.

However, Java-side listeners that were registered globally (outside of the context) are **not**
automatically removed by the reset, because they live in the JVM, not in the JavaScript context.
The [josm/context] module provides a registration API so your scripts can declare cleanup
callbacks that are called automatically before the context is closed.

## Registering cleanup callbacks

Import <code class="inline">addResetCallback</code> from [josm/context] and pass it a
zero-argument function. The function is called just before the context is destroyed, while the
context is still live &ndash; so you can safely call into Java and execute any cleanup logic you
need.

```js
import { addResetCallback } from 'josm/context'

// ... set up something ...

addResetCallback(() => {
  // ... tear it down ...
})
```

You can register as many callbacks as you like. They are called in registration order.

## Example: cleaning up an AWT event listener

The following script registers a global AWT event listener that logs every key event to the
scripting console, and uses <code class="inline">addResetCallback</code> to remove it when
the context is reset.

```js
import { addResetCallback } from 'josm/context'
import * as console from 'josm/scriptingconsole'

const Toolkit = Java.type('java.awt.Toolkit')
const AWTEvent = Java.type('java.awt.AWTEvent')

const listener = (event) => {
  console.println(`key event: ${event}`)
}
Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK)

addResetCallback(() => {
  Toolkit.getDefaultToolkit().removeAWTEventListener(listener)
  console.println('AWT key listener removed')
})
```

Without the <code class="inline">addResetCallback</code>, clicking **Reset Context** would
discard the JavaScript context, but the AWT listener would keep firing. The next time it
received a key event it would try to execute JavaScript in a dead context and throw an error.

## Example: cleaning up a layer change listener

```js
import { addResetCallback } from 'josm/context'
import * as console from 'josm/scriptingconsole'

const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')

const layerChangeListener = {
  layerAdded(e) {
    console.println(`layer added: ${e.getAddedLayer().getName()}`)
  },
  layerRemoving(e) {
    console.println(`layer removing: ${e.getRemovedLayer().getName()}`)
  },
  activeOrEditLayerChanged(e) {}
}
MainApplication.getLayerManager().addLayerChangeListener(layerChangeListener)

addResetCallback(() => {
  MainApplication.getLayerManager().removeLayerChangeListener(layerChangeListener)
})
```

## Example: disposing a dialog

If your script opens a dialog window, you are responsible for disposing it on context reset.
Use <code class="inline">addResetCallback</code> to dispose the window and remove any
listeners it registered:

```js
import { addResetCallback } from 'josm/context'

const JFrame = Java.type('javax.swing.JFrame')
const MainApplication = Java.type('org.openstreetmap.josm.gui.MainApplication')

const dialog = new JFrame('My Tool')
// ... build dialog contents ...
dialog.setVisible(true)

const layerListener = { /* ... */ }
MainApplication.getLayerManager().addLayerChangeListener(layerListener)

addResetCallback(() => {
  MainApplication.getLayerManager().removeLayerChangeListener(layerListener)
  dialog.dispose()
})
```

[josm/context]: ../../api/v3/module-josm_context.html
