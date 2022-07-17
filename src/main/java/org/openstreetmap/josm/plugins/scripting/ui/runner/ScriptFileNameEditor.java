package org.openstreetmap.josm.plugins.scripting.ui.runner;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * An input text field for script file names.
 *
 * Used as editor in {@link MostRecentlyRunScriptsComboBox}.
 */
public class ScriptFileNameEditor extends JTextField {
    private final static Color ERROR_BG_COLOR = new Color(255,204,204);

    public ScriptFileNameEditor() {
        getDocument().addDocumentListener(new ScriptFileNameChecker());
    }

    private void renderNOK(String errorMessage) {
        setBorder(BorderFactory.createLineBorder(Color.red));
        setBackground(ERROR_BG_COLOR);
        if (errorMessage != null) {
            setToolTipText(errorMessage);
        }
    }

    private void renderOK() {
        setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        setBackground(Color.WHITE);
        setToolTipText("");
    }

    private void checkFileName() {
        final var name = getText();
        if (name == null || name.isBlank()) {
            renderOK();
            return;
        }
        final var file = new File(name);
        if (file.exists() && file.isFile() && file.canRead()) {
            renderOK();
        } else {
            renderNOK(tr("File doesn''t exist or isn''t readable"));
        }
    }

    class ScriptFileNameChecker implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            checkFileName();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            checkFileName();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            checkFileName();
        }
    }
}
