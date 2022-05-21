package org.openstreetmap.josm.plugins.scripting.ui;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.text.MessageFormat;
import java.util.logging.Logger;

public class EditorPaneBuilder {
    static private final String RESOURCE_NAME_STYLE_SHEET =
        "/css/default-editor-pane.css";
    static private final Logger logger =
        Logger.getLogger(EditorPaneBuilder.class.getName());

    private static StyleSheet loadDefaultStyleSheet() {
        var styleSheet = new StyleSheet();
        final var styleSheetUri =
            EditorPaneBuilder.class.getResource(RESOURCE_NAME_STYLE_SHEET);
        if (styleSheetUri == null) {
            logger.warning(String.format(
                "failed to load default CSS style sheet from resource '%s'",
                RESOURCE_NAME_STYLE_SHEET
            ));
            return styleSheet;
        }
        styleSheet.importStyleSheet(styleSheetUri);
        return styleSheet;
    }

    /**
     * Builds a non-editable {@link JEditorPane} and styles it. Font
     * parameters match the {@link UIManager}s font parameters.
     *
     * @return the info pane
     */
    public static JEditorPane buildInfoEditorPane() {
        final JEditorPane jepInfo =
            new JEditorPane("text/html", null /* no text */);
        jepInfo.setOpaque(false);
        jepInfo.setEditable(false);
        final StyleSheet ss = loadDefaultStyleSheet();

        // dynamically build a rule for <strong> using the current
        // font used in labels
        final Font f = UIManager.getFont("Label.font");
        final String cssRuleFontFamily =
            "font-family: ''{0}'';font-size: {1,number}pt; font-weight: {2}; " +
            "font-style: {3};";
        final String rule = "strong {" +
            MessageFormat.format(cssRuleFontFamily,
                f.getName(),
                f.getSize(),
                "bold",
                f.isItalic() ? "italic" : "normal") +
                "}";
        ss.addRule(rule);
        final HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(ss);
        jepInfo.setEditorKit(kit);
        return jepInfo;
    }
}
