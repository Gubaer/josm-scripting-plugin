package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * functional, interactive test for {@link ScriptingConsole}
 */
public class ScriptingConsoleTest extends JFrame {

    private ScriptingConsoleTest() throws Exception {

        @SuppressWarnings("unused")
        // final JOSMFixture fixture = new JOSMFixture(true);
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);

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

    static public void main(String[] args) throws Exception {
        ScriptingConsoleTest app = new ScriptingConsoleTest();
        app.setVisible(true);
    }
}
