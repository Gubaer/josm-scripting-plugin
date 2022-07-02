package org.openstreetmap.josm.plugins.scripting.ui.console;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

public class ContextNameTextField extends JTextField {

    private ContextComboBoxModel model;

    public ContextNameTextField(@NotNull final ContextComboBoxModel model) {
        Objects.requireNonNull(model);
        this.model = model;
        this.getDocument().addDocumentListener(new ContextNameChangeListener());
    }

    private void renderNOK() {
        setBorder(BorderFactory.createLineBorder(Color.red));
        setBackground(Color.RED.brighter());
    }

    private void renderOK() {
        setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        setBackground(Color.WHITE);
    }

    private void render() {
        final var text = getText();
        if (text.isBlank()) {
            renderOK();
            setToolTipText("");
        } else if (model.isAllowedNewContextName(text.trim())) {
            renderOK();
            setToolTipText("");
        } else {
            renderNOK();
            setToolTipText(tr("Context name already exists. Please choose another name."));
        }
    }

    class ContextNameChangeListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            render();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            render();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            render();
        }
    }
}
