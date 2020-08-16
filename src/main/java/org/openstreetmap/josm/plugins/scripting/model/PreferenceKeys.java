package org.openstreetmap.josm.plugins.scripting.model;

/**
 * Preferences keys used in the scripting plugin
 */
public interface PreferenceKeys {
    /**
     * <p>Preference entry for a list of jar files (full path per file)
     * providing JSR 223 compatible scripting engines.</p>
     *
     * <p><strong>Default:</strong></p> - empty collection
     */
    String PREF_KEY_SCRIPTING_ENGINE_JARS = "scripting.engine-jars";

    /**
     * <p>The preferences key for the script file history.</p>
     */
    String PREF_KEY_FILE_HISTORY = "scripting.RunScriptDialog.file-history";

    /**
     * <p>The preferences key for the last script file name entered in the
     * script file selection field.</p>
     */
    String PREF_KEY_LAST_FILE = "scripting.RunScriptDialog.last-file";


    /**
     * The preferences key for the current default engine used in the
     * scripting console. The value is string <em>type/id</em>, where
     * <code>type</code> is either <code>embedded</code>,
     * <code>plugged</code>, or <code>graalvm</code>.
     * <p>
     *
     * If type is <code>plugged</code>, the value is the name of a JSR 223
     * compliant scripting engine. If it is <code>embedded</code>, then the id
     * is the name of an embedded scripting engine (currently always
     * <code>rhino</code>) If the type is <code>graalv</code>, <code>id</code>
     * is the id of a language supported by the GraalVM.
     * <p><
     *
     * If missing or invalid, the embedded default scripting engine is
     * assumed.
     *
     */
    String PREF_KEY_SCRIPTING_ENGINE = "scripting.console.default.engine";


    /**
     * <p>The array of CommonJS module repositories. Entries are URLs,
     * either file URLs or jar URLs.</p>
     *
     * <p>Default value: empty</p>
     */
    String PREF_KEY_COMMONJS_MODULE_REPOSITORIES =
            "scripting.commonjs-module-repositories";

    /**
     * <p>The array of local paths which are added to
     * the <tt>sys.path</tt> of the internal Jython interpreter</p>
     *
     * <p>Default value: empty</p>
     */
    String PREF_KEY_JYTHON_SYS_PATHS = "scripting.jython.sys.path";

    /**
     * <p>The array of python plugins to be loaded when JOSM
     * starts up, provided the Jython interpreter is on the class
     * path.</p>
     *
     * <p>Each entry has the form <tt>package_name.ClassName</tt>, i.e.
     * <tt>my_plugin.MyPlugin</tt>. The scripting plugin will load
     * it, if <tt>my_plugin.py</tt> is on the paths in <tt>sys.paths</tt>
     * and if it implements the python class <code>MyPlugin</code>.</p>
     *
     * <p>Default value: empty</p>
     */
    String PREF_KEY_JYTHON_PLUGINS = "scripting.jython.plugins";
}
