package org.openstreetmap.josm.plugins.scripting.python;

import java.util.Collection;

import org.openstreetmap.josm.gui.MapFrame;

/**
 * @deprecated Starting with plugin version 0.2.0 Jython plugins
 * are not supported anymore.
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "0.2.0")
public interface IPythonPluginManager {

    /**
     * Updates <tt>sys.path</tt> of the current Jython environment.
     *
     * After the invocation <tt>sys.path</tt> consists of the original
     * <tt>sys.path</tt> at startup time, concatenated with the paths in
     * <code>paths</code>.
     *
     * @param paths the plugin specific paths
     */
    void updatePluginSpecificSysPaths(Collection<String> paths);

    /**
     * <p>Loads a Python plugin.</p>
     *
     * <p><code>pluginClassName</code> is a fully qualified Python class
     * name, i.e.
     * <ul>
     *   <li><tt>MyPlugin</tt></li>
     *   <li><tt>my_module.MyPlugin</tt></li>
     *   <li><tt>my_package.my_module.MyPlugin</tt></li>
     * </ul>
     * <p>Note that the plugin must be available somewhere on
     * <tt>sys.path</tt>, see
     *  {@link IPythonPluginManager#updatePluginSpecificSysPaths(Collection)}.
     * </p>
     *
     * @param pluginClassName the fully qualified plugin class name
     * @return the loaded python plugin
     */
    JosmPythonPlugin loadPlugin(String pluginClassName);

    /**
     * Invoked by the scripting plugin to notify all loaded python
     * plugins about the change of the map frame.
     *
     * @param oldFrame the old map frame
     * @param newFrame the new map frame
     */
    void notifyMapFrameChanged(MapFrame oldFrame, MapFrame newFrame);
}
