package org.openstreetmap.josm.plugins.scripting.python;

import org.openstreetmap.josm.gui.MapFrame;

/**
 * The interface a Jython plugin has to implement.
 *
 * Here's the skeleton of a Python plugin:
 * <pre>
 * from org.openstreetmap.josm.plugins.scripting.python import JosmPythonPlugin
 *
 * class MyPlugin(JosmPythonPlugin):
 *    def onLoad(self):
 *       print "onLoad: starting ..."
 *
 *    def onMapFrameChanged(self, oldFrame, newFrame):
 *         print "onMapFrameChanged: entering ..."
 * </pre>
 *
 * @deprecated Starting with plugin version 0.2.0 Jython plugins
 * are not supported anymore.
 */
@Deprecated(forRemoval = true, since = "0.2.0")
public interface JosmPythonPlugin {

    /**
     * Invoked by the plugin manager after the plugin has been
     * loaded successfully.
     */
    void onLoad();

    /**
     * Invoked by the plugin manager when JOSMs {@link MapFrame}
     * changes.
     *
     *
     *
     * @param oldFrame the old value of the current map frame
     * @param newFrame the new value of the current map frame
     */
    void onMapFrameChanged(MapFrame oldFrame, MapFrame newFrame);
}
