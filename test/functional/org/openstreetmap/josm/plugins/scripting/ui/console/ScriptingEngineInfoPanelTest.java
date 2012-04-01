package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

public class ScriptingEngineInfoPanelTest extends JFrame {

	private ScriptingEningeInfoPanel infoPanel;
	private ScriptEditorModel model;
	private JOSMFixture fixture;
	
	public ScriptingEngineInfoPanelTest() {

		fixture = JOSMFixture.createUnitTestFixture();
		fixture.init();	
		
		build(); 
	}
	
	protected void build() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		model = new ScriptEditorModel();
		infoPanel = new ScriptingEningeInfoPanel(model);
		c.add(infoPanel, BorderLayout.CENTER);		
		setSize(600, 50);
	}
	
	static public void main(String args[]) {
		ScriptingEngineInfoPanelTest app = new ScriptingEngineInfoPanelTest();
		app.setVisible(true);
	}
}
