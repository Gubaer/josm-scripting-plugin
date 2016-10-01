package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.tools.ImageProvider;

public class SaveAction extends AbstractAction implements
        PropertyChangeListener{
    static private final Logger logger =
           Logger.getLogger(SaveAsAction.class.getName());

    public SaveAction() {
        putValue(NAME, tr("Save"));
        putValue(SHORT_DESCRIPTION, tr("Save script to current file"));
        putValue(SMALL_ICON, ImageProvider.get("save"));
        updateEnabledState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScriptingConsole.getInstance().save();
    }

    protected void updateEnabledState() {
        final ScriptingConsole console = ScriptingConsole.getInstance();
        if (console == null){
            setEnabled(false);
            return;
        }
        setEnabled(console.getScriptEditorModel().getScriptFile() != null);
    }

    /* --------------------------------------------------------------------- */
    /* interface PropertyChangeListener                                      */
    /* --------------------------------------------------------------------- */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabledState();
    }
}