package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;

import javax.validation.constraints.Null;

/**
 * <p>Manages the state of the scripting console</p>
 */
public class ScriptEditorModel {
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(ScriptEditorModel.class.getName());

    /**
     * The current script engine as described by a
     * {@link ScriptEngineDescriptor}. The value is a
     * {@link ScriptEngineDescriptor} or null. */
    static final String PROP_SCRIPT_ENGINE  =
            ScriptEditorModel.class.getName() + ".scriptEngine";

    /** The current script file. If the value is null, there is no script file
     * loaded. The value is a {@link File} or null.
     */
    static final String PROP_SCRIPT_FILE =
            ScriptEditorModel.class.getName() + ".scriptFile";

    private File scriptFile = null;
    private final PropertyChangeSupport support =
            new PropertyChangeSupport(this);
    private ScriptEngineDescriptor descriptor;

    public void addPropertyChangeListener(PropertyChangeListener l){
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l){
        support.removePropertyChangeListener(l);
    }

    protected ScriptEngineDescriptor selectDefaultScriptEngine() {
        if (GraalVMFacadeFactory.isGraalVMPresent()) {
            return GraalVMFacadeFactory.getOrCreateGraalVMFacade()
                .getScriptEngineDescriptors().stream()
                .filter(desc -> "js".equals(desc.getEngineId()))
                .findFirst()
                .orElse(null);
        } else {
            return null;
        }
    }

    /**
     * Creates a new script editor model.
     * <p>
     * Initializes the model with the default scripting engine.
     */
    public ScriptEditorModel() {
        this.descriptor = selectDefaultScriptEngine();
    }

    /**
     * Creates a new script editor model.
     * <p>
     * Initializes the model with the scripting engine <code>desc</code>.
     * If <code>desc</code> is null initializes the model with the default
     * scripting engine.
     *
     * @param desc the scripting engine.
     */
    public ScriptEditorModel(final @Null ScriptEngineDescriptor desc) {
        this.descriptor = desc == null
            ? selectDefaultScriptEngine()
            : desc;
    }

    /**
     * Sets the script engine descriptor. Notifies property change listeners
     * with a property change event for the property {@link #PROP_SCRIPT_ENGINE}.
     *
     * @param desc the descriptor. Can be null.
     */
    public void setScriptEngineDescriptor(@Null ScriptEngineDescriptor desc) {
//        if (desc == null) desc = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
        if (desc == null && this.descriptor == null) return;
        if (desc != null && desc.equals(this.descriptor)) return;
//        if (desc.equals(this.descriptor)) return;
        ScriptEngineDescriptor old = this.descriptor;
        this.descriptor = desc;
        support.firePropertyChange(PROP_SCRIPT_ENGINE, old, this.descriptor);
    }

    /**
     * Replies the script engine descriptor.
     *
     * @return the descriptor
     */
    public ScriptEngineDescriptor getScriptEngineDescriptor() {
        return this.descriptor;
    }

    /**
     * Replies the current script file, if present
     *
     * @return the current script file
     */
    public Optional<File> getScriptFile() {
        return Optional.ofNullable(scriptFile);
    }

    /**
     * Sets the current script file. May be null. Notifies property change
     * listeners with a property change event for the property
     * {@link #PROP_SCRIPT_FILE}.
     *
     * @param scriptFile the file. May be null.
     */
    public void setScriptFile(File scriptFile) {
        if (this.scriptFile == scriptFile) return;
        if (this.scriptFile != null && this.scriptFile.equals(scriptFile)) {
            return;
        }

        File oldValue = this.scriptFile;
        this.scriptFile = scriptFile;
        support.firePropertyChange(PROP_SCRIPT_FILE, oldValue, this.scriptFile);
    }
}
