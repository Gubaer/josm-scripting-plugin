package org.openstreetmap.josm.plugins.scripting.ui.preferences;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.ui.preferences.rhino.RhinoEngineConfigurationPanel;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link RhinoEngineConfigurationPanel}
 */
public class RhinoEngineConfigurationPanelTest extends JFrame {

    private RhinoEngineConfigurationPanelTest() throws Exception {
        @SuppressWarnings("unused")
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
        build();
    }

    private void build() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(new RhinoEngineConfigurationPanel(), BorderLayout.CENTER);
        setSize(400, 600);
    }

    static public void main(String[] args) throws Exception {
        RhinoEngineConfigurationPanelTest app =
            new RhinoEngineConfigurationPanelTest();
        app.setVisible(true);
    }
}
