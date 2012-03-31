package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import javax.script.ScriptEngineFactory;

import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;

/**
 * <p>Manages state of the scripting console</p>
 */
public class ScriptEditorModel {
	// static private final Logger logger = Logger.getLogger(ConsoleModel.class.getName());
	static public final StringProperty PREF_SCRIPT_ENGINE_NAME = new StringProperty(
			ScriptEditorModel.class.getName() + ".scriptEngineName", 
			null
	);
	
	static public final String PROP_SCRIPT_ENGINE_FACTORY = ScriptEditorModel.class.getName() + ".scriptEngineFactory";
	static public final String PROP_SCRIPT_FILE = ScriptEditorModel.class.getName() + ".scriptFile";
	
	private ScriptEngineFactory factory = null;
	private File scriptFile = null;
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		support.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l){
		support.removePropertyChangeListener(l);
	}

	protected void restoreFromPreferences() {
		String name = PREF_SCRIPT_ENGINE_NAME.get();
		if (name == null){
			factory = null;
		} else {
			factory = JSR223ScriptEngineProvider.getInstance().getScriptFactoryByName(name);			
		}
	}
	
	protected void saveToPreferences() {
		if (factory == null){
			PREF_SCRIPT_ENGINE_NAME.put(null);
		} else {
			PREF_SCRIPT_ENGINE_NAME.put(factory.getEngineName());
		}
	}
	
	public ScriptEditorModel() {
		restoreFromPreferences();
	}
	
	public ScriptEngineFactory getScriptEngineFactory() {
		return factory;
	}

	public void setScriptEngineFactory(ScriptEngineFactory factory) {
		if (this.factory == factory) return;
		ScriptEngineFactory oldValue = this.factory;
		this.factory = factory;
		support.firePropertyChange(PROP_SCRIPT_ENGINE_FACTORY, oldValue, this.factory);
		saveToPreferences();
	}

	public File getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(File scriptFile) {
		if (this.scriptFile == scriptFile) return;
		if (this.scriptFile != null && this.scriptFile.equals(scriptFile)) return;
		
		File oldValue = this.scriptFile;
		this.scriptFile = scriptFile;		
		support.firePropertyChange(PROP_SCRIPT_FILE, oldValue, this.scriptFile);
	}
}
