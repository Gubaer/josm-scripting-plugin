package org.openstreetmap.josm.plugins.scripting.ui.release

import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent

class ReleaseNotesTest extends JFrame {
    private ReleaseNotesTest() throws Exception {

        @SuppressWarnings("unused")
        final fixture = JOSMFixture.createFixture(true /* with gui */)

        final c = getContentPane()
        c.setLayout(new FlowLayout())
        final btn = new JButton()
        btn.setAction(new AbstractAction() {
            @Override
            void actionPerformed(ActionEvent e) {
                final dialog = new ReleaseNotes(ReleaseNotesTest.this)
                dialog.setVisible(true)
            }
        })
        btn.setText("Launch")
        c.add(btn)

        setSize(200,200)
    }

    static void main(String[] args) throws Exception {
        final app = new ReleaseNotesTest()
        app.setVisible(true)
    }
}
