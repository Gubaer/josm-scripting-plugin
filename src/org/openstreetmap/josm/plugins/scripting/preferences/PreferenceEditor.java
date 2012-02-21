package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

public class PreferenceEditor extends DefaultTabPreferenceSetting {
	static public final String ICON_NAME = "script-engine";
	//static private final Logger logger = Logger.getLogger(PreferenceEditor.class.getName());
	
	private JTabbedPane tpPreferenceTabs;
	private ScriptEnginesConfigurationPanel pnlScriptEngineConfiguration;
	
	public PreferenceEditor(){
		super(
				ICON_NAME, // icon name
				tr("Scripting"), // title
				tr("Configure script engines and scripts"), // description
				false // expert mode only?
		);		
	}

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		JPanel pnl = new JPanel(new BorderLayout());
		tpPreferenceTabs = new JTabbedPane();
		tpPreferenceTabs.add(tr("Script engines"), pnlScriptEngineConfiguration = new ScriptEnginesConfigurationPanel());
		pnl.add(tpPreferenceTabs, BorderLayout.CENTER);
		createPreferenceTabWithScrollPane(gui, pnl);
	}

	@Override
	public boolean ok() {
		pnlScriptEngineConfiguration.persistToPreferences();
		return false;
	}
}
