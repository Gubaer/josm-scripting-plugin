package org.openstreetmap.josm.plugins.scripting.ui;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

public class MostRecentlyRunScriptsComboBox extends JComboBox<String>{

    public MostRecentlyRunScriptsComboBox(MostRecentlyRunScriptsModel model) {
        setModel(model.getComboBoxModel());
        setEditable(true);
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
