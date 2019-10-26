package org.openstreetmap.josm.plugins.scripting.preferences;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link SysPathsEditorPanel}
 */
public class SysPathsEditorPanelTest extends JFrame {

    private void build() {
        setSize(600,400);
        getContentPane().setLayout(new BorderLayout());

        SysPathsEditorPanel pnl = new SysPathsEditorPanel();
        getContentPane().add(pnl, BorderLayout.CENTER);
    }

    private SysPathsEditorPanelTest() throws Exception {
        @SuppressWarnings("unused")
        final JOSMFixture fixture = new JOSMFixture(true);
        build();
    }

    static public void main(String[] args) throws Exception {
        SysPathsEditorPanelTest app = new SysPathsEditorPanelTest();
        app.setVisible(true);
    }
}
