package org.openstreetmap.josm.plugins.scripting;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.tools.I18n.tr;

public class RunScriptAction extends JosmAction{
	public RunScriptAction() {
		super(
			tr("Run..."),        // title
			"run", 			     // icon name
			tr("Run a script"),  // tooltip 
			null,                // no shortcut 
			false                // don't register
		);		
		putValue(MNEMONIC_KEY, KeyEvent.VK_R);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		RunScriptDialog dialog = new RunScriptDialog(Main.parent);
		dialog.setVisible(true);		
	}		
}
