package org.openstreetmap.josm.plugins.scripting;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Functional, interactive test for {@link RunScriptDialog}.
 */
public class RunScriptDialogTest extends JFrame {

    @SuppressWarnings("FieldCanBeLocal")
    private final JOSMFixture fixture;

    public RunScriptDialogTest() throws Exception {
        fixture = new JOSMFixture(true);

        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(e -> {
            RunScriptDialog dialog =
                new RunScriptDialog(RunScriptDialogTest.this);
            dialog.setVisible(true);
        });
    }

    static public void main(String[] args) throws Exception {
        RunScriptDialogTest app = new RunScriptDialogTest();
        app.setVisible(true);
    }
}
