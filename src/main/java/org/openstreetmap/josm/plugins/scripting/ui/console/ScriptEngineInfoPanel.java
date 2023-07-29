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
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
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

    private static String buildSelectScriptEngineLink(final String label) {
        // http://josm/select-script-engine is "internal" URL, HyperlinkListener
        // will respond to it and open the ScriptEngineSelectionDialog
        return String.format("<a href=\"http://josm/select-script-engine\">%s</a>", label);
    }

    private static String buildJavascriptAPIHint(ScriptEngineDescriptor desc) {
        final var sb = new StringBuilder();
//        if (desc.isDescribingMozillaRhino()) {
//            sb.append("<p>");
//            sb.append(tr(
//                "Mozilla Rhino and the <a href=\"{0}\">JavaScript API V1</a> " +
//                "are <span class=\"warning\">deprecated</span>. They will be " +
//                "removed end of 2022.",
//                "https://gubaer.github.io/josm-scripting-plugin/docs/v1/v1.html"
//                )
//            );
//            sb.append("</p>");
//            return sb.toString();
//        } else
        if (desc.isDescribingGraalJS()) {
            sb.append("<p>");
            sb.append(tr(
                "With GraalJS, use <a href=\"{0}\">JavaScript API V3</a>.",
                "https://gubaer.github.io/josm-scripting-plugin/docs/v3/v3.html"
            ));
            sb.append("</p>");
            return sb.toString();
        } else {
            return "";
        }
    }

    protected void refreshInfo(ScriptEngineDescriptor desc){
        final var sb = new StringBuilder();
        if (desc == null){
            sb.append("<html>");
            sb.append(tr("No script engine selected.")).append(" ");
            sb.append(buildSelectScriptEngineLink(tr("Select") + "..."));
            sb.append("</html>");
        }
//        else if (desc.isDefault()) {
//            sb.append("<html>");
//            sb.append("<p>");
//            sb.append(
//                tr("Executing scripts with the built-in scripting engine "
//                + "for language <strong>{0}</strong> based on "
//                + "<strong>{1}</strong>.",
//                desc.getLanguageName().orElse(tr("unknown")),
//                desc.getEngineName().orElse(tr("unknown"))
//                )
//            );
//            sb.append(" ").append(buildSelectScriptEngineLink(tr("Change") + "..."));
//            sb.append("</p>");
//            sb.append(buildJavascriptAPIHint(desc));
//            sb.append("</html>");
//        }
        else {
            sb.append("<html>");
            sb.append("<p>");
            sb.append(tr("Executing scripts in language <strong>{0}</strong> "
                + "using engine <strong>{1}</strong>.",
                desc.getLanguageName().orElse(tr("unknown")),
                ScriptEngineCellRenderer.defaultEngineName(desc))
            );
            sb.append(" ").append(buildSelectScriptEngineLink(tr("Change") + "..."));
            sb.append("</p>");
            sb.append(buildJavascriptAPIHint(desc));
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
            if ("http://josm/select-script-engine".equals(e.getURL().toString())) {
                promptForScriptEngine();
                return;
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ex) {
                    logger.log(Level.WARNING, MessageFormat.format(
                        "Failed to convert URL ''{0}'' to URI. Can't launch system web browser.",
                        e.getURL().toString()
                    ));
                }
            } else {
                logger.warning("Desktop is not supported. Can't launch system web browser.");
            }
        }
    }
}
