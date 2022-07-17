package org.openstreetmap.josm.plugins.scripting.ui.runner;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class MostRecentlyRunScriptsComboBox extends JComboBox<String>{

    private static class Editor extends BasicComboBoxEditor {
        private ScriptFileNameEditor editor;

        @Override
        protected JTextField createEditorComponent() {
            editor = new ScriptFileNameEditor();
            return editor;
        }

        @Override
        public Component getEditorComponent() {
            return editor;
        }
    }

    public MostRecentlyRunScriptsComboBox(MostRecentlyRunScriptsModel model) {
        setModel(model.getComboBoxModel());
        setEditable(true);
        setEditor(new Editor());
    }

    public String getText() {
        return ((JTextComponent)getEditor().getEditorComponent()).getText();
    }

    public void setText(String value) {
        //setAutocompleteEnabled(false);
        ((JTextComponent)getEditor().getEditorComponent()).setText(value);
        //setAutocompleteEnabled(true);
    }
}
