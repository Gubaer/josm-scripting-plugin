package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;

public class ConfigureAction extends JosmAction {
	public ConfigureAction() {
		super(
			tr("Configure..."),        // title
			"scripting-preference", 			    // icon name
			tr("Configure scripting preferences"),  // tooltip 
			null,                // no shortcut 
			false                // don't register
		);		
		putValue("help", HelpUtil.ht("/Plugin/Scripting"));
	}
	
	protected Component getChildByName(Component parent, String name){
		if (parent == null) return null;
		if (name == null) return null;
		if (name.equals(parent.getName())) return parent;
		if (parent instanceof Container) {
			for (Component child: ((Container)parent).getComponents()) {
				Component found = getChildByName(child, name);
				if (found != null) return found;
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		PreferenceDialog dialog = new PreferenceDialog(Main.parent);
		dialog.selectPreferencesTabByName(PreferenceEditor.ICON_NAME);		
	}		
}
