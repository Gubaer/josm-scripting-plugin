package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.tools.Shortcut;

public class RunScriptAction extends JosmAction {
    private static final long serialVersionUID = 1L;

    public RunScriptAction() {
        super(tr("Run..."), // title
            "run", // icon name
            tr("Run a script"), // tooltip
            Shortcut.registerShortcut("scripting:runScript",
                    tr("Scripting: Run a Script"), KeyEvent.VK_R,
                    Shortcut.NONE // don't assign an action group, let the
                                  // the user assign in the preferences
            ), false, // don't register toolbar item
            "scripting:toggleConsole", false // don't install adapters
        );
        putValue("help", HelpUtil.ht("/Plugin/Scripting"));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        RunScriptDialog dialog = new RunScriptDialog(Main.parent);
        dialog.setVisible(true);
    }
}
