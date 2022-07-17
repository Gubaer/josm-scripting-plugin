package org.openstreetmap.josm.plugins.scripting.ui.widgets;

import org.openstreetmap.josm.plugins.scripting.context.ContextRegistry;
import org.openstreetmap.josm.plugins.scripting.context.IContext;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A panel to interactively select or create a scripting context for the current scripting
 * engine.
 *
 * This panel is a {@link ICurrentContextSource} and fires a {@link ICurrentContextSource#PROP_SELECTED_CONTEXT}
 * property change event when the currently selected context changes.
 */
public class SelectContextPanel extends JPanel implements ICurrentContextSource, PropertyChangeListener {

    static private final Logger logger = Logger.getLogger(SelectContextPanel.class.getName());

    private ContextComboBoxModel contextComboBoxModel;
    private ContextComboBox contextComboBox;

    private IContext selectedContext;

    public SelectContextPanel() {
        build();
    }

    protected void build() {
        setLayout(new GridBagLayout());
        final var builder = new GridBagConstraintBuilder();

        // the model listens to property change events from the
        // context registry
        contextComboBoxModel = new ContextComboBoxModel();
        ContextRegistry.getInstance().addPropertyChangeListener(contextComboBoxModel);

        final var insets = new Insets(2,2,2,2);
        add(
            new JLabel(tr("Existing:")),
            builder.gridx(0).gridy(0).weightx(0.0).insets(insets).constraints()
        );
        contextComboBox = new ContextComboBox(contextComboBoxModel);
        add(
            contextComboBox,
            builder.gridx(1).gridy(0).weightx(1.0).insets(insets).constraints()
        );
        contextComboBox.addItemListener(new SelectedContextListener());

        // the 'delete' button with its action
        final var deleteContextAction = new DeleteContextAction(contextComboBoxModel);
        final var btnDelete = new JButton(deleteContextAction);
        btnDelete.setMargin(new Insets(0,0,0,0));
        btnDelete.setContentAreaFilled(false);
        contextComboBox.addItemListener(deleteContextAction);

        add(
            btnDelete,
            builder.gridx(2).gridy(0).weightx(0.0).insets(insets).constraints()
        );
        add(
            new JLabel(tr("New:")),
            builder.gridx(0).gridy(1).weightx(0.0).insets(insets).constraints()
        );
        final var contextNameTextField = new ContextNameTextField(contextComboBoxModel);
        add(
            contextNameTextField,
            builder.gridx(1).gridy(1).weightx(1.0).insets(insets).constraints()
        );

        // the 'create' button with its action
        final var createContextAction = new CreateContextAction(contextNameTextField, contextComboBoxModel);
        final var btnCreate = new JButton(createContextAction);
        btnCreate.setMargin(new Insets(0,0,0,0));
        btnCreate.setContentAreaFilled(false);
        contextNameTextField.addPropertyChangeListener(createContextAction);

        // the context name field action. Don't listen to property changes PROP_IS_VALID_CONTEXT_NAME
        // because text field is always enabled, even if current input is invalid
        final var contextNameTextFieldAction = new CreateContextAction(contextNameTextField, contextComboBoxModel);
        contextNameTextField.setAction(contextNameTextFieldAction);

        add(
            btnCreate,
            builder.gridx(2).gridy(1).weightx(0.0).fillboth().insets(insets).constraints()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IContext getSelectedContext() {
        return (IContext) contextComboBox.getSelectedItem();
    }

    /**
     * Sets the engine for which scripting contexts can be selected.
     *
     * @param engine the engine
     */
    public void setEngine(ScriptEngineDescriptor engine) {
        contextComboBoxModel.setEngine(engine);
    }

    class SelectedContextListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getSource() != contextComboBox) {
                return;
            }
            switch(event.getStateChange()) {
                case ItemEvent.DESELECTED:
                case ItemEvent.SELECTED:
                    final var oldSelectedContext = selectedContext;
                    selectedContext = (IContext) contextComboBox.getSelectedItem();
                    firePropertyChange(
                        ICurrentContextSource.PROP_SELECTED_CONTEXT,
                        oldSelectedContext,
                        selectedContext
                    );
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        logger.info("property changed " + event.getPropertyName());
        if (!event.getPropertyName().equals(IScriptEngineInfoModel.PROP_SCRIPT_ENGINE)) {
            return;
        }
        setEngine((ScriptEngineDescriptor) event.getNewValue());
    }
}
