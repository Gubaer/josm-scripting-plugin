package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class PreferenceEditor extends JPanel implements PreferenceSetting {
	
	private JTabbedPane tpPreferenceTabs;
	private ScriptEnginesConfigurationPanel pnlScriptEngineConfiguration;
	
	public PreferenceEditor(){
		build();
	}
	
	protected void build() {
		setLayout(new BorderLayout());
		
		tpPreferenceTabs = new JTabbedPane();
		tpPreferenceTabs.add(tr("Script engines"), pnlScriptEngineConfiguration = new ScriptEnginesConfigurationPanel());
		add(tpPreferenceTabs, BorderLayout.CENTER);
	}

	@Override
	public void addGui(PreferenceTabbedPane gui) {
        String description = tr("Configure script engines and scripts");
        JPanel tab = gui.createPreferenceTab("script-engine", tr("Scripting"), description);        
        tab.add(this, GBC.eol().fill(GBC.BOTH));
        this.setName("scripting.preferences.editor");
	}

	@Override
	public boolean ok() {
		pnlScriptEngineConfiguration.persistToPreferences();
		return false;
	}
}
