package org.openstreetmap.josm.plugins.scripting.preferences;
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;


public class RhinoEngineConfigurationPanelTest extends JFrame {
	
	private JOSMFixture fixture;
	
	public RhinoEngineConfigurationPanelTest() {
		fixture = JOSMFixture.createUnitTestFixture();
		fixture.init();				
		build();
	}
	
	protected void build() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(new RhinoEngineConfigurationPanel(), BorderLayout.CENTER);
		setSize(400, 600);
	}
		
	static public void main(String args[]) {
		RhinoEngineConfigurationPanelTest app = new RhinoEngineConfigurationPanelTest();
		app.setVisible(true);
	}
}
