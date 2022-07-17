package org.openstreetmap.josm.plugins.scripting.ui.runner;

import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Action to launch a {@link JFileChooser} to select a script file
 */
public class SelectScriptFileAction extends AbstractAction{

    private MostRecentlyRunScriptsComboBox mostRecentlyRunScripts;

    /**
     * Creates the action
     *
     * @param mostRecentlyRunScripts the editor for the most recently run scripts
     */
    SelectScriptFileAction(@NotNull final MostRecentlyRunScriptsComboBox mostRecentlyRunScripts) {
        Objects.requireNonNull(mostRecentlyRunScripts);
        this.mostRecentlyRunScripts = mostRecentlyRunScripts;
        putValue(SHORT_DESCRIPTION, tr("Launch file selection dialog"));
        putValue(SMALL_ICON, ImageProvider.get("open", ImageProvider.ImageSizes.SMALLICON));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final var fileName = mostRecentlyRunScripts.getText().trim();
        File currentFile = null;
        if (!fileName.isEmpty()) {
            currentFile = new File(fileName);
        }
        final var chooser = new JFileChooser();
        chooser.setDialogTitle(tr("Select a script file"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileHidingEnabled(false);
        if (currentFile != null) {
            chooser.setCurrentDirectory(currentFile);
            chooser.setSelectedFile(currentFile);
        }
        int ret = chooser.showOpenDialog(RunScriptDialog.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        currentFile = chooser.getSelectedFile();
        mostRecentlyRunScripts.setText(currentFile.toString());
    }
}
