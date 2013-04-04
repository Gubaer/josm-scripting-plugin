package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane.PreferencePanel;
import org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder;

public class PreferenceEditor extends DefaultTabPreferenceSetting {
	static public final String ICON_NAME = "script-engine";
	//static private final Logger logger = Logger.getLogger(PreferenceEditor.class.getName());
	
	private JTabbedPane tpPreferenceTabs;
	private ScriptEnginesConfigurationPanel pnlScriptEngineConfiguration;
	private RhinoEngineConfigurationPanel pnlRhinoEngineConfiguration;
	private JythonConfigurationPanel pnlJythonConfiguration;
	
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
		tpPreferenceTabs.add(tr("Embedded Rhino engine"), pnlRhinoEngineConfiguration = new RhinoEngineConfigurationPanel());
		tpPreferenceTabs.add(tr("Jython engine"), pnlJythonConfiguration = new JythonConfigurationPanel());		
		pnl.add(tpPreferenceTabs, BorderLayout.CENTER);
		
		pnlJythonConfiguration.loadFromPreferences();
		
		PreferencePanel pp = gui.createPreferenceTab(this);
		pp.add(pnl, GridBagConstraintBuilder.gbc().cell(0, 2).fillboth().weight(1.0,1.0).constraints());
	}

	@Override
	public boolean ok() {
		pnlScriptEngineConfiguration.persistToPreferences();
		pnlRhinoEngineConfiguration.persistToPreferences();
		pnlJythonConfiguration.persistToPreferences();
		return false;
	}
}
