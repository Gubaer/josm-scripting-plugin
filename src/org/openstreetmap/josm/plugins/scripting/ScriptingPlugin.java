package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class ScriptingPlugin extends Plugin {
	
	private static ScriptingPlugin instance;
	
	public static ScriptingPlugin getInstance() {
		return instance;
	
	}
	
	public ScriptingPlugin(PluginInformation info) {
		super(info);
		instance = this;
		installScriptsMenu();
	}
	
	protected void installScriptsMenu(){
		JMenu mnuMacro;
		Main.main.menu.add(mnuMacro = new JMenu(tr("Scripts")));
		mnuMacro.setMnemonic('S');
		mnuMacro.add(new RunScriptAction());		
	}
}
