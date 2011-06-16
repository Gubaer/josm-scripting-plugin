package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.openstreetmap.josm.tools.ImageProvider;

public class OpenAction extends AbstractAction {
	static private final Logger logger = Logger.getLogger(OpenAction.class.getName());
	
	protected File askFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(tr("Select a script"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		int ret = chooser.showOpenDialog(ScriptingConsole.getScriptingConsole());			
		if (ret != JFileChooser.APPROVE_OPTION) return null;
		
		return chooser.getSelectedFile();
	}
	
	public OpenAction() {
		putValue(NAME, tr("Open"));
		putValue(SHORT_DESCRIPTION, tr("Open a script file"));
		putValue(SMALL_ICON, ImageProvider.get("open"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File f = askFile();
		if (f == null) return;
		ScriptingConsole.getScriptingConsole().open(f);
	}
}
