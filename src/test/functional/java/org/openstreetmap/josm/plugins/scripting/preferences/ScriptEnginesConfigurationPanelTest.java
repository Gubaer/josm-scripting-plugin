package org.openstreetmap.josm.plugins.scripting.preferences;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link ScriptEnginesConfigurationPanel}
 */
public class ScriptEnginesConfigurationPanelTest extends JFrame {

    private ScriptEnginesConfigurationPanelTest() throws Exception {
        @SuppressWarnings("unused")
        // final JOSMFixture fixture = new JOSMFixture(true);
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
        build();
    }

    private void build() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(new ScriptEnginesConfigurationPanel(), BorderLayout.CENTER);
        setSize(400, 600);
    }

    static public void main(String[] args) throws Exception {
        ScriptEnginesConfigurationPanelTest app =
            new ScriptEnginesConfigurationPanelTest();
        app.setVisible(true);
    }
}
