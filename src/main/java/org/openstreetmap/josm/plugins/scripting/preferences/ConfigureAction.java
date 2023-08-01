package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;

public class ConfigureAction extends JosmAction {

    private PreferenceDialog dialog;

    public ConfigureAction() {
        super(
            tr("Configure..."),                        // title
            "scripting-preference",                 // icon name
            tr("Configure scripting preferences"),  // tooltip
            null,                                   // no shortcut
            false                                   // don't register
        );
        putValue("help", HelpUtil.ht("/Plugin/Scripting"));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        dialog = new PreferenceDialog(MainApplication.getMainFrame());
        dialog.addComponentListener(visibilityListener);
        dialog.setVisible(true);
    }

    private final ComponentListener visibilityListener =
            new  ComponentListener() {
        @Override
        public void componentHidden(ComponentEvent arg0) {
            // cleanup when dialog becomes invisible
            dialog.removeComponentListener(this);
            dialog = null;
        }
        @Override
        public void componentShown(ComponentEvent arg0) {
            // programatically select scripting preferences tab when
            // dialog becomes visible
            dialog.selectPreferencesTabByName(PreferenceEditor.ICON_NAME);
        }

        @Override
        public void componentMoved(ComponentEvent arg0) {/* ignore */}
        @Override
        public void componentResized(ComponentEvent arg0) {/* ignore */}
    };
}
