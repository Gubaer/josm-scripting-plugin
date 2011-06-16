package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.projection.Mercator;

public class ScriptingConsolePanelTest extends JFrame {
	
	private ScriptingConsolePanel console;
	
	protected void build() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(console = new ScriptingConsolePanel(), BorderLayout.CENTER);
		
		setSize(600,800);
	}	
	
	public ScriptingConsolePanelTest() {
		build();
	}
	
	static public void main(String args[]) {
		Main.pref = new Preferences();
		Main.setProjection(new Mercator());
		ScriptingConsolePanelTest app = new ScriptingConsolePanelTest();
		app.setVisible(true);
	}
}
