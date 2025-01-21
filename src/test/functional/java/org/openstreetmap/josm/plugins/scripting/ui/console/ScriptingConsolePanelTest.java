package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link ScriptingConsolePanel}
 */
public class ScriptingConsolePanelTest extends JFrame {

    @SuppressWarnings({"unused", "UnusedAssignment"})
    private void build() {
        final ScriptingConsolePanel console;
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(console = new ScriptingConsolePanel(getRootPane()), BorderLayout.CENTER);
        setSize(600,800);
    }

    private ScriptingConsolePanelTest() throws Exception {
        @SuppressWarnings("unused")
        // final JOSMFixture fixture = new JOSMFixture(true);
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
        build();
    }

    static public void main(String[] args) throws Exception {
        ScriptingConsolePanelTest app = new ScriptingConsolePanelTest();
        app.setVisible(true);
    }
}
