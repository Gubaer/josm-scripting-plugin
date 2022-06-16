package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;

import javax.swing.*;
import java.awt.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class CommonJSRepoConfigurationPanel extends AbstractRepoConfigurationPanel{

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
                + tr(
            "<p>"
                    + "The embedded GraalVM can load <strong>CommonJS modules</strong> "
                    + "with the function <code>require()</code>. It resolves CommonJS modules "
                    + "in the directories or jar files configured below."
                    + "</p>"
            )
            + "</html>";
        pane.setText(text);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }
}
