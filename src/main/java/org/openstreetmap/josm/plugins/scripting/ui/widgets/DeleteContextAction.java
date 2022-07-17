package org.openstreetmap.josm.plugins.scripting.ui.widgets;

import org.openstreetmap.josm.plugins.scripting.context.ContextRegistry;
import org.openstreetmap.josm.plugins.scripting.context.IContext;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

public class DeleteContextAction extends AbstractAction implements ItemListener {

    private final ContextComboBoxModel contextComboBoxModel;

    /**
     * Creates the action
     *
     * @param contextComboBoxModel the model with the list of currently defined contexts
     */
    DeleteContextAction(@NotNull final ContextComboBoxModel contextComboBoxModel) {
        Objects.requireNonNull(contextComboBoxModel);
        this.contextComboBoxModel = contextComboBoxModel;
        var selectedContext = (IContext) contextComboBoxModel.getSelectedItem();
        if (selectedContext == null || selectedContext.isDefault()) {
            setEnabled(false);
        }
        putValue(SHORT_DESCRIPTION, tr("Delete the selected context"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs/delete", ImageProvider.ImageSizes.SMALLICON));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        var selectedContext = (IContext) contextComboBoxModel.getSelectedItem();
        if (selectedContext == null || selectedContext.isDefault()) {
            return;
        }
        ContextRegistry.getInstance().removeContext(selectedContext);
        contextComboBoxModel.removeElement(selectedContext);
        contextComboBoxModel.selectDefaultContext();
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            var selectedContext = (IContext) contextComboBoxModel.getSelectedItem();
            setEnabled(!selectedContext.isDefault());
        }
    }
}
