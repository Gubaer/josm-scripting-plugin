package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole;
import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole.ScriptingConsoleListener;
import org.openstreetmap.josm.tools.Shortcut;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

@SuppressWarnings("serial")
public class ToggleConsoleAction extends JosmAction
    implements ScriptingConsoleListener {

    @SuppressWarnings("unused")
    static private final Logger logger =
        Logger.getLogger(ToggleConsoleAction.class.getName());

    public ToggleConsoleAction(){
        super(
            tr("Show scripting console"),   // title
            (String)null,                   // icon name
            tr("Show/hide the scripting console"),  // tooltip
            Shortcut.registerShortcut(
                    "scripting:toggleConsole",
                    tr("Scripting: Show/hide scripting console"),
                    KeyEvent.VK_T,
                    Shortcut.NONE
            ),
            false,               // don't register toolbar item
            "scripting:toggleConsole",
            false                // don't install adapters
        );
        putValue(SELECTED_KEY, ScriptingConsole.getInstance() != null);
        ScriptingConsole.addScriptingConsoleListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScriptingConsole.toggleScriptingConsole();
    }

    @Override
    public void scriptingConsoleChanged(ScriptingConsole oldValue,
            ScriptingConsole newValue) {
        putValue(SELECTED_KEY, newValue != null);
    }
}
