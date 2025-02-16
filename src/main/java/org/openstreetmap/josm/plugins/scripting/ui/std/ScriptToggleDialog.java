package org.openstreetmap.josm.plugins.scripting.ui.std;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import javax.swing.*;
import java.awt.*;

import static ch.poole.openinghoursparser.I18n.tr;

/**
 * The script toggle dialog is a dialog that allows the user to execute a script from a list of configured scripts.
 */
public class ScriptToggleDialog extends ToggleDialog {

    public ScriptToggleDialog() {
        super(tr("Script Toggle Dialog"), "script-engine", tr("Toggle script dialog"), null, 150);
        buildContent();
    }

    private void buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("This is the Script Toggle Dialog"), BorderLayout.CENTER);
        createLayout(panel, false, null);
    }
}