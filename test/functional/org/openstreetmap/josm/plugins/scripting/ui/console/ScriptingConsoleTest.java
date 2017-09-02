package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;


public class ScriptingConsoleTest extends JFrame {

    private final JOSMFixture fixture;

    public ScriptingConsoleTest() {

        fixture = JOSMFixture.createUnitTestFixture();
        fixture.init();

        Container c = getContentPane();
        c.setLayout(new FlowLayout());
        JButton btn = new JButton();
        btn.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScriptingConsole.showScriptingConsole();
            }
        });
        btn.setText("Launch");
        c.add(btn);

        setSize(200,200);
    }

    static public void main(String args[]) {
        ScriptingConsoleTest app = new ScriptingConsoleTest();
        app.setVisible(true);
    }
}
