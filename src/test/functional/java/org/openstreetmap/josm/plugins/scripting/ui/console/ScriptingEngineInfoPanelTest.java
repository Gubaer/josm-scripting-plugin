package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link ScriptEngineInfoPanel}
 */
public class ScriptingEngineInfoPanelTest extends JFrame {

    private ScriptingEngineInfoPanelTest() throws Exception {
        @SuppressWarnings("unused")
        // final JOSMFixture fixture = new JOSMFixture(true);
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
        build();
    }

    private void build() {
        final ScriptEngineInfoPanel infoPanel;
        final ScriptEditorModel model;
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        model = new ScriptEditorModel();
        infoPanel = new ScriptEngineInfoPanel(model);
        c.add(infoPanel, BorderLayout.CENTER);
        setSize(600, 50);
    }

    static public void main(String[] args) throws Exception {
        ScriptingEngineInfoPanelTest app = new ScriptingEngineInfoPanelTest();
        app.setVisible(true);
    }
}
