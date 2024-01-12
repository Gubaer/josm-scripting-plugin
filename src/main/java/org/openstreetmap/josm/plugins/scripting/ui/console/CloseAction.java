package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.tools.ImageProvider;

public class CloseAction extends AbstractAction {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(CloseAction.class.getName());

    public CloseAction() {
        putValue(NAME, tr("Close"));
        putValue(SHORT_DESCRIPTION, tr("Close the scripting console"));
        putValue(SMALL_ICON, ImageProvider.get("exit", ImageProvider.ImageSizes.MENU));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ScriptingConsole.hideScriptingConsole();
    }
}
