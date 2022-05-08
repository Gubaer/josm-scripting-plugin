package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineCellRenderer;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineSelectionDialog;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Displays summary information about the currently selected scripting engine.
 */
@SuppressWarnings("unused")
public class ScriptEngineInfoPanel extends JPanel implements
PropertyChangeListener, HyperlinkListener{
    @SuppressWarnings("unused")
    static private final Logger logger =
        Logger.getLogger(ScriptEngineInfoPanel.class.getName());

    private JEditorPane jepInfo;
    private ScriptEditorModel model;

    /**
     * Creates a new info panel
     *
     * @param model the model to listen too for updated script engines
     */
    public ScriptEngineInfoPanel(@NotNull ScriptEditorModel model){
        Objects.requireNonNull(model);
        model.addPropertyChangeListener(this);
        this.model = model;
        build();
        refreshInfo(model.getScriptEngineDescriptor());
    }

    protected void build() {
        jepInfo = EditorPaneBuilder.buildInfoEditorPane();
        jepInfo.addHyperlinkListener(this);
        setLayout(new BorderLayout());
        add(jepInfo, BorderLayout.CENTER);
    }

    public ScriptEngineInfoPanel() {
        build();
    }

    private static void appendSelectScriptEngineLink(final StringBuilder sb,
                                                     final String label) {
        sb.append("<a href=\"urn:select-script-engine\">")
                .append(tr(label)).append("</a>");
    }

    protected void refreshInfo(ScriptEngineDescriptor desc){
        StringBuilder sb = new StringBuilder();
        if (desc == null){
            sb.append("<html>");
            sb.append(tr("No script engine selected.")).append(" ");
            appendSelectScriptEngineLink(sb, "Select");
            sb.append("<html>");
        } else if (desc.isDefault()) {
            sb.append("<html>");
            sb.append(
                    tr("Executing scripts with the built-in scripting engine "
                    +"for language <strong>{0}</strong> based on "
                    + "<strong>{1}</strong>.",
                    desc.getLanguageName().orElse(tr("unknown")),
                    desc.getEngineName().orElse(tr("unknown"))
                    )
            );
            sb.append(" ");
            appendSelectScriptEngineLink(sb, "Change");
            sb.append("</html>");
        } else {
            sb.append("<html>");
            sb.append(tr("Executing scripts in language <strong>{0}</strong> "
                + "using engine <strong>{1}</strong>.",
                desc.getLanguageName().orElse(tr("unknown")),
                ScriptEngineCellRenderer.defaultEngineName(
                    desc.getEngineName().orElse(null)))
            );
            sb.append(" ");
            appendSelectScriptEngineLink(sb, "Change");
            sb.append("</html>");
        }
        jepInfo.setText(sb.toString());
    }

    protected void promptForScriptEngine() {
        final var desc = ScriptEngineSelectionDialog.select(
            this, model.getScriptEngineDescriptor());
        if (desc != null){
            logger.log(Level.FINE, String.format(
                "Interactively selected script engine. id=%s, language=%s",
                desc.getEngineId(),
                desc.getLanguageName()
            ));
            model.setScriptEngineDescriptor(desc);
        } else {
            logger.log(Level.FINE, "No script engine selected");
        }
    }

    /* --------------------------------------------------------------------- */
    /* interface PropertyChangeListener                                      */
    /* --------------------------------------------------------------------- */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(
                ScriptEditorModel.PROP_SCRIPT_ENGINE)) {
            return;
        }
        refreshInfo((ScriptEngineDescriptor)evt.getNewValue());
    }

    /* --------------------------------------------------------------------- */
    /* interface HyperlinkListener                                           */
    /* --------------------------------------------------------------------- */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            promptForScriptEngine();
        }
    }
}
