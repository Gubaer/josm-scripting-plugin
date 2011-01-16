package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.preferences.ConfigureAction;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceEditor;

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
		mnuMacro = Main.main.menu.addMenu(tr("Scripting"), KeyEvent.VK_S, Main.main.menu.defaultMenuPos, ht("/Plugin/Scripting"));
		mnuMacro.setMnemonic('S');
		mnuMacro.add(new RunScriptAction());
		mnuMacro.add(new JSeparator());
		mnuMacro.add(new ConfigureAction());
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new PreferenceEditor();
	}
}
