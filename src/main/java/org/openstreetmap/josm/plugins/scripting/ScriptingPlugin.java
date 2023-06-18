package org.openstreetmap.josm.plugins.scripting;

import org.mozilla.javascript.*;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginClassLoader;
import org.openstreetmap.josm.plugins.PluginHandler;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryRegistry;
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.JarJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleRepositoryBuilder;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.IllegalESModuleBaseUri;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.preferences.ConfigureAction;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceEditor;
import org.openstreetmap.josm.plugins.scripting.ui.MostRecentlyRunScriptsModel;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptAction;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptDialog;
import org.openstreetmap.josm.plugins.scripting.ui.ToggleConsoleAction;
import org.openstreetmap.josm.plugins.scripting.ui.console.SyntaxConstantsEngine;
import org.openstreetmap.josm.plugins.scripting.ui.release.ReleaseNotes;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.PrintStream;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

@SuppressWarnings("ClassInitializerMayBeStatic")
public class ScriptingPlugin extends Plugin implements PreferenceKeys{
    static private final Logger logger = Logger.getLogger(ScriptingPlugin.class.getName());

    @SuppressWarnings("WeakerAccess")
    static public final String START_MODULE_NAME = "ScriptingPlugin_Start";

    private static ScriptingPlugin instance;
    private static Scriptable startModule;

    private void initLocalInstallation() {
        final File f = new File(getPluginDirs().getUserDataDirectory(false), "modules");
        if (!f.exists()) {
            if (!f.mkdirs()) {
                logger.warning(tr("Failed to create directory ''{0}''", f));
            }
        }
    }

    public static ScriptingPlugin getInstance() {
        return instance;
    }

    private void initGraalVMJSModuleRepository(PluginInformation info) {
        final var uri =
            CommonJSModuleRepositoryRegistry.buildRepositoryUrlForBuiltinModules(info);
        if (uri != null) {
            try {
                CommonJSModuleRepositoryRegistry.getInstance().setBuiltInRepository(
                    new JarJSModuleRepository(uri)
                );
            } catch(IOException e) {
                logger.log(Level.WARNING,
                    tr("Failed to configure built-in CommonJS module repository"), e);
            }
        }
        CommonJSModuleRepositoryRegistry.getInstance()
            .loadFromPreferences(Preferences.main());
    }

    private void initGraalVMESModuleRepositories(PluginInformation info) {
        final var uri = ESModuleResolver.buildRepositoryUrlForBuiltinModules(info);
        if (uri != null) {
            var builder = new ESModuleRepositoryBuilder();
            try {
                var repo = builder.build(uri);
                ESModuleResolver.getInstance().setApiRepository(repo);
            } catch (IllegalESModuleBaseUri e) {
                logger.log(Level.WARNING, MessageFormat.format(
                    "Failed to create ES Module repository for API V3 modules. uri=''{0}''", uri), e);
            }
        }
        ESModuleResolver.getInstance().loadFromPreferences(Preferences.main());
    }

    @SuppressWarnings("unused")
    public ScriptingPlugin(PluginInformation info) {
        this(info, false /* not in test environment */);
    }

    private static Handler newSystemOut() {
        Handler h = null;
        final PrintStream err = System.err;
        System.setErr(System.out);
        try {
            h = new ConsoleHandler(); // Snapshot of System.err
        } finally {
            System.setErr(err);
        }
        return h;
    }
    
    public static final Logger applog = Logger.getGlobal();
    
    
    public ScriptingPlugin(PluginInformation info, boolean inTestEnvironment) {
        super(info);
        try {
            instance = this;
            
            Logger.getLogger("java.awt").setLevel(Level.OFF);
            Logger.getLogger("sun.awt").setLevel(Level.OFF);
            Logger.getLogger("javax.swing").setLevel(Level.OFF);
            
            Logger.getLogger("org.openstreetmap.josm.plugins.scripting.graalvm").setLevel(Level.FINE);
            Logger.getLogger("org.openstreetmap.josm.plugins.scripting.graalvm.esmodule").setLevel(Level.FINE);
            
            // Create and set handler
            Handler systemOut = new ConsoleHandler();
            systemOut.setLevel( Level.ALL );
            applog.addHandler( systemOut );
            applog.setLevel( Level.ALL );

            // Prevent logs from processed by default Console handler.
            Logger rootLog = Logger.getLogger("");
            rootLog.setLevel( Level.FINE );
            rootLog.getHandlers()[0].setLevel( Level.FINE ); // Default console handle
            
            
            installResourceFiles();
            installScriptsMenu();
            initLocalInstallation();
            if (GraalVMFacadeFactory.isGraalVMPresent()) {
                initGraalVMJSModuleRepository(info);
                initGraalVMESModuleRepositories(info);
            }
            SyntaxConstantsEngine.getInstance().loadRules(this);
            final RhinoEngine engine = RhinoEngine.getInstance();
            engine.initScope();
            JOSMModuleScriptProvider provider = JOSMModuleScriptProvider.getInstance();
            Optional<URL> url = provider.lookup(START_MODULE_NAME);
            
            
            if (url.isEmpty()) {
                logger.info(tr("No startup module ''{0}'' found.", START_MODULE_NAME));
            } else {
                try {
                    startModule = engine.require(START_MODULE_NAME);
                } catch (RhinoException e) {
                    logger.log(Level.SEVERE, tr(
                        "Failed to load start module ''{0}'' from URL ''{1}''.",
                        START_MODULE_NAME, url.get()), e);
                }
                if (startModule != null) {
                    logger.info(tr(
                        "Successfully loaded startup module ''{0}'' from URL ''{1}.''",
                        START_MODULE_NAME, url.get()));
                    jsOnStart();
                }
            }

            if (!inTestEnvironment && !ReleaseNotes.hasSeenLatestReleaseNotes()) {
                var dialog = new ReleaseNotes(MainApplication.getMainFrame());
                dialog.setVisible(true);
            }
        } catch(JavaScriptException e) {
            logger.log(Level.WARNING,
                tr("Failed to initialize scripting plugin"),e);
        }
    }

    private void jsOnStart() {
        if (startModule == null)
            return;
        Object o = startModule.get("onStart", startModule);
        if (o == Scriptable.NOT_FOUND){
            return;
        }
        if (!(o instanceof Function)) {
            logger.warning(tr(
                  "Property ''{0}'' of module ''start'' should be a function, "
                + "got ''{1}'' instead",
                 "onStart", o));
            return;
        }
        RhinoEngine.getInstance().executeOnSwingEDT((Function) o);
    }

    private void jsOnMapFrameChanged(MapFrame oldFrame, MapFrame newFrame) {
        if (startModule == null)
            return;
        final Object o = startModule.get("onMapFrameChanged", startModule);
        if (o == Scriptable.NOT_FOUND)
            return;
        if (!(o instanceof Function)) {
            logger.warning(tr(
                "Property ''{0}'' of module ''start'' should be a function, " +
                "got ''{1}'' instead", "onMapFrameChanged", o)
             );
            return;
        }
        RhinoEngine.getInstance().executeOnSwingEDT((Function) o,
            new Object[] { oldFrame, newFrame });
    }

    private final Action toggleConsoleAction = new ToggleConsoleAction();

    // object initialization required, don't change to static { ... }
    {
        // make sure  RunScriptDialog singleton is initialized
        // *before* the RunScriptAction singleton below. Ensures that
        // toolbar buttons for scripts are added to the toolbar as
        // expected.
        //noinspection ResultOfMethodCallIgnored
        RunScriptDialog.getInstance();
    }

    private final Action runScriptAction = new RunScriptAction();
    private final Action configureAction = new ConfigureAction();

    private void installScriptsMenu() {
        final MainMenu mainMenu = MainApplication.getMenu();
        final JMenu scriptingMenu = mainMenu.addMenu(
            "Scripting", tr("Scripting"),
            -1 /* no mnemonic key */ ,
            MainApplication.getMenu().getDefaultMenuPos(),
            ht("/Plugin/Scripting")
        );
        scriptingMenu.setMnemonic('S');
        MostRecentlyRunScriptsModel.getInstance().loadFromPreferences(Preferences.main());
        populateStandardEntries(scriptingMenu);
        populateMruMenuEntries(scriptingMenu);
        MostRecentlyRunScriptsModel.getInstance().getPropertyChangeSupport()
            .addPropertyChangeListener((e) ->  {
                if (e.getPropertyName().equals(MostRecentlyRunScriptsModel.PROP_SCRIPTS)) {
                    scriptingMenu.removeAll();
                    populateStandardEntries(scriptingMenu);
                    populateMruMenuEntries(scriptingMenu);
                }
             }
       );
    }

    private void populateStandardEntries(JMenu scriptingMenu) {
        scriptingMenu.add(new JCheckBoxMenuItem(toggleConsoleAction));
        scriptingMenu.add(runScriptAction);
        scriptingMenu.add(new JSeparator());
        scriptingMenu.add(configureAction);
    }

    private void populateMruMenuEntries(JMenu scriptingMenu) {
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
    }

    /**
     * Installs the default mime types shipped in the resource
     * <tt>/resources/mime.types.default</tt> in the plugin directory.
     */
    private void installResourceFiles() {
        final File mimeTypesTarget = new File(getPluginDirs()
            .getUserDataDirectory(false), "mime.types");
        if (mimeTypesTarget.exists()) return; // don't have to install it
        final String res = "/resources/mime.types.default";
        try(InputStream is = getClass().getResourceAsStream(res)){
            if (is == null) {
                logger.warning(tr(
                     "Didn''t find resource ''{0}''. "
                   + "Can''t install default mime types.",
                   res));
                return;
            }
            if (mimeTypesTarget.getParentFile().mkdirs()) {
                logger.warning(tr(
                    "Failed to create directory ''{0}''. Can''t install " +
                    "mime.types file",
                    mimeTypesTarget.getAbsolutePath()
                ));
            }
            try(FileOutputStream fout = new FileOutputStream(mimeTypesTarget)) {
                byte [] buf = new byte[1024];
                int read;
                while((read = is.read(buf)) > 0) {
                    fout.write(buf, 0, read);
                }
            }
            logger.info(tr(
                  "Successfully installed default mime types in file ''{0}''.",
                  mimeTypesTarget.getAbsolutePath()
            ));
        } catch(IOException e) {
            logger.warning(tr(
                    "Failed to install default mime types "
                  + "in the plugin directory ''{0}''.",
                  getPluginDirs().getUserDataDirectory(false).toString(),
                  e
            ));
        }
    }

    static public class PluginNotFoundException extends Exception {
        PluginNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Loads a class from a 3d-party plugin present in JOSM
     *
     * @param pluginName the short plugin name, i.e. <pre>contourmerge</pre>
     * @param className the fully qualified class name
     * @return the loaded class
     * @throws PluginNotFoundException thrown, if the plugin isn't available,
     *      i.e. because it isn't configured or loaded in JOSM
     * @throws ClassNotFoundException thrown, if the class could not be
     *      loaded using the class loader of the 3d-party plugin
     */
    @SuppressWarnings("unused") // public API, used from JavaScript
    static public NativeJavaClass loadClassFrom3dPartyPlugin(
            @NotNull final String pluginName,
            @NotNull final String className)
        throws PluginNotFoundException, ClassNotFoundException{
        final PluginClassLoader cl =
            PluginHandler.getPluginClassLoader(pluginName);
        if (cl == null) {
            throw new PluginNotFoundException(
                tr("plugin class loader for plugin ''{0}'' not found", pluginName));
        }
        return new NativeJavaClass(RhinoEngine.getInstance().getScope(),
            cl.loadClass(className));
    }
}
