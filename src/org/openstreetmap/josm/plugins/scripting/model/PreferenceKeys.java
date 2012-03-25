package org.openstreetmap.josm.plugins.scripting.model;

/**
 * Preferences keys used in the scripting plugin 
 */
public interface PreferenceKeys {
	/**
	 * <p>Preference entry for a list of jar files (full path per file) providing
	 * JSR 223 compatible scripting engines.</p>
	 * 
	 * <p><strong>Default:</strong></p> - empty collection
	 */
	String PREF_KEY_SCRIPTING_ENGINE_JARS = "scripting.engine-jars";
	
	/**
	 * <p>The preferences key for the script file history.</p> 
	 */
	String PREF_KEY_FILE_HISTORY = "scripting.RunScriptDialog.file-history";
	
	/**
	 * <p>The preferences key for the last script file name entered in the script file
	 * selection field.</p> 
	 */	
	String PREF_KEY_LAST_FILE = "scripting.RunScriptDialog.last-file";
	
	
	/**
	 * <p>The preferences key for the current default engine used in the scripting
	 * console. The value is string <em>type/id</em>, where <code>type</code> is
	 * either <code>embedded</code> or <code>plugged</code>.</p>
	 * 
	 * <p>If type is <code>plugged</code>, the the is the name of a JSR 223 compliant
	 * scripting engine. It is is <code>embedded</code> then the id is the name of an
	 * embedded scripting engine (currently always <code>rhino</code>).</p>
	 * 
	 * <p>If missing or invalid, the embedded default scripting engine is assumed.</p>
	 * 
	 */
	String PREF_KEY_SCRIPTING_ENGING = "scripting.console.default.engine";
}
