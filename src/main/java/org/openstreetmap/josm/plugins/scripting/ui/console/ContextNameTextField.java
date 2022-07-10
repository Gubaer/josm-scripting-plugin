package org.openstreetmap.josm.plugins.scripting.ui.console;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A text input field for a context name.
 */
public class ContextNameTextField extends JTextField {
    private final static Color ERROR_BG_COLOR = new Color(255,204,204);

    /**
     * Fired when the context of the text field becomes a valid or an invalid
     * context name.
     */
    public final static String PROP_IS_VALID_CONTEXT_NAME = ContextNameTextField.class.getName() + ".isValidContextName";

    final private ContextComboBoxModel model;
    private boolean isValidContextName = false;

    /**
     * Creates a new text field for a context name.
     *
     * Only accepts valid context names. A context name is valid if isn't blank and if
     * it isn't already used in the <code>model</code>.
     *
     * @param model the context combo box model
     */
    public ContextNameTextField(@NotNull final ContextComboBoxModel model) {
        Objects.requireNonNull(model);
        this.model = model;
        this.getDocument().addDocumentListener(new ContextNameChangeListener());
        firePropertyChange(PROP_IS_VALID_CONTEXT_NAME, isValidContextName, isValidContextName);
    }

    public boolean isValidContextName() {
        return isValidContextName;
    }

    private void renderNOK() {
        setBorder(BorderFactory.createLineBorder(Color.red));
        setBackground(ERROR_BG_COLOR);
    }

    private void renderOK() {
        setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        setBackground(Color.WHITE);
    }

    private void render() {
        final boolean oldIsValidContextName = isValidContextName;
        final var text = getText();
        if (text.isBlank()) {
            renderOK();
            setToolTipText("");
            isValidContextName = false;
        } else if (model.isAllowedNewContextName(text.trim())) {
            renderOK();
            setToolTipText("");
            isValidContextName = true;
        } else {
            renderNOK();
            setToolTipText(tr("Context name already exists. Please choose another name."));
            isValidContextName = false;
        }
        if (oldIsValidContextName != isValidContextName) {
            firePropertyChange(PROP_IS_VALID_CONTEXT_NAME, oldIsValidContextName, isValidContextName);
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
