package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.graalvm.IGraalVMContext;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptErrorViewerModel;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Action to run the script which is currently edited in the scripting console.
 */
public class RunScriptAction extends AbstractAction implements PropertyChangeListener, ItemListener {

    static public final Logger logger = Logger.getLogger(RunScriptAction.class.getName());

    final private ScriptEditorModel model;
    final private ScriptErrorViewerModel errorModel;
    final private ContextComboBoxModel contextComboBoxModel;

    final private ScriptEditor scriptEditor;

    public RunScriptAction(@NotNull final ScriptEditor scriptEditor,
                           @NotNull final ScriptErrorViewerModel errorModel,
                           @NotNull final ContextComboBoxModel contextComboBoxModel) {
        Objects.requireNonNull(scriptEditor);
        Objects.requireNonNull(errorModel);
        Objects.requireNonNull(contextComboBoxModel);
        this.scriptEditor = scriptEditor;
        this.model = scriptEditor.getModel();
        this.errorModel = errorModel;
        this.contextComboBoxModel  = contextComboBoxModel;
        putValue(SMALL_ICON, ImageProvider.get("media-playback-start", ImageProvider.ImageSizes.SMALLICON));
        putValue(SHORT_DESCRIPTION, tr("Execute the script"));
        putValue(NAME, tr("Run"));
        model.addPropertyChangeListener(this);
        updateEnabledState();
    }

    private void runOnSwingEDT(Runnable r){
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch(InvocationTargetException e){
                Throwable throwable = e.getCause();
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if(throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                // no other checked exceptions expected - log a warning
                logger.log(Level.WARNING, String.format(
                    "Unexpected exception wrapped in InvocationTargetException: %s",
                    throwable.toString()
                ), throwable);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Runs a script with a GraalVM engine.
     *
     * Runs the script on the Swing EDT. Handling errors is delegated to
     * <code>errorViewerModel</code>.
     *
     * @param context the context
     * @param script the script
     * @param errorViewerModel callback to handle errors
     */
    private void runScriptWithGraalEngine(
            @NotNull final IGraalVMContext context,
            final String script,
            final @Null ScriptErrorViewerModel errorViewerModel) {
        Objects.requireNonNull(context);
        if (script == null) {
            return;
        }
        final Runnable task = () -> {
            try {
                context.eval(script);
            } catch(Throwable e) {
                errorViewerModel.setError(e);
            }
        };
        runOnSwingEDT(task);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        errorModel.clearError();
        final var context = contextComboBoxModel.getSelectedItem();
        if (context == null) {
            logger.warning("No scripting context selected. Can't execute the script");
            return;
        }

        final String source = scriptEditor.getScript();
        switch(model.getScriptEngineDescriptor().getEngineType()) {
            case EMBEDDED:
                // TODO(gubaer): implement
//                new ScriptExecutor(ScriptingConsolePanel.this)
//                        .runScriptWithEmbeddedEngine(source, errorModel);
                break;
            case PLUGGED:
                // TODO(gubaer): implement
//                new ScriptExecutor(ScriptingConsolePanel.this)
//                        .runScriptWithPluggedEngine(
//                                model.getScriptEngineDescriptor(),
//                                source,
//                                errorModel
//                        );
                break;
            case GRAALVM:
                if (! (context instanceof  IGraalVMContext)) {
                    logger.warning(MessageFormat.format(
                        "Expected selected context of type ''{0}'', "
                        + "got type ''{1}''. Can''t execute script.",
                        IGraalVMContext.class.getName(),
                        context.getClass().getName()
                    ));
                }
                try {
                    runScriptWithGraalEngine(
                        (IGraalVMContext) context,
                        source,
                        errorModel
                    );
                } catch(Throwable ex) {
                    errorModel.setError(ex);
                }
                break;
        }
    }

    protected void updateEnabledState() {
        boolean enabled = model.getScriptEngineDescriptor() != null;
        enabled = enabled && contextComboBoxModel.getSelectedItem() != null;
        setEnabled(enabled);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(ScriptEditorModel.PROP_SCRIPT_ENGINE)) {
            return;
        }
        updateEnabledState();
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        switch(itemEvent.getStateChange()) {
            case ItemEvent.SELECTED:
            case ItemEvent.DESELECTED:
                updateEnabledState();
        }
    }
}