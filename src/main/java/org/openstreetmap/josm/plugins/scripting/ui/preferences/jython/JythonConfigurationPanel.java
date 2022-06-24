package org.openstreetmap.josm.plugins.scripting.ui.preferences.jython;

import static org.openstreetmap.josm.plugins.scripting.ui
    .GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;


import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;

/**
 * @deprecated Starting with plugin version 0.2.0 Jython plugins
 * are not supported anymore.
 */
@SuppressWarnings({"removal", "unused"})
@Deprecated(forRemoval = true, since = "0.2.0")
public class JythonConfigurationPanel extends VerticallyScrollablePanel{

    private static boolean isJythonPresent() {
        return false;
    }

    @SuppressWarnings("unused")
    private static final Logger logger =
        Logger.getLogger(JythonConfigurationPanel.class.getName());

    private SysPathsEditorPanel pnlSysPathEditor;
    private PythonPluginsConfigurationPanel pnlPythonPlugins;

    protected JPanel buildInfoPanel() {
        String text;
        if (isJythonPresent()) {
            text = "<html>" + tr(
                    "The scripting plugin can load and run plugins written in "
                    + "<strong>Python</strong>. "
                    + "Below you can configure where it looks for locally "
                    + "deployed plugins and "
                    + "which plugins it should load and launch at startup time."
                )
                + "</html>";
        } else {
            text = "<html>"
                + tr("<strong>Support for python plugins is disabled.</strong>"
                    +"<br>"
                    + "The scripting plugin can load and run plugins written "
                    + "in <strong>Python</strong>. "
                    + "In order to use this feature the Jython interpreter has "
                    + "to be on "
                    + "the class path when you start JOSM. Currently, it "
                    + "isn''t and "
                    + "python plugin support is therefore disabled.<br><br>"
                    + "Launch JOSM with the following command line: <br>"
                    + "java -cp \"josm-jar.jar:/path/to/jython.jar\" "
                    + "org.openstreetmap.josm.gui.MainApplication"
                )
                + "</html>";
        }

        HtmlPanel info = new HtmlPanel();
        info.setText(text);
        return info;
    }

    protected void build() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(3,3,3,3);
        pnlSysPathEditor = new SysPathsEditorPanel();
        pnlPythonPlugins = new PythonPluginsConfigurationPanel();
        add(buildInfoPanel(), gbc().cell(0,0).fillHorizontal()
                .weight(1.0, 0.0).insets(insets).constraints());
        add(pnlSysPathEditor,
                gbc().cell(0,1).fillboth().weight(1.0, 0.5)
                .insets(insets).constraints());
        add(pnlPythonPlugins,
                gbc().cell(0,2).fillboth().weight(1.0, 0.5)
                .insets(insets).constraints());
        if (!isJythonPresent()) {
            disableComponenteTree(pnlSysPathEditor);
            disableComponenteTree(pnlPythonPlugins);
        }
    }

    public JythonConfigurationPanel() {
        build();
    }

    protected void disableComponenteTree(Component root) {
        if (root == null) return;
        root.setEnabled(false);
        if (root instanceof JComponent) {
           JComponent c = (JComponent) root;
           if (c.getComponentPopupMenu() != null) {
               c.setComponentPopupMenu(null);
           }
        }
        if (root instanceof Container) {
            for (Component child: ((Container)root).getComponents()) {
                disableComponenteTree(child);
            }
        }
    }

    /**
     * Persist the current preferences values
     */
    public void persistToPreferences() {
        final Preferences pref = Preferences.main();
        pnlSysPathEditor.getModel().persistToPreferences(pref);
        pnlPythonPlugins.getModel().persistToPreferences(pref);
    }

    /**
     * Load the preferences values from the preferences file
     */
    public void loadFromPreferences() {
        final Preferences pref = Preferences.main();
        pnlSysPathEditor.getModel().loadFromPreferences(pref);
        pnlPythonPlugins.getModel().loadFromPreferences(pref);
    }
}
