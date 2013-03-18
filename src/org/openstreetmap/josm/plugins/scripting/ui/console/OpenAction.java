package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.tools.ImageProvider;

public class OpenAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	static private final Logger logger = Logger.getLogger(OpenAction.class.getName());
	static public final StringProperty PREF_LAST_OPEN_DIR = new StringProperty(
			OpenAction.class.getName() + ".lastDir", 
			null
	 );
	
	protected File getLastOpenedDirectory() {
		String dir = PREF_LAST_OPEN_DIR.get();
		if (dir == null) return null;
		File f = new File(dir);
		if (! f.isDirectory() && f.canRead()) return null;
		return f;
	}
	
	protected File askFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(tr("Select a script"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);	
		chooser.setFileHidingEnabled(false);
		File dir = getLastOpenedDirectory();
		if (dir != null) {
			chooser.setCurrentDirectory(dir);
		}
		int ret = chooser.showOpenDialog(ScriptingConsole.getInstance());			
		if (ret != JFileChooser.APPROVE_OPTION) return null;		
		dir = chooser.getCurrentDirectory();
		if (dir != null){
			PREF_LAST_OPEN_DIR.put(dir.getAbsolutePath());
		}
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
		ScriptingConsole.getInstance().open(f);
	}
}
