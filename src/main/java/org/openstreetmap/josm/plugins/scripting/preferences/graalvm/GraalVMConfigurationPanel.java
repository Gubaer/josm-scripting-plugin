package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMConfigurationPanel extends JPanel {
    private static final Logger logger =
            Logger.getLogger(GraalVMConfigurationPanel.class.getName());

    private JTabbedPane tpPreferencesTabs;
    private CommonJSRepoConfigurationPanel pnlCommonJSRepoConfiguration;

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
            + tr(
                   "<p>A copy of the GraalVM for JavaScript is included in the plugin "
                 + "distribution and can be selected to execute a script written "
                 + "in JavaScript/ECMAScript."
                 + "</p>"
            )
            + "</html>";
        pane.setText(text);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected JPanel buildTabs() {
        JPanel pnl = new JPanel(new BorderLayout());
        tpPreferencesTabs = new JTabbedPane();
        tpPreferencesTabs.add(tr("CommonJS module repositories"),
                pnlCommonJSRepoConfiguration =
                        new CommonJSRepoConfigurationPanel());
        pnl.add(tpPreferencesTabs, BorderLayout.CENTER);
        return pnl;
    }

    protected void build() {
        setLayout(new BorderLayout());
        add(buildInfoPanel(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    public GraalVMConfigurationPanel() {
        build();
    }

    public void persistToPreferences() {
        pnlCommonJSRepoConfiguration.persistToPreferences();
    }
}
