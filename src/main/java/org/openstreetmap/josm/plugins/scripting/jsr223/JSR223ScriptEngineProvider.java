package org.openstreetmap.josm.plugins.scripting.jsr223;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType;
import org.openstreetmap.josm.plugins.scripting.preferences.ScriptEngineJarInfo;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;
/**
 * Provides a list model for the list of available JSR223 compatible
 * script engines.
 */
@SuppressWarnings("unused")
public class JSR223ScriptEngineProvider
    extends AbstractListModel<ScriptEngineDescriptor>
    implements PreferenceKeys {

    /**
     * The list of default mime types, mapping file suffixes to content mime
     * types, provided as resource in the jar.
     */
    static public final String DEFAULT_MIME_TYPES =
       "/resources/mime.types.default";

    @SuppressWarnings("unused")
    static private final Logger logger =
        Logger.getLogger(JSR223ScriptEngineProvider.class.getName());

    static private JSR223ScriptEngineProvider instance;

    /**
     * Replies the unique instance
     *
     * @return the unique instance
     */
    static public @NotNull JSR223ScriptEngineProvider getInstance() {
        if (instance == null) {
            instance = new JSR223ScriptEngineProvider();
        }
        return instance;
    }

    private final List<ScriptEngineFactory> factories = new ArrayList<>();
    private final List<ScriptEngineDescriptor> descriptors = new ArrayList<>();
    private final List<File> scriptEngineJars = new ArrayList<>();
    private MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
    private ClassLoader scriptClassLoader = getClass().getClassLoader();
    private ScriptEngineManager manager = null;

    protected @NotNull ScriptEngineManager getScriptEngineManager() {
        if (manager == null){
            manager = new ScriptEngineManager(scriptClassLoader);
        }
        return manager;
    }

    protected void loadMimeTypesMap() {
        // skip loading mime types if we aren't running in the context of a
        // plugin instance
        if (ScriptingPlugin.getInstance() != null)  {
            File f = ScriptingPlugin.getInstance().getPluginDirs().getUserDataDirectory(false);
            f = new File(f,"mime.types");
            if (f.isFile() && f.canRead()){
                try {
                    mimeTypesMap = new MimetypesFileTypeMap(
                        Files.newInputStream(f.toPath()));
                    return;
                } catch(IOException e) {
                    logger.log(Level.WARNING,
                        tr("failed to load mime types from file ''{0}''.", f),
                        e
                    );
                }
            }
        }

        try (final InputStream is = getClass()
                .getResourceAsStream(DEFAULT_MIME_TYPES)){
            if (is == null){
                logger.warning(tr("failed to load default mime "
                    + "types from resource ''{0}''.", DEFAULT_MIME_TYPES));
                return;
            }
            mimeTypesMap = new MimetypesFileTypeMap(is);
        } catch(IOException e) {
            logger.log(Level.WARNING, tr("failed to load default mime "
                + "types from  resource ''{0}''.", DEFAULT_MIME_TYPES),e);
        }
    }

    protected void restoreScriptEngineUrlsFromPreferences() {
        scriptEngineJars.clear();
        if (Preferences.main() != null) {
            Preferences.main().getList(PREF_KEY_SCRIPTING_ENGINE_JARS).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ScriptEngineJarInfo::new)
                .filter(info -> info.getStatusMessage()
                    .equals(ScriptEngineJarInfo.OK_MESSAGE))
                .forEach(info ->
                    scriptEngineJars.add(new File(info.getJarFilePath())));
        }
        buildClassLoader();
    }

    protected void buildClassLoader() {
        URL[] urls = scriptEngineJars.stream()
            .map(jar -> {
                try {
                    return jar.toURI().toURL();
                } catch(MalformedURLException e) {
                    // shouldn't happen because the entries in
                    // 'scriptEngineJars' are existing, valid files.
                    // Ignore the exception, but log it.
                    logger.log(Level.WARNING,tr(
                        "found malformed URL to script engine jar. URL=''{0}''"
                        ), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toArray(URL[]::new);

        if (urls.length > 0){
            scriptClassLoader = new URLClassLoader(
                urls,
                getClass().getClassLoader()
            );
        } else {
            scriptClassLoader = getClass().getClassLoader();
        }
    }

    protected void loadScriptEngineFactories() {
        Objects.requireNonNull(scriptClassLoader,
            "expected scriptClassLoader != null");
        factories.clear();
        descriptors.clear();
        final ScriptEngineManager manager =
            new ScriptEngineManager(scriptClassLoader);
        factories.addAll(manager.getEngineFactories());

        factories.sort(Comparator.comparing(ScriptEngineFactory::getEngineName));
        factories.stream().map(ScriptEngineDescriptor::new)
            .collect(Collectors.toCollection(() -> descriptors));
    }

    private JSR223ScriptEngineProvider(){
        restoreScriptEngineUrlsFromPreferences();
        loadScriptEngineFactories();
        loadMimeTypesMap();
        fireContentsChanged(this, 0, scriptEngineJars.size());
    }

    /**
     * Replies the list of jar files from which script engines are
     * loaded.
     *
     * @return the list of jar files
     */
    public @NotNull List<File> getScriptEngineJars() {
        return new ArrayList<>(scriptEngineJars);
    }

    /**
     * Replies a script engine by name or null, if no such script
     * engine exists.
     *
     * @param name the name
     * @return the script engine
     * @throws NullPointerException - if <code>name</code> is null
     */
    public ScriptEngine getEngineByName(@NotNull String name) {
        Objects.requireNonNull(name);
        // Note: getScriptEngineManager().get(name) doesn't work as
        // expected. This is a workaround.
        return getScriptEngineFactories().stream()
            .filter(f -> f.getEngineName().equals(name))
            .findAny()
            .map(ScriptEngineFactory::getScriptEngine)
            .orElse(null);
    }

    /**
     * Replies true, if a JSR223-compatible scripting engine with name
     * <code>name</code> is currently available.
     *
     * @param name the name
     * @return true, if a JSR223-compatible scripting engine with name
     *      <code>name</code> is currently available; false, otherwise
     * @throws NullPointerException - if <code>name</code> is null
     */
    public boolean hasEngineWithName(@NotNull String name){
        Objects.requireNonNull(name);
        return getEngineByName(name) != null;
    }

    /**
     * Replies a suitable script engine for a mime type or null, if no such
     * script engine exists.
     *
     * @return the script engine
     * @see ScriptEngineManager#getEngineByMimeType(String)
     */
    public ScriptEngine getEngineByMimeType(String mimeType) {
        return getScriptEngineManager().getEngineByMimeType(mimeType);
    }

    /**
     * Derives a mime type from the file suffix and replies a script engine
     * descriptor suitable for this mime type.
     *
     * @param scriptFile the script file
     * @return the script engine descriptor. null, if no suitable script engine
     * is available
     */
    public ScriptEngineDescriptor getEngineForFile(File scriptFile) {
        if (scriptFile == null) return null;
        final String mimeType = getContentTypeForFile(scriptFile);
        return getScriptEngineManager().getEngineFactories().stream()
            .filter(factory -> factory.getMimeTypes().contains(mimeType))
            .findFirst()
            .map(ScriptEngineDescriptor::new)
            .orElse(null);
    }

    /**
     * Replies the content type for file {@code scriptFile}.
     *
     * @param scriptFile the file
     * @return the content type
     * @throws NullPointerException - if <code>scriptFile</code> is null
     */
    public String getContentTypeForFile(@NotNull File scriptFile){
        Objects.requireNonNull(scriptFile);
        return mimeTypesMap.getContentType(scriptFile);
    }

    /**
     * Sets the list of jar files which provide JSR 226 compatible script
     * engines.
     * <p>
     * null entries in the list are ignored. Entries which aren't
     * {@link ScriptEngineJarInfo#getStatusMessage() valid} are ignored.
     *
     * @param jars the list of jar files. Can be null to set an empty list of
     *  jar files.
     */
    public void setScriptEngineJars(@Null List<File> jars){
        this.scriptEngineJars.clear();
        if (jars != null){
            jars.stream()
                .filter(Objects::nonNull)
                .filter(jar -> new ScriptEngineJarInfo(jar.toString())
                      .getStatusMessage()
                      .equals(ScriptEngineJarInfo.OK_MESSAGE)
                 )
                .collect(Collectors.toCollection(() -> scriptEngineJars));
        }
        buildClassLoader();
        loadScriptEngineFactories();
        fireContentsChanged(this, 0, scriptEngineJars.size());
    }

    /**
     * Replies a script engine created by the i-th script engine factory.
     *
     * @param i the index
     * @return the engine
     */
    public ScriptEngine getScriptEngine(int i){
        return factories.get(i).getScriptEngine();
    }

    /**
     * Replies a script engine for the first script engine factory
     * whose name matches with the name in the descriptor <code>desc</code>,
     * or null, if no such scripting engine is found.
     *
     * @param desc the descriptor
     * {@link ScriptEngineType#PLUGGED}
     * @return the script engine or null
     * @throws NullPointerException - if <code>desc</code> is null
     * @throws IllegalArgumentException - if <code>desc</code> isn't a descriptor
     *  for a plugged engine
     */
    public ScriptEngine getScriptEngine(@NotNull ScriptEngineDescriptor desc) {
        Objects.requireNonNull(desc);
        Assert.assertArg(desc.getEngineType().equals(ScriptEngineType.PLUGGED),
                "Expected a descriptor for a plugged script engine, "
                + "got ''{0}''", desc);
        return factories.stream()
            .filter(factory ->
                desc.getEngineId().equals(factory.getNames().get(0)))
            .findFirst()
            .map(ScriptEngineFactory::getScriptEngine)
            .orElse(null);
    }

    /**
     * Replies the first script engine factory with name {@code name}, or
     * null,if no such factory exists. Replies null, if {@code name} is
     * null.
     * <p>
     * A script engine factory is matching with <code>name</code> if its
     * {@link ScriptEngineFactory#getEngineName() engine name} or one of its
     * {@link ScriptEngineFactory#getNames() short names} is equal to
     * <code>name</code>.
     *
     * @param name the name
     * @return the script engine factory
     */
    public ScriptEngineFactory getScriptFactoryByName(String name){
        Predicate<ScriptEngineFactory> hasName = (factory) ->
               (factory.getEngineName().equals(name))
            || factory.getNames().contains(name);

        return factories.stream()
            .filter(hasName)
            .findFirst()
            .orElse(null);
    }

    /**
     * Replies a list of the available script engine factories.
     *
     * @return the factories
     */
    public @NotNull  List<ScriptEngineFactory> getScriptEngineFactories() {
        return new ArrayList<>(factories);
    }

    /* --------------------------------------------------------------------- */
    /* ListModel                                                             */
    /* --------------------------------------------------------------------- */
    @Override
    public ScriptEngineDescriptor getElementAt(int i) {
        return descriptors.get(i);
    }

    @Override
    public int getSize() {
        return descriptors.size();
    }
}