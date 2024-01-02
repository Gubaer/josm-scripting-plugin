package org.openstreetmap.josm.plugins.scripting.model;

import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.IOAccess;

/**
 * Preferences keys used in the scripting plugin
 */
public interface PreferenceKeys {
    /**
     * Preference entry for a list of jar files (full path per file)
     * providing JSR 223 compatible scripting engines.
     * <p>
     * <strong>Default:</strong> empty collection
     */
    String PREF_KEY_SCRIPTING_ENGINE_JARS = "scripting.engine-jars";

    /**
     * The preferences key for the script file history.
     * <p>
     * Used until version 0.3.0 of the scripting plugin. Replaced by {@link #PREF_KEY_MOST_RECENTLY_USED_SCRIPTS}.
     */
    @Deprecated(since = "0.3.1")
    String PREF_KEY_FILE_HISTORY = "scripting.RunScriptDialog.file-history";


    /**
     * The preference key for the list of most recently used scripts. Remembers
     * the path to the script and the engine's ID to run the script.
     * <p>
     * Used since version 0.3.1 of the scripting plugin. Replaces {@link #PREF_KEY_FILE_HISTORY}.
     *
     * @since 0.3.1
     */
    String PREF_KEY_MOST_RECENTLY_USED_SCRIPTS = "scripting.RunScriptDialog.most-recently-used-scripts";

    /**
     * The preferences key for the last script file name entered in the
     * script file selection field.
     */
    String PREF_KEY_LAST_FILE = "scripting.RunScriptDialog.last-file";

    /**
     * The preferences key for the current default engine used in the
     * scripting console. The value is string <em>type/id</em>, where
     * <code>type</code> is either <code>plugged</code>, or <code>graalvm</code>.
     * <p>
     * If type is <code>plugged</code>, the value is the name of a JSR 223
     * compliant scripting engine.  If the type is <code>graalvm</code>, <code>id</code>
     * is the id of a language supported by the GraalVM.
     * <p>
     * If missing or invalid, no default engine is assumed.
     */
    String PREF_KEY_SCRIPTING_ENGINE = "scripting.console.default.engine";

    /**
     * The array of ES Module repositories. Entries are URLs, either file URLs or jar URLs.
     * <p>
     * Default value: empty
     */
    String PREF_KEY_GRAALVM_ES_MODULE_REPOSITORIES = "scripting.graalvm.es-module-repositories";

    /**
     * The array of CommonJS module repositories used in the GraalVM.
     * Entries are URLs, either file URLs or jar URLs.
     * <p>
     * Default value: empty
     */
    String PREF_KEY_GRAALVM_COMMONJS_MODULE_REPOSITORIES = "scripting.graalvm.commonjs-module-repositories";

    /**
     * The array of local paths which are added to
     * the <tt>sys.path</tt> of the internal Jython interpreter
     * <p>
     * Default value: empty
     */
    String PREF_KEY_JYTHON_SYS_PATHS = "scripting.jython.sys.path";

    /**
     * The policy whether the GraalVM is granted the privilege to create and
     * execute external processes.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     */
    String GRAALVM_CREATE_PROCESS_POLICY = "scripting.graalvm.create-process-policy";

    /**
     * The policy whether the GraalVM is granted the privilege to create
     * threads.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     */
    String GRAALVM_CREATE_THREAD_POLICY = "scripting.graalvm.create-thread-policy";

    /**
     * The policy whether the GraalVM can use experimental language options.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowExperimentalOptions(boolean)
     */
    String GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY = "scripting.graalvm.use-experimental-options-policy";

    /**
     * The policy whether the GraalVM can load new host classes via jar or
     * class file.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowHostClassLoading(boolean)
     */
    String GRAALVM_HOST_CLASS_LOADING_POLICY = "scripting.graalvm.host-class-loading-policy";

    /**
     * The policy whether the GraalVM can execute IO operations.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowIO(IOAccess) (boolean)
     */
    String GRAALVM_IO_POLICY = "scripting.graalvm.io-policy";

    /**
     * The policy whether the GraalVM can execute native methods.
     * <p>
     * Either <code>allow</code>, <code>deny</code>, or <code>derive</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowNativeAccess(boolean)
     */
    String GRAALVM_NATIVE_ACCESS_POLICY = "scripting.graalvm.native-access-policy";

    /**
     * The policy whether the GraalVM can read environment variables.
     * <p>
     * Either <code>derive</code>, or <code>none</code>.
     * Default (if missing or illegal) is <code>derive</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowEnvironmentAccess(EnvironmentAccess)
     */
    String GRAALVM_ENVIRONMENT_ACCESS_POLICY = "scripting.graalvm.environment-access-policy";

    /**
     * The policy whether and how the GraalVM can access public constructors,
     * methods or fields of public classes.
     * <p>
     * Either <code>all</code>, <code>none</code>, or <code>explicit</code>.
     * Default (if missing or illegal) is <code>explicit</code>.
     *
     * @see org.graalvm.polyglot.Context.Builder#allowHostAccess(HostAccess) 
     */
    String GRAALVM_HOST_ACCESS_POLICY = "scripting.graalvm.host-access-policy";
}
