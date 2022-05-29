package org.openstreetmap.josm.plugins.scripting;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineSelectionDialog;

import javax.swing.*;
import java.awt.*;

/**
 * functional, interactive test for {@link ScriptEngineSelectionDialog}
 */
public class ScriptEngineSelectionDialogTest extends JFrame {

    ScriptEngineSelectionDialogTest() throws Exception {
        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(e -> {
            ScriptEngineDescriptor desc =
                ScriptEngineSelectionDialog.select(
                    ScriptEngineSelectionDialogTest.this,null);
            System.out.println(desc);
        });

        @SuppressWarnings("unused")
        final JOSMFixture fixture = JOSMFixture.createFixture(true /* with gui */);
    }

    static public void main(String[] args) throws Exception {
        ScriptEngineSelectionDialogTest app =
            new ScriptEngineSelectionDialogTest();
        app.setVisible(true);
    }
}
