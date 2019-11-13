package org.openstreetmap.josm.plugins.scripting.ui;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.text.MessageFormat;

public class EditorPaneBuilder {
    /**
     * Builds an non-editable {@link JEditorPane} and styles it. Font
     * parameters match the {@link UIManager}s font parameters.
     *
     * @return the info pane
     */
    public static JEditorPane buildInfoEditorPane() {
        final JEditorPane jepInfo =
            new JEditorPane("text/html", null /* no text */);
        jepInfo.setOpaque(false);
        jepInfo.setEditable(false);
        final Font f = UIManager.getFont("Label.font");
        final StyleSheet ss = new StyleSheet();
        final String cssRuleFontFamily =
            "font-family: ''{0}'';font-size: {1,number}pt; font-weight: {2}; " +
            "font-style: {3}";
        final String rule = "strong {" +
            MessageFormat.format(cssRuleFontFamily,
                f.getName(),
                f.getSize(),
                "bold",
                f.isItalic() ? "italic" : "normal") +
                "}";
        ss.addRule(rule);
        ss.addRule("a {text-decoration: underline; color: blue}");
        final HTMLEditorKit kit = new HTMLEditorKit();
        kit.setStyleSheet(ss);
        jepInfo.setEditorKit(kit);
        return jepInfo;
    }
}
