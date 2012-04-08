package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

/**
 * <p>Manages the state of the scripting console</p>
 */
public class ScriptEditorModel {
	// static private final Logger logger = Logger.getLogger(ConsoleModel.class.getName());

	/** 
	 * The current script engine as described by a {@link ScriptEngineDescriptor}. The value is a 
	 * {@link ScriptEngineDescriptor} or null. */
	static public final String PROP_SCRIPT_ENGINE  = ScriptEditorModel.class.getName() + ".scriptEngine";
	
	/** The current script file. If the value is null, there is no script file loaded. The value is 
	 *  a {@link File} or null.
	 */
	static public final String PROP_SCRIPT_FILE = ScriptEditorModel.class.getName() + ".scriptFile";
	
	private File scriptFile = null;
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);
	private ScriptEngineDescriptor descriptor;
	
	public void addPropertyChangeListener(PropertyChangeListener l){
		support.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l){
		support.removePropertyChangeListener(l);
	}
	
	public ScriptEditorModel() {
		this.descriptor = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
	}
	
	public ScriptEditorModel(ScriptEngineDescriptor desc) {
		if (desc == null) desc = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
		this.descriptor = desc;
	}

	/**
	 * Sets the script engine descriptor. Notifies property change listeners with a property change event
	 * for the property {@link #PROP_SCRIPT_ENGINE}.
	 * 
	 * @param desc the descriptor. If null, assumes {@link ScriptEngineDescriptor#DEFAULT_SCRIPT_ENGINE}
	 */
	public void setScriptEngineDescriptor(ScriptEngineDescriptor desc) {
		if (desc == null) desc = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
		if (desc.equals(this.descriptor)) return;
		ScriptEngineDescriptor old = this.descriptor;
		this.descriptor = desc;
		support.firePropertyChange(PROP_SCRIPT_ENGINE, old, this.descriptor);
	}
	
	/**
	 * Replies the script engine descriptor.
	 * 
	 * @return
	 */
	public ScriptEngineDescriptor getScriptEngineDescriptor() {
		return this.descriptor;
	}
	
	/**
	 * Replies the current script file, or null.
	 * 
	 * @return the current script file, or null
	 */
	public File getScriptFile() {
		return scriptFile;
	}

	/**
	 * Sets the current script file. May be null. Notifies property change listeners with a property
	 * change event for the property {@link #PROP_SCRIPT_FILE}.
	 * 
	 * @param scriptFile the file. May be null.
	 */
	public void setScriptFile(File scriptFile) {
		if (this.scriptFile == scriptFile) return;
		if (this.scriptFile != null && this.scriptFile.equals(scriptFile)) return;
		
		File oldValue = this.scriptFile;
		this.scriptFile = scriptFile;		
		support.firePropertyChange(PROP_SCRIPT_FILE, oldValue, this.scriptFile);
	}
}
