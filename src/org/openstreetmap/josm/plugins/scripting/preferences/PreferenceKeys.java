package org.openstreetmap.josm.plugins.scripting.preferences;

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
}
