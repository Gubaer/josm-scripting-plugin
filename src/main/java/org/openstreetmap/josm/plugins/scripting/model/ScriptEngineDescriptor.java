package org.openstreetmap.josm.plugins.scripting.model;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;

import javax.script.ScriptEngineFactory;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Describes a scripting engine used in the scripting plugin.
 */
public class ScriptEngineDescriptor implements PreferenceKeys {
    static private final Logger logger = Logger.getLogger(ScriptEngineDescriptor.class.getName());

    public enum ScriptEngineType {
        /**
         * a scripting engine supplied as JSR233 compliant scripting engine
         */
        PLUGGED("plugged"),

        /**
         * a scripting engine/language supported by the GraalVM
         */
        GRAALVM("graalvm")
        ;

        public final String preferencesValue;
        ScriptEngineType(String preferencesValue) {
            this.preferencesValue = preferencesValue;
        }

        /**
         * Infers the script engine type from preference value. The value is
         * a string <code>type/engineId</code>. This method decodes the
         * component <code>type</code>. Replies <code>null</code> if no type
         * can be inferred.
         * <p>
         * <strong>Examples</strong>
         * <pre>
         *    type = ScriptEngineType.fromPreferencesValue("embedded/rhino");
         *    type = ScriptEngineType.fromPreferencesValue("plugged/groovy");
         *    type = ScriptEngineType.fromPreferencesValue("graalvm/js");
         * </pre>
         *
         * @param preferencesValue the preferences value
         * @return the type
         */
        static public ScriptEngineType fromPreferencesValue(String preferencesValue) {
            if (preferencesValue == null) return null;
            preferencesValue = preferencesValue.trim().toLowerCase();
            final int i = preferencesValue.indexOf("/");
            final String pv = i < 0
                ? preferencesValue
                : preferencesValue.substring(0, i);
            return Arrays.stream(values())
                .filter(e -> e.preferencesValue.equals(pv))
                .findFirst()
                .orElse(null);
        }
    }

    private final ScriptEngineType engineType;
    private final String engineId;
    private String languageName = null;
    private String languageVersion = null;
    private String engineName = null;
    private String engineVersion = null;
    private final HashSet<String> contentMimeTypes = new HashSet<>();

    /**
     * Replies a script engine descriptor derived from a preference value
     * <code>engineType/engineId</code> in {@link Preferences#main()}.
     *
     * @return the scripting engine descriptor
     * @see #buildFromPreferences(Preferences)
     */
    @SuppressWarnings("unused")
    static public ScriptEngineDescriptor buildFromPreferences(){
        return buildFromPreferences(Preferences.main());
    }

    /**
     * Replies a script engine descriptor derived from a preference value
     * <code>engineType/engineId</code>.
     * <p>
     * Looks for  the preference value with key
     * {@link PreferenceKeys#PREF_KEY_SCRIPTING_ENGINE}.
     * If this key doesn't exist or if it doesn't refer to a supported
     * combination of <code>engineType</code> and <code>engineId</code>,
     * null isreplied.
     *
     * @param preferences the preferences
     * @return the scripting engine descriptor
     */
    static public ScriptEngineDescriptor buildFromPreferences(final Preferences preferences) {
        if (preferences == null) return null;
        String prefValue = preferences.get(PREF_KEY_SCRIPTING_ENGINE);
        return buildFromPreferences(prefValue);
    }

    /**
     * Replies a script engine descriptor derived from a preference value
     * <code>engineType/engineId</code>.
     *
     * @param preferenceValue the preference value. If null, replies null
     *
     * @return the scripting engine descriptor. null, if <code>preferenceValue</code> doesn't
     *  refer to a supported scripting engine
     */
    static public ScriptEngineDescriptor buildFromPreferences(final String preferenceValue){
        if (preferenceValue == null) return null;

        final var type = ScriptEngineType.fromPreferencesValue(preferenceValue);
        if (type == null) {
            //NOTE: might be a legal preferences value for former plugin
            // versions. No attempt to recover from these values, when this
            // code goes productive, former preference values are automatically
            // reset to the current default scripting engine.
            logger.warning(tr("Preference value ''{0}'' "
                + "consist of an unsupported engine ID. Expected pattern "
                + "''type/id''.",preferenceValue));
            return null;
        }

        final int i = preferenceValue.indexOf("/");
        if (i < 0) return null;
        var engineId = preferenceValue.substring(i+1);
        switch(type){
            case PLUGGED:
                // don't lowercase. Lookup in ScriptEngineManager could be
                // case-sensitive
                engineId = engineId.trim();
                logger.log(Level.FINE, MessageFormat.format("buildFromPreferences: engineId={0}", engineId));
                if (!JSR223ScriptEngineProvider.getInstance().hasEngineWithName(engineId)) {
                    logger.warning(tr("Preference value ''{0}'' refers to an unsupported JSR223 compatible "
                        + "scripting engine with id ''{1}''",
                        preferenceValue, engineId));
                    return null;
                }
                return new ScriptEngineDescriptor(ScriptEngineType.PLUGGED, engineId);

            case GRAALVM:
                if (!GraalVMFacadeFactory.isGraalVMPresent()) {
                    logger.warning(tr("Preferences value ''{0}'' refers to an "
                        + "GraalVM engine, but currently the GraalVM isn''t present "
                        + "on the classpath.",
                        preferenceValue
                    ));
                    return null;
                }
                final String id = engineId;
                final var engine = GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                    .getScriptEngineDescriptors().stream().filter(d ->
                        d.getLocalEngineId().equalsIgnoreCase(id)
                    )
                    .findFirst();
                if (engine.isEmpty()) {
                    logger.warning(tr("Preference value ''{0}'' refers to an GraalVM engine "
                        + "with id''{1}''. The GraalVM for this language is currently not "
                        + "present.",
                        preferenceValue,
                        id
                    ));
                    return null;
                } else {
                    return engine.get();
                }

            default:
                // shouldn't happen
                throw new IllegalStateException(String.format(
                    "unexpected scripting engine type '%s'", type
                ));
        }
    }

    protected void initParametersForJSR223Engine(String engineId) {
        ScriptEngineFactory factory = JSR223ScriptEngineProvider.getInstance()
                .getScriptFactoryByName(engineId);
        initParametersForJSR223Engine(factory);
    }

    protected void initParametersForJSR223Engine(ScriptEngineFactory factory) {
        if (factory == null){
            this.languageName = null;
            this.languageVersion = null;
            this.engineName = null;
            this.contentMimeTypes.clear();
            this.engineVersion = null;
        } else {
            this.languageName = factory.getLanguageName();
            this.languageVersion = factory.getLanguageVersion();
            this.engineName = factory.getEngineName();
            this.contentMimeTypes.clear();
            this.contentMimeTypes.addAll(factory.getMimeTypes());
            this.engineVersion = factory.getEngineVersion();
        }
    }

    /**
     * Creates a new descriptor for the type {@link ScriptEngineType#PLUGGED}.
     *
     * @param engineId the engine id. Must not be null.
     */
    public ScriptEngineDescriptor(@NotNull String engineId){
        Objects.requireNonNull(engineId);
        this.engineType = ScriptEngineType.PLUGGED;
        this.engineId = engineId.trim();
        initParametersForJSR223Engine(this.engineId);
    }

    /**
     * Creates a new descriptor.
     *
     * @param engineType the engine type. Must not be null.
     * @param engineId the engine id. Must not be null.
     */
    public ScriptEngineDescriptor(@NotNull ScriptEngineType engineType,
            @NotNull String engineId) {
        Objects.requireNonNull(engineType);
        Objects.requireNonNull(engineId);
        this.engineId = engineId;
        this.engineType= engineType;
        if (this.engineType.equals(ScriptEngineType.PLUGGED)) {
            initParametersForJSR223Engine(this.engineId);
        }
    }

    /**
     * Creates a new descriptor.
     *
     * @param engineType the engine type. Must not be null.
     * @param engineId the engine id. Must not be null.
     * @param languageName the name of the scripting language. May be null.
     * @param engineName the name of the scripting engine. May be null.
     * @param contentType the content type of the script sources. Ignored
     *   if null.
     */
    public ScriptEngineDescriptor(
            @NotNull ScriptEngineType engineType,
            @NotNull String engineId,
            String engineName, String languageName,  String contentType) {
        this(engineType, engineId, engineName, languageName, contentType,
                null /* engineVersion */,
                null /* languageVersion */);
    }

    /**
     * Creates a new descriptor.
     *
     * @param engineType the engine type. Must not be null.
     * @param engineId the engine id. Must not be null.
     * @param languageName the name of the scripting language. May be null.
     * @param engineName the name of the scripting engine. May be null.
     * @param contentType the content type of the script sources. Ignored
     *   if null.
     * @param engineVersion the version of the engine
     * @param languageVersion the version the language
     */
    public ScriptEngineDescriptor(
            @NotNull ScriptEngineType engineType,
            @NotNull String engineId,
            String engineName, String languageName, String contentType,
            String engineVersion, String languageVersion) {
        Objects.requireNonNull(engineType);
        Objects.requireNonNull(engineId);
        this.engineId = engineId;
        this.engineType= engineType;
        this.languageName = languageName == null ? null : languageName.trim();
        this.engineName = engineName == null ? null : engineName.trim();
        if (contentType != null) {
            this.contentMimeTypes.add(contentType.trim());
        }
        this.engineVersion = engineVersion;
        this.languageVersion = languageVersion;
    }
    /**
     * Creates a new descriptor given a factory for JSR223-compatible script
     * engines.
     *
     * @param factory the factory. Must not be null.
     */
    public ScriptEngineDescriptor(@NotNull final ScriptEngineFactory factory) {
        Objects.requireNonNull(factory);
        this.engineType = ScriptEngineType.PLUGGED;
        final var engineNames = factory.getNames();
        if (engineNames == null || engineNames.isEmpty()) {
            logger.warning(MessageFormat.format("script engine factory ''{0}''"
                + " doesn''t provide engine names. Using engine factory name ''{0}'' instead.",
                factory.getEngineName()));
            this.engineId = factory.getEngineName();
            this.engineName = factory.getEngineName();
        } else {
            // use the first of the provided names as ID and engine name
            this.engineId = engineNames.get(0);
            if (factory.getEngineName() != null) {
                this.engineName = factory.getEngineName();
            } else {
                this.engineName = engineNames.get(0);
            }
        }
        initParametersForJSR223Engine(factory);
    }

    /**
     * Replies the local engine id for the given engine type, i.e. <code>js</code>
     *
     * @return the local engine id
     */
    public @NotNull String getLocalEngineId() {
        return engineId;
    }

    /**
     * Replies the ful engine id for the given engine type, i.e. <code>graalvm/js</code>
     *
     * @return the full engine id
     */
    public @NotNull String getFullEngineId() {
        return MessageFormat.format("{0}/{1}", engineType.preferencesValue,engineId);
    }

    /**
     * Replies the engine type
     *
     * @return engine type
     */
    public ScriptEngineType getEngineType() {
        return engineType;
    }

    /**
     * Replies a string representing the descriptor in the format
     * <em>engineType/engineInfo</em>.
     *
     * @return the preferences value
     * @see #buildFromPreferences()
     * @deprecated use {@link #getFullEngineId()} instead
     */
    @Deprecated(since = "0.3.1")
    public String getPreferencesValue() {
        return getFullEngineId();
    }

    /**
     * Replies the name of the scripting language supported by this scripting
     * engine, if it is known.
     *
     * @return the name of the scripting language supported by this scripting
     *  engine.
     */
    public Optional<String> getLanguageName() {
        return Optional.ofNullable(languageName);
    }

    /**
     * Replies the name of the scripting engine, if it is known.
     *
     * @return the name of the scripting engine
     */
    public Optional<String> getEngineName() {
        return Optional.ofNullable(engineName);
    }

    /**
     * Replies the version of the scripting engine, if it is known.
     *
     * @return the version of the scripting engine
     */
    public Optional<String> getEngineVersion() {
        return Optional.ofNullable(engineVersion);
    }

    /**
     * Replies the language version, if it is known.
     *
     * @return the language version
     */
    public Optional<String> getLanguageVersion() {
        return Optional.ofNullable(languageVersion);
    }

    /**
     * Replies the content types of the script source
     *
     * @return the content types. An unmodifiable list. An empty list, if no
     *  content types are known.
     */
    public Set<String> getContentMimeTypes() {
        return Collections.unmodifiableSet(contentMimeTypes);
    }

    /**
     * Sets the content mime types of the script source
     *
     * @param mimeTypes the content mime types
     */
    public void setContentMimeTypes(@NotNull Collection<String> mimeTypes) {
        this.contentMimeTypes.clear();
        this.contentMimeTypes.addAll(mimeTypes);
    }

    public String toString() {
        return getPreferencesValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((engineId == null) ? 0 : engineId.hashCode());
        result = prime * result
                + ((engineType == null) ? 0 : engineType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptEngineDescriptor other = (ScriptEngineDescriptor) obj;
        if (engineId == null) {
            if (other.engineId != null)
                return false;
        } else if (!engineId.equals(other.engineId))
            return false;
        return engineType == other.engineType;
    }

    /**
     * Replies true if this descriptor describes the GraalJS engine.
     *
     * @return true if this descriptor describes the GraalJS engine; false,
     * otherwise
     */
    public boolean isDescribingGraalJS() {
        return this.engineType.equals(ScriptEngineType.GRAALVM)
                && "js".equals(this.engineId);
    }
}
