package org.openstreetmap.josm.plugins.scripting.python;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * <p>An instance of this class manages the configured Python
 * plugins for JOSM.</p>
 * 
 * <p>A Python plugin implements the Java interface {@link JosmPythonPlugin}.</p>
 * 
 * <p>The manager loads configured Python plugins when the scripting
 * plugin starts up, provided the Jython interpreter is on the class
 * path.</p>
 * 
 * <p>It loads plugins from the paths given by Jytons <tt>sys.path</tt>. You
 * can configured additional paths with the JOSM preference key 
 * {@link PreferenceKeys#PREF_KEY_JYTHON_SYS_PATHS} or using
 * {@link #addSysPath(String)}.</p>
 * 
 */
public class PythonPluginManager implements     
    PreferenceKeys, IPythonPluginManager{
    static private final Logger logger = Logger.getLogger(
            PythonPluginManager.class.getName());
  
    @Override
    public void updatePluginSpecificSysPaths(Collection<String> paths) {
        PySystemState sys = Py.getSystemState();
        for (String path: originalSysPaths) {
        	sys.path.append(new PyString(path));
        }        
        if (paths != null) {
        	for (String path: paths) {
            	sys.path.append(new PyString(path));
            } 
        }
        sys.setClassLoader(PythonPluginManager.class.getClassLoader());        
    }
    
    private PythonInterpreter interpreter;
    private PythonInterpreter getInterpreter() {
        if (interpreter == null) {
            interpreter = new PythonInterpreter();
        }
        return interpreter;
    }
    
    /** the map of loaded plugins */
    static final private Map<String, JosmPythonPlugin> plugins =
            new HashMap<String, JosmPythonPlugin>();

    private final List<String> originalSysPaths = new ArrayList<String>(); 
    public PythonPluginManager() {
        PySystemState state = Py.getSystemState();
        for (int i =0; i< state.path.__len__(); i++) {
            PyString path = (PyString)state.path.__getitem__(i);
            originalSysPaths.add(path.toString());
        }                
    }
    
    /**
     * Replies the list of paths in <tt>sys.path</tt> at startup time of 
     * the plugin, i.e. before any plugin specific paths have been added.
     * 
     * @return the list of paths 
     */
    public List<String> getOriginalSysPaths() {
        return Collections.unmodifiableList(originalSysPaths);
    }
    
    @Override
    public JosmPythonPlugin loadPlugin(String pluginClassName) {
        JosmPythonPlugin plugin = plugins.get(pluginClassName);
        if (plugin != null) return plugin;
        
        PythonInterpreter interpreter = getInterpreter();
        int idx = pluginClassName.lastIndexOf(".");
        String className = pluginClassName;
        if (idx >= 0) {
            String module = pluginClassName.substring(0, idx);
            className = pluginClassName.substring(idx + 1);
            String importStatement = String.format(
                    "from %s import %s", module, className);
            try {
                interpreter.exec(importStatement);
            } catch(Exception e) {
                logger.warning(tr("Failed to load python module ''{0}''. \n"
                   + "Make sure the preferences with key ''{1}'' include "
                   + "the path to the module.",
                   module,
                   PreferenceKeys.PREF_KEY_JYTHON_SYS_PATHS
                ));
                e.printStackTrace();
                return null;
            }
        } 
       
        
        try {
            PyObject pluginClass = interpreter.get(className);
            if (pluginClass == null) {
               logger.warning(tr("Failed to lookup plugin class ''{0}''.", className));
               return null;
            }
            PyObject pluginInstance = pluginClass.__call__();
            logger.info("instantiated plugin: " + pluginInstance);
            
            plugin = (JosmPythonPlugin)
                    pluginInstance.__tojava__(JosmPythonPlugin.class);
            plugins.put(pluginClassName, plugin);
            
        } catch(Exception e) {
            logger.warning(tr("Failed to instantiate plugin ''{0}''.", pluginClassName));
            e.printStackTrace();
            return null;
        }
        
        try {
            plugin.onLoad();
        } catch(Exception e) {
            logger.warning(tr("''onLoad()'' for plugin ''{0}'' failed. "
                    + "Plugin isn''t properly initialized.",
                    pluginClassName
            ));
            System.out.println(e);
            e.printStackTrace();
        }    
        return plugin;
    }   
    
    @Override
    public void notifyMapFrameChanged(MapFrame oldFrame, MapFrame newFrame){
        for(JosmPythonPlugin plugin: plugins.values()) {
            plugin.onMapFrameChanged(oldFrame, newFrame);
        }
    }
}
