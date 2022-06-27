package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.context.IContext;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.validation.constraints.NotNull;
import java.awt.*;

/**
 * A combo box for the contexts hosted by a scripting engine.
 */
public class ContextComboBox extends JComboBox<IContext> {

    /**
     * Creates the combo box with a new model.
     */
    public ContextComboBox() {
        super(new ContextComboBoxModel());
        setPreferredSize(new Dimension(200, getPreferredSize().height));
        setEditable(true);
        setRenderer(renderer);
        setEditor(editor);
    }

    /**
     * Creates the combo box for the combo box model <code>model</code>.
     *
     * @param model the model
     */
    public ContextComboBox(@NotNull final ComboBoxModel<IContext> model) {
        super(model);
        setPreferredSize(new Dimension(200, getPreferredSize().height));
        setEditable(true);
        setRenderer(renderer);
        setEditor(editor);
    }

    private static final Renderer renderer = new Renderer();
    private static final Editor editor = new Editor();

    static private class Editor extends BasicComboBoxEditor {
        private JTextField editor = new JTextField();

        @Override
        public Component getEditorComponent() {
            return editor;
        }

        @Override
        public void setItem(Object obj) {
            if (obj == null) {
                editor.setText("");
            } else if (obj instanceof IContext) {
                editor.setText(((IContext)obj).getDisplayName());
            } else {
                editor.setText("");
            }
        }
    }

    static private class Renderer extends JLabel implements ListCellRenderer<IContext> {

        Renderer() {
            setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends IContext> list, IContext context,
                                          int i, boolean isSelected, boolean cellHasFocus) {
            if (context == null) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setForeground(list.getForeground());
                setBackground(list.getBackground());
                setText("User defined contexts");
                return this;
            }
            setFont(getFont().deriveFont(Font.PLAIN));
            setText(context.getDisplayName());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            return this;
        }
    }
}
