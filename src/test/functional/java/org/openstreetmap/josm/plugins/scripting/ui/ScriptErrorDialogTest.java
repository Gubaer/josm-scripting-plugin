package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

public class ScriptErrorDialogTest extends JFrame {
    private JOSMFixture fixture;

    public ScriptErrorDialogTest() throws Exception {
        fixture = new JOSMFixture(true);

        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(e -> {
            Throwable exception = new Exception();
            try {
                int a = 5 / 0;
            } catch(Throwable t) {
                exception = t;
            }
            ScriptErrorDialog.showErrorDialog(exception);
        });
    }

    static public void main(String[] args) throws Exception {
        final ScriptErrorDialogTest app = new ScriptErrorDialogTest();
        app.setVisible(true);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
