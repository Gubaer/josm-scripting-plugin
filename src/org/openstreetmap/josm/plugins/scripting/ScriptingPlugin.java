package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.js.RhinoEngine;
import org.openstreetmap.josm.plugins.scripting.preferences.ConfigureAction;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceEditor;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptAction;
import org.openstreetmap.josm.plugins.scripting.ui.ToggleConsoleAction;

public class ScriptingPlugin extends Plugin {
	static private final Logger logger = Logger.getLogger(ScriptingPlugin.class.getName());
	
	private static ScriptingPlugin instance;
	private static Scriptable startModule;
	
	protected void initLocalInstallation() {
		File f = new File(getPluginDir(), "modules");
		if (! f.exists()) {
			if (! f.mkdirs()) {
				logger.warning(String.format("Failed to create directory '%s'", f.toString()));
				return;
			}
		}
	}
	
	public static ScriptingPlugin getInstance() {
		return instance;	
	}
	
	public ScriptingPlugin(PluginInformation info) {
		super(info);
		instance = this;
		installScriptsMenu();	
		initLocalInstallation();
		
		RhinoEngine engine = RhinoEngine.getInstance();
		engine.initScope();
		try {
			startModule = engine.require("start");
		} catch(RhinoException e) {
			JOSMModuleScriptProvider provider = JOSMModuleScriptProvider.getInstance();
			URL url = provider.lookup("start");
			logger.log(Level.SEVERE, String.format("Failed to load start module '%s' from URL '%s'.", "start", url), e);
		}
		if (startModule != null) {
			logger.info(String.format("Successfully loaded startup module '%s'", "start"));
			jsOnStart();
		}				
	}
	
	protected void jsOnStart() {
		if (startModule == null) return;
		Object o = startModule.get("onStart", startModule);
		if (o == Scriptable.NOT_FOUND) return;
		if (! (o instanceof Function)) {
			logger.warning(String.format(
				"module 'start': property '%s' should be a function, got %s instead", "onStart", o
			));
			return;
		}
		RhinoEngine.getInstance().executeOnSwingEDT((Function)o);
	}
	
	protected void jsOnMapFrameChanged(MapFrame oldFrame, MapFrame newFrame) {
		if (startModule == null) return;
		Object o = startModule.get("onMapFrameChanged", startModule);
		if (o == Scriptable.NOT_FOUND) return;
		if (! (o instanceof Function)) {
			logger.warning(String.format(
					"module 'start': property '%s' should be a function, got %s instead", "onMapFrameChanged", o
				));
				return;
		}
		RhinoEngine.getInstance().executeOnSwingEDT((Function)o, new Object[]{oldFrame, newFrame});
	}
	
	protected void installScriptsMenu(){
		JMenu mnuMacro;
		mnuMacro = Main.main.menu.addMenu(tr("Scripting"), KeyEvent.VK_S, Main.main.menu.defaultMenuPos, ht("/Plugin/Scripting"));
		mnuMacro.setMnemonic('S');
		mnuMacro.add(new JCheckBoxMenuItem(new ToggleConsoleAction()));
		mnuMacro.add(new RunScriptAction());
		mnuMacro.add(new JSeparator());
		mnuMacro.add(new ConfigureAction());
	}

	@Override
	public PreferenceSetting getPreferenceSetting() {		
		return new PreferenceEditor();
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		jsOnMapFrameChanged(oldFrame, newFrame);
	}	
}
