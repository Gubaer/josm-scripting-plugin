package org.openstreetmap.josm.plugins.scripting;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.preferences.ConfigureAction;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceEditor;
import org.openstreetmap.josm.plugins.scripting.python.IPythonPluginManager;
import org.openstreetmap.josm.plugins.scripting.python.PythonPluginManagerFactory;
import org.openstreetmap.josm.plugins.scripting.ui.MostRecentlyRunScriptsModel;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptAction;
import org.openstreetmap.josm.plugins.scripting.ui.ToggleConsoleAction;
import org.openstreetmap.josm.spi.preferences.Config;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.plugins.scripting.python
        .PythonPluginManagerFactory.isJythonPresent;
import static org.openstreetmap.josm.tools.I18n.tr;

public class ScriptingPlugin extends Plugin implements PreferenceKeys{
    static private final Logger logger =
            Logger.getLogger(ScriptingPlugin.class.getName());
    static public final String START_MODULE_NAME = "ScriptingPlugin_Start";

    private static ScriptingPlugin instance;
    private static Scriptable startModule;

    private IPythonPluginManager pythonPluginManager;

    protected void initLocalInstallation() {
        final File f = new File(getPluginDirs()
                .getUserDataDirectory(false), "modules");
        if (!f.exists()) {
            if (!f.mkdirs()) {
                logger.warning(String.format("Failed to create directory '%s'",
                        f.toString()));
                return;
            }
        }
    }

    public static ScriptingPlugin getInstance() {
        return instance;
    }

    public ScriptingPlugin(PluginInformation info) {
        super(info);
        try {
            instance = this;
            installResourceFiles();
            installScriptsMenu();
            initLocalInstallation();
            final RhinoEngine engine = RhinoEngine.getInstance();
            engine.initScope();
            JOSMModuleScriptProvider provider = JOSMModuleScriptProvider
                    .getInstance();
            Optional<URL> url = provider.lookup(START_MODULE_NAME);
            if (!url.isPresent()) {
                logger.info(String.format("No startup module '%s' found.",
                        START_MODULE_NAME));
            } else {
                try {
                    startModule = engine.require(START_MODULE_NAME);
                } catch (RhinoException e) {
                    logger.log(Level.SEVERE, String.format(
                        "Failed to load start module '%s' from URL '%s'.",
                        START_MODULE_NAME, url.get()), e);
                }
                if (startModule != null) {
                    logger.info(String.format(
                        "Successfully loaded startup module '%s' from URL '%s'",
                        START_MODULE_NAME, url.get()));
                    jsOnStart();
                }
            }
            if (isJythonPresent()) {
                loadPythonPlugins();
            }
        } catch(JavaScriptException e) {
            System.out.println(
              tr("FATAL: Failed to initialize scripting plugin.\n")
            );
            System.out.println(e);
            e.printStackTrace();
        }
    }

    protected void loadPythonPlugins() {
        pythonPluginManager = PythonPluginManagerFactory
                .createPythonPluginManager();
        if (pythonPluginManager == null) return;

        pythonPluginManager.updatePluginSpecificSysPaths(
            Config.getPref().getList(PREF_KEY_JYTHON_SYS_PATHS)
        );

        Config.getPref().getList(PREF_KEY_JYTHON_PLUGINS)
            .stream()
            .filter(plugin -> ! plugin.trim().isEmpty())
            .forEach(plugin -> pythonPluginManager.loadPlugin(plugin));
    }

    protected void jsOnStart() {
        if (startModule == null)
            return;
        Object o = startModule.get("onStart", startModule);
        if (o == Scriptable.NOT_FOUND){
            return;
        }
        if (!(o instanceof Function)) {
            logger.warning(String.format(
                "module 'start': property '%s' should be a function, "
                    + "got %s instead",
                 "onStart", o));
            return;
        }
        RhinoEngine.getInstance().executeOnSwingEDT((Function) o);
    }

    protected void jsOnMapFrameChanged(MapFrame oldFrame, MapFrame newFrame) {
        if (startModule == null)
            return;
        final Object o = startModule.get("onMapFrameChanged", startModule);
        if (o == Scriptable.NOT_FOUND)
            return;
        if (!(o instanceof Function)) {
            logger.warning(String.format(
            "module 'start': property '%s' should be a function, got %s instead",
             "onMapFrameChanged", o)
             );
            return;
        }
        RhinoEngine.getInstance().executeOnSwingEDT((Function) o,
                new Object[] { oldFrame, newFrame });
    }

    private final Action toggleConsoleAction = new ToggleConsoleAction();
    private final Action runScriptAction = new RunScriptAction();
    private final Action configureAction = new ConfigureAction();

    protected void installScriptsMenu() {
        final MainMenu mainMenu = MainApplication.getMenu();
        final JMenu scriptingMenu = mainMenu.addMenu(
                "Scripting", tr("Scripting"), -1 /* no mnemonic key */ ,
                MainApplication.getMenu().getDefaultMenuPos(),
                ht("/Plugin/Scripting")
        );
        scriptingMenu.setMnemonic('S');
        MostRecentlyRunScriptsModel.getInstance()
                .loadFromPreferences(Preferences.main());
        populateStandardentries(scriptingMenu);
        populateMruMenuEntries(scriptingMenu);
        MostRecentlyRunScriptsModel.getInstance().addObserver(
            (a, b) ->  {
                scriptingMenu.removeAll();
                populateStandardentries(scriptingMenu);
                populateMruMenuEntries(scriptingMenu);
             }
       );
    }

    protected void populateStandardentries(JMenu scriptingMenu) {
        scriptingMenu.add(new JCheckBoxMenuItem(toggleConsoleAction));
        scriptingMenu.add(runScriptAction);
        scriptingMenu.add(new JSeparator());
        scriptingMenu.add(configureAction);
    }

    protected void populateMruMenuEntries(JMenu scriptingMenu) {
        final List<Action> actions = MostRecentlyRunScriptsModel
                .getInstance()
                .getRunScriptActions();
        if (!actions.isEmpty()) {
            scriptingMenu.addSeparator();
            actions.stream().limit(10).forEach(scriptingMenu::add);
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new PreferenceEditor();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        jsOnMapFrameChanged(oldFrame, newFrame);

        if (pythonPluginManager != null) {
            pythonPluginManager.notifyMapFrameChanged(oldFrame, newFrame);
        }
    }

    /**
     * Installs the default mime types shipped in the resource
     * <tt>/resources/mime.types.default</tt> in the plugin directory.
     */
    protected void installResourceFiles() {
        final File mimeTypesTarget = new File(getPluginDirs()
                .getUserDataDirectory(false), "mime.types");
        if (mimeTypesTarget.exists()) return; // don't have to install it
        final String res = "/resources/mime.types.default";
        try(InputStream is = getClass().getResourceAsStream(res)){
            if (is == null) {
                logger.warning(String.format(
                        "Didn't find resource '%s'. "
                       + "Can't install default mime types.",
                       res));
                return;
            }
            mimeTypesTarget.getParentFile().mkdirs();
            try(FileOutputStream fout = new FileOutputStream(mimeTypesTarget)) {
                byte [] buf = new byte[1024];
                int read;
                while((read = is.read(buf)) > 0) {
                    fout.write(buf, 0, read);
                }
            }
            logger.info(String.format(
                  "Successfully installed default mime types in file '%s'.",
                  mimeTypesTarget.getAbsolutePath()
            ));
        } catch(IOException e) {
            logger.warning(String.format(
                    "Failed to install default mime types "
                  + "in the plugin directory '%s'. Exception is: %s",
                  getPluginDirs().getUserDataDirectory(false).toString(),
                  e.toString()
            ));
            e.printStackTrace();
        }
    }
}
