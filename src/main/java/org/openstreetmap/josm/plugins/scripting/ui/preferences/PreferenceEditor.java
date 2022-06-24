package org.openstreetmap.josm.plugins.scripting.ui.preferences;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane.PreferencePanel;
import org.openstreetmap.josm.plugins.scripting.ui.preferences.graalvm.GraalVMConfigurationPanel;
import org.openstreetmap.josm.plugins.scripting.ui.preferences.rhino.RhinoEngineConfigurationPanel;
import org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

public class PreferenceEditor extends DefaultTabPreferenceSetting {
    static public final String ICON_NAME = "script-engine";
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(PreferenceEditor.class.getName());

    private ScriptEnginesConfigurationPanel pnlScriptEngineConfiguration;
    private GraalVMConfigurationPanel pnlGraalVMConfiguration;
    private RhinoEngineConfigurationPanel pnlRhinoEngineConfiguration;

    public PreferenceEditor(){
        super(
                ICON_NAME, // icon name
                tr("Scripting"), // title
                tr("Configure script engines and scripts"), // description
                false // expert mode only?
        );
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel pnl = new JPanel(new BorderLayout());
        JTabbedPane tpPreferenceTabs = new JTabbedPane();
        tpPreferenceTabs.add(tr("Script engines"),
             pnlScriptEngineConfiguration =
             new ScriptEnginesConfigurationPanel());
        tpPreferenceTabs.add(tr("Rhino engine (deprecated)"),
                pnlRhinoEngineConfiguration =
                        new RhinoEngineConfigurationPanel());
        tpPreferenceTabs.add(tr("GraalVM"),
                pnlGraalVMConfiguration =
                        new GraalVMConfigurationPanel());
        pnl.add(tpPreferenceTabs, BorderLayout.CENTER);

        PreferencePanel pp = gui.createPreferenceTab(this);
        pp.add(pnl, GridBagConstraintBuilder.gbc().cell(0, 2)
                .fillboth().weight(1.0,1.0).constraints());
    }

    @Override
    public boolean ok() {
        pnlScriptEngineConfiguration.persistToPreferences();
        pnlRhinoEngineConfiguration.persistToPreferences();
        pnlGraalVMConfiguration.persistToPreferences();
        return false;
    }
}
