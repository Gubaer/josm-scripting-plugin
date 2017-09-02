package org.openstreetmap.josm.plugins.scripting.preferences;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

public class SysPathsEditorPanelTest extends JFrame {

    private JOSMFixture fixture;

    protected void build() {
        setSize(600,400);
        getContentPane().setLayout(new BorderLayout());

        SysPathsEditorPanel pnl = new SysPathsEditorPanel();
        getContentPane().add(pnl, BorderLayout.CENTER);
    }

    public SysPathsEditorPanelTest() throws Exception {
        fixture = new JOSMFixture();
        build();
    }


    static public void main(String args[]) throws Exception {
        SysPathsEditorPanelTest app = new SysPathsEditorPanelTest();
        app.setVisible(true);
    }


}
