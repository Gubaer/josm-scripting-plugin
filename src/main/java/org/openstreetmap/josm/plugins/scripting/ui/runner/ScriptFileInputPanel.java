package org.openstreetmap.josm.plugins.scripting.ui.runner;

import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A widget for editing a script file name, or selecting a script file
 * name using {@link JFileChooser}, or from a list of most recently
 * run scripts.
 */
public class ScriptFileInputPanel extends JPanel {

    private MostRecentlyRunScriptsComboBox cbScriptFile;

    public ScriptFileInputPanel() {
        build();
    }

    protected void build() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = gbc().cell(0, 0).weight(0, 0).fillboth()
                .insets(3, 3, 3, 3).constraints();
        add(new JLabel(tr("File:")), gc);

        cbScriptFile = new MostRecentlyRunScriptsComboBox(
            MostRecentlyRunScriptsModel.getInstance()
        );
        SelectAllOnFocusGainedDecorator.decorate((JTextField) cbScriptFile
                .getEditor().getEditorComponent());
        cbScriptFile.setToolTipText(tr("Enter the name of a script file"));
        gc = gbc(gc).cell(1, 0).weightx(1.0).spacingright(0).constraints();
        add(cbScriptFile, gc);

        gc = gbc(gc).cell(2, 0).weightx(0.0).spacingleft(0).constraints();
        final var btnSelectScriptFile = new JButton(new SelectScriptFileAction(cbScriptFile));
        btnSelectScriptFile.setMargin(new Insets(0,0,0,0));
        btnSelectScriptFile.setContentAreaFilled(false);
        add(btnSelectScriptFile, gc);
        btnSelectScriptFile.setFocusable(false);
    }

    @Override
    public boolean requestFocusInWindow() {
        return cbScriptFile.requestFocusInWindow();
    }

    /**
     * Sets the file name in the editor
     *
     * @param fileName the file name. If null, clears the file name
     */
    public void setFileName(@Null String fileName) {
        if (fileName == null) {
            fileName = "";
        }
        fileName = fileName.trim();
        cbScriptFile.setText(fileName);
    }

    /**
     * Replies the current file name
     *
     * @return the file name
     */
    public @NotNull String getFileName() {
        return cbScriptFile.getText();
    }
}
