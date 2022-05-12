package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SaveAsAction extends AbstractAction {
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(SaveAsAction.class.getName());

    private File askFile() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(tr("Select a script"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileHidingEnabled(false);
        final int ret = chooser.showSaveDialog(ScriptingConsole.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return null;

        return chooser.getSelectedFile();
    }

    SaveAsAction() {
        putValue(NAME, tr("Save as ..."));
        putValue(SHORT_DESCRIPTION, tr("Save to a script file"));
        putValue(SMALL_ICON, ImageProvider.get("save_as", ImageProvider.ImageSizes.MENU));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final File f = askFile();
        if (f == null) return;
        ScriptingConsole.getInstance().save(f);
    }
}
