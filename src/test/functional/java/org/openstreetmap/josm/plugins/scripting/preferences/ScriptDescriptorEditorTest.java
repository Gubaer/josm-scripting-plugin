package org.openstreetmap.josm.plugins.scripting.preferences;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link ScriptDescriptorEditorDialog}
 */
public class ScriptDescriptorEditorTest extends JFrame {

    private ScriptDescriptorEditorTest() throws Exception {
        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(e -> {
            ScriptDescriptorEditorDialog dialog =
                new ScriptDescriptorEditorDialog
                    (ScriptDescriptorEditorTest.this
                );
            dialog.setVisible(true);
        });

        @SuppressWarnings("unused")
        final JOSMFixture fixture = new JOSMFixture(true);
    }

    static public void main(String[] args) throws Exception {
        ScriptDescriptorEditorTest app = new ScriptDescriptorEditorTest();
        app.setVisible(true);
    }
}
