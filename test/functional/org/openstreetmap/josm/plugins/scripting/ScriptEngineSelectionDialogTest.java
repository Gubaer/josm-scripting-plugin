package org.openstreetmap.josm.plugins.scripting;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineSelectionDialog;


public class ScriptEngineSelectionDialogTest extends JFrame {

    private JOSMFixture fixture;

    public ScriptEngineSelectionDialogTest() throws Exception {
        getContentPane().setLayout(new FlowLayout());
        setSize(100,100);
        JButton btn = new JButton("Launch");
        getContentPane().add(btn);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScriptEngineDescriptor desc = ScriptEngineSelectionDialog.select(ScriptEngineSelectionDialogTest.this,null);
                System.out.println(desc);
            }
        });

        fixture = new JOSMFixture();
    }

    static public void main(String args[]) throws Exception {
        ScriptEngineSelectionDialogTest app = new ScriptEngineSelectionDialogTest();
        app.setVisible(true);
    }
}
