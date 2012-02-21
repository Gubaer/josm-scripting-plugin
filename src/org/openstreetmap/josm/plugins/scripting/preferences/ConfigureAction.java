package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;

public class ConfigureAction extends JosmAction {
	public ConfigureAction() {
		super(
			tr("Configure..."),        			    // title
			"scripting-preference", 			    // icon name
			tr("Configure scripting preferences"),  // tooltip 
			null,                                   // no shortcut 
			false                                   // don't register
		);		
		putValue("help", HelpUtil.ht("/Plugin/Scripting"));
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		PreferenceDialog dialog = new PreferenceDialog(Main.parent);
		dialog.selectPreferencesTabByName(PreferenceEditor.ICON_NAME);	
		dialog.setVisible(true);
	}		
}
