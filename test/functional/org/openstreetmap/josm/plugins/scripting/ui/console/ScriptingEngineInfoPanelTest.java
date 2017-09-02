package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

public class ScriptingEngineInfoPanelTest extends JFrame {

    private ScriptEngineInfoPanel infoPanel;
    private ScriptEditorModel model;
    private JOSMFixture fixture;

    public ScriptingEngineInfoPanelTest() throws Exception {
        fixture = new JOSMFixture();
        build();
    }

    protected void build() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        model = new ScriptEditorModel();
        infoPanel = new ScriptEngineInfoPanel(model);
        c.add(infoPanel, BorderLayout.CENTER);
        setSize(600, 50);
    }

    static public void main(String args[]) throws Exception {
        ScriptingEngineInfoPanelTest app = new ScriptingEngineInfoPanelTest();
        app.setVisible(true);
    }
}
