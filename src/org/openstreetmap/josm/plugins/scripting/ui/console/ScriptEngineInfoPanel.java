package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineSelectionDialog;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * Displays summary information about the currently selected scripting engine.
 *
 */
public class ScriptEngineInfoPanel extends JPanel implements PropertyChangeListener, HyperlinkListener{
    static private final Logger logger = Logger.getLogger(ScriptEngineInfoPanel.class.getName());

    private JEditorPane jepInfo;
    private ScriptEditorModel model;

    /**
     * Creates a new info panel
     *
     * @param model the model to listen too for updated script engines
     */
    public ScriptEngineInfoPanel(ScriptEditorModel model){
        Assert.assertArgNotNull(model, "model");
        model.addPropertyChangeListener(this);
        this.model = model;
        build();
        refreshInfo(model.getScriptEngineDescriptor());
    }

    protected void build() {
        jepInfo = new JEditorPane("text/html", "");
        jepInfo.setOpaque(false);
        jepInfo.setEditable(false);
        jepInfo.addHyperlinkListener(this);
        Font f = UIManager.getFont("Label.font");
        StyleSheet ss = new StyleSheet();
        String rule = MessageFormat
                .format("font-family: ''{0}'';font-size: {1,number}pt; font-weight: {2}; font-style: {3}",
                        f.getName(), f.getSize(), f.isBold() ? "bold"
                                : "normal", f.isItalic() ? "italic" : "normal");
        rule = "body {" + rule + "}";
        rule = MessageFormat
                .format("font-family: ''{0}'';font-size: {1,number}pt; font-weight: {2}; font-style: {3}",
                        f.getName(), f.getSize(), "bold",
                        f.isItalic() ? "italic" : "normal");
        rule = "strong {" + rule + "}";
        ss.addRule(rule);
        ss.addRule("a {text-decoration: underline; color: blue}");
        HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(ss);
        jepInfo.setEditorKit(kit);
        setLayout(new BorderLayout());
        add(jepInfo, BorderLayout.CENTER);
    }

    public ScriptEngineInfoPanel() {
        build();
    }

    protected void refreshInfo(ScriptEngineDescriptor desc){
        StringBuffer sb = new StringBuffer();
        if (desc == null){
            sb.append("<html>");
            sb.append(tr("No script engine selected.")).append(" ");
            sb.append("<a href=\"urn:select-script-engine\">").append(tr("Select...")).append("</a>");
            sb.append("<html>");
        } else if (desc.isDefault()) {
            sb.append("<html>");
            sb.append(
                    tr("Executing scripts with the built-in scripting engine for <strong>{0}</strong> based on <strong>{1}</strong>.",
                    desc.getLanguageName(),
                    desc.getEngineName()
                    )
            );
            sb.append(" ");
            sb.append("<a href=\"urn:change-script-engine\">").append(tr("Change...")).append("</a>");
            sb.append("</html>");
        } else {
            sb.append("<html>");
            sb.append(tr(
                    "Executing scripts in language <strong>{0}</strong> using engine <strong>{1}</strong>.",
                    desc.getLanguageName() == null ? tr("unknown") : desc.getLanguageName(),
                    desc.getEngineName() == null ? tr("unknown") : desc.getEngineName()
                )
            );
            sb.append(" ");
            sb.append("<a href=\"urn:change-script-engine\">").append(tr("Change...")).append("</a>");
            sb.append("</html>");
        }
        jepInfo.setText(sb.toString());
    }

    protected void promptForScriptEngine() {
        ScriptEngineDescriptor desc = ScriptEngineSelectionDialog.select(this, model.getScriptEngineDescriptor());
        if (desc != null){
            model.setScriptEngineDescriptor(desc);
        }
    }

    /* ----------------------------------------------------------------------- */
    /* interface PropertyChagneListener                                        */
    /* ----------------------------------------------------------------------- */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(ScriptEditorModel.PROP_SCRIPT_ENGINE)) return;
        refreshInfo((ScriptEngineDescriptor)evt.getNewValue());
    }

    /* ----------------------------------------------------------------------- */
    /* interface HyperlinkListener                                             */
    /* ----------------------------------------------------------------------- */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            promptForScriptEngine();
        }
    }
}
