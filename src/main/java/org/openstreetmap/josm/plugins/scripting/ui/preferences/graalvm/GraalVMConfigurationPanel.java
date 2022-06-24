package org.openstreetmap.josm.plugins.scripting.ui.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The preferences configuration panel for the GraalVM. Supports interactive
 * configuration of a list of CommonJS module repositories.
 */
public class GraalVMConfigurationPanel extends JPanel implements HyperlinkListener {
    @SuppressWarnings("unused")
    private static final Logger logger =
            Logger.getLogger(GraalVMConfigurationPanel.class.getName());

    private CommonJSRepoConfigurationPanel pnlCommonJSRepoConfiguration;
    private ESModuleRepoConfigurationPanel pnlESModuleRepoConfiguration;

    static private final String MESSAGE_01 = tr(
          "The scripting plugin can run JavaScript scripts using "
        + "<a href=\"https://github.com/oracle/graaljs\">GraalJS</a>, "
        + "the JavaScript engine provided by the <a href=\"https://www.graalvm.org/\">GraalVM</a>."
        + "To be available in the scripting plugin, GraalJS has "
        + "to be on the classpath when JOSM starts. ");

    static private final String MESSAGE_02 = tr(
        "JOSM has been started with GraalJS on the classpath. You can use GraalJS "
        + "in the scripting plugin.");

    static private final String MESSAGE_03 = tr(
            "JOSM has <strong>not</strong> been started with GraalJS on the classpath. "
        + "<a href=\"{0}\">This documentation</a> "
        + "explains how you can start it with GraalJS on the classpath.",
        "https://gubaer.github.io/josm-scripting-plugin/docs/graaljs.html");

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        pane.addHyperlinkListener(this);

        String message = String.format("<p>%s</p>", MESSAGE_01);
        if (GraalVMFacadeFactory.isGraalVMPresent()) {
            message += String.format("<p>%s</p>", MESSAGE_02);
        } else {
            message += String.format("<p>%s</p>", MESSAGE_03);
        }

        message = String.format("<html>%s</html>", message);
        pane.setText(message);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected JPanel buildTabs() {
        JPanel pnl = new JPanel(new BorderLayout());
        JTabbedPane tpPreferencesTabs = new JTabbedPane();
        tpPreferencesTabs.add(tr("ES Module repositories"),
            pnlESModuleRepoConfiguration = new ESModuleRepoConfigurationPanel());
        tpPreferencesTabs.add(tr("CommonJS module repositories"),
            pnlCommonJSRepoConfiguration = new CommonJSRepoConfigurationPanel());
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

    /**
     * Persist preferences to the JOSM preferences file.
     */
    public void persistToPreferences() {
        pnlCommonJSRepoConfiguration.persistToPreferences();
        pnlESModuleRepoConfiguration.persistToPreferences();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (!HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())) {
            // hovering, etc.
            return;
        }
        if (Desktop.isDesktopSupported()) {
            var desktop = Desktop.getDesktop();
            try {
                desktop.browse(evt.getURL().toURI());
            } catch(URISyntaxException | IOException e) {
                logger.log(Level.WARNING, String.format(
                    "failed to open default browser with URL '%s'", evt.getURL()
                ), e);
            }
        } else {
            logger.warning("java.awt.Desktop isn't available. Can't open default system browser.");
        }
    }
}
