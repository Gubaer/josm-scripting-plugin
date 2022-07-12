package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.context.ContextRegistry;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

public class CreateContextAction extends AbstractAction implements PropertyChangeListener {

    private final ContextNameTextField contextNameTextField;
    private final ContextComboBoxModel contextComboBoxModel;

    /**
     * Creates an action
     *
     * @param contextNameTextField the text field with the new context name
     * @param contextComboBoxModel the model with the list of currently defined contexts
     */
    CreateContextAction(@NotNull final ContextNameTextField contextNameTextField,
                        @NotNull final ContextComboBoxModel contextComboBoxModel) {
        Objects.requireNonNull(contextNameTextField);
        Objects.requireNonNull(contextComboBoxModel);
        this.contextComboBoxModel = contextComboBoxModel;
        this.contextNameTextField = contextNameTextField;
        //putValue(NAME, tr("Create"));
        putValue(SHORT_DESCRIPTION, tr("Create a new context"));
        putValue(SMALL_ICON, ImageProvider.get("add", ImageProvider.ImageSizes.SMALLICON));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!contextNameTextField.isValidContextName()) {
            return;
        }
        final var name = contextNameTextField.getText();
        ContextRegistry.getInstance().createUserDefinedContext(
            name, contextComboBoxModel.getEngine()
        );
        contextNameTextField.setText("");
        contextComboBoxModel.selectByName(name);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!ContextNameTextField.PROP_IS_VALID_CONTEXT_NAME.equals(event.getPropertyName())) {
            return;
        }
        final var value = (boolean)event.getNewValue();
        setEnabled(value);
    }
}
