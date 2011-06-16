package org.openstreetmap.josm.plugins.scripting.preferences;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;


public class ScriptDescriptorEditorTest extends JFrame {
	
	private JOSMFixture fixture;
	
	public ScriptDescriptorEditorTest() {
		getContentPane().setLayout(new FlowLayout());
		setSize(100,100);
		JButton btn = new JButton("Launch");
		getContentPane().add(btn);
		btn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptDescriptorEditorDialog dialog = new ScriptDescriptorEditorDialog(ScriptDescriptorEditorTest.this);
				dialog.setVisible(true);
			}
		});
		
		fixture = JOSMFixture.createUnitTestFixture();
		fixture.init();				
	}
		
	static public void main(String args[]) {
		ScriptDescriptorEditorTest app = new ScriptDescriptorEditorTest();
		app.setVisible(true);
	}
}
