package org.openstreetmap.josm.plugins.scripting.ui.preferences;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.ui.preferences.rhino.ModuleRepositoryDialog;

import javax.swing.*;
import java.awt.*;

public class ModuleRepositoryDialogTest extends JFrame {

    ModuleRepositoryDialogTest() throws Exception {
        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(e -> {
            ModuleRepositoryDialog dialog =
                new ModuleRepositoryDialog(ModuleRepositoryDialogTest.this);
            dialog.setVisible(true);
        });

        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
    }

    static public void main(String[] args) throws Exception {
        ModuleRepositoryDialogTest app = new ModuleRepositoryDialogTest();
        app.setVisible(true);
    }
}
