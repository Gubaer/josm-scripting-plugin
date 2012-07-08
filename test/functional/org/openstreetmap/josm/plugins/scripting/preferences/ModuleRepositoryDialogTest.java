package org.openstreetmap.josm.plugins.scripting.preferences;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptDialog;


public class ModuleRepositoryDialogTest extends JFrame {
	
	private JOSMFixture fixture;
	
	public ModuleRepositoryDialogTest() {
		getContentPane().setLayout(new FlowLayout());
		setSize(100,100);
		JButton btn = new JButton("Launch");
		getContentPane().add(btn);
		btn.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ModuleRepositoryDialog dialog = new ModuleRepositoryDialog(ModuleRepositoryDialogTest.this);
				dialog.setVisible(true);
			}
		});
		
		fixture = JOSMFixture.createUnitTestFixture();
		fixture.init();				
	}
		
	static public void main(String args[]) {
		ModuleRepositoryDialogTest app = new ModuleRepositoryDialogTest();
		app.setVisible(true);
	}
}
