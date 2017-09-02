package org.openstreetmap.josm.plugins.scripting.preferences;
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;


public class ScriptEnginesConfigurationPanelTest extends JFrame {

    private JOSMFixture fixture;

    public ScriptEnginesConfigurationPanelTest() throws Exception {
        fixture = new JOSMFixture();
        build();
    }

    protected void build() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(new ScriptEnginesConfigurationPanel(), BorderLayout.CENTER);
        setSize(400, 600);
    }

    static public void main(String args[]) throws Exception {
        ScriptEnginesConfigurationPanelTest app = new ScriptEnginesConfigurationPanelTest();
        app.setVisible(true);
    }
}
