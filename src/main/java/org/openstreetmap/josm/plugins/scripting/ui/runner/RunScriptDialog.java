package org.openstreetmap.josm.plugins.scripting.ui.runner;

import org.openstreetmap.josm.actions.ActionParameter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.ParameterizedAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.ToolbarPreferences;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.RunScriptService;
import org.openstreetmap.josm.plugins.scripting.ui.widgets.ScriptEngineInfoPanel;
import org.openstreetmap.josm.plugins.scripting.ui.widgets.SelectContextPanel;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Utils;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Provides a modal dialog for selecting and running a script.
 */
public class RunScriptDialog extends JDialog implements PreferenceKeys {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(RunScriptDialog.class.getName());

    static private final RunScriptDialog instance =
        new RunScriptDialog(MainApplication.getMainFrame());

    /**
     * Replies the unique instance of the dialog
     *
     * @return the unique instance
     */
    static public @NotNull RunScriptDialog getInstance() {
        return instance;
    }

    /**
     * the input field for the script file name
     */
    //private MostRecentlyRunScriptsComboBox cbScriptFile;
    private ScriptFileInputPanel pnlScriptFileInput;

    private Action actRun;
    private JCheckBox addOnToolbar;

    private final RunScriptDialogModel model;

    /**
     * Replies the dialog model.
     *
     * @return the model
     */
    public @NotNull RunScriptDialogModel getModel() {
        return model;
    }

    /**
     * Constructor
     *
     * @param parent the dialog owner
     */
    public RunScriptDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent),
                ModalityType.DOCUMENT_MODAL);
        model = new RunScriptDialogModel();
        build();
        HelpUtil.setHelpContext(this.getRootPane(),
                HelpUtil.ht("/Plugin/Scripting"));
    }

    private JPanel buildInfoPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        HtmlPanel info = new HtmlPanel();
        info.setText(
              "<html>"
            + tr("Select a script file and click on <strong>Run</strong>.")
            + "</html>"
        );
        pnl.add(info, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildControlButtonPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btn;

        actRun = new RunAction();
        btn = new JButton(actRun);
        pnl.add(btn);
        btn.setFocusable(true);
        btn.registerKeyboardAction(actRun, KeyStroke.getKeyStroke("ENTER"),
                JComponent.WHEN_FOCUSED);
        pnl.add(new JButton(new CancelAction()));
        final var helpAction =  new ContextSensitiveHelpAction(
            HelpUtil.ht("/Plugin/Scripting#Run"));
        pnl.add(new JButton(helpAction));
        return pnl;
    }

    private JPanel buildMainPanel() {
        // the widget for editing a script file name
        pnlScriptFileInput = new ScriptFileInputPanel();

        // the panel with info about the current script engine
        final var enginePanel = new ScriptEngineInfoPanel(model);
        enginePanel.setBorder(
            BorderFactory.createTitledBorder(tr("Select script engine"))
        );

        // a panel with a checkbox for whether the script should
        // be added to the toolbar
        final var toolbarPnl = new JPanel(new FlowLayout(FlowLayout.LEADING));
        addOnToolbar = new JCheckBox(tr("Add toolbar button"), false);
        addOnToolbar.setToolTipText(tr("Add a button for this script file to the toolbar."));
        toolbarPnl.add(addOnToolbar);

        final var pnlSelectContext = new SelectContextPanel();
        pnlSelectContext.setBorder(
            BorderFactory.createTitledBorder(tr("Select scripting context"))
        );

        getModel().addPropertyChangeListener(pnlSelectContext);

        // assemble the main panel
        final var pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.add(pnlScriptFileInput);
        pnlMain.add(enginePanel);
        pnlMain.add(pnlSelectContext);
        pnlMain.add(toolbarPnl);

        return pnlMain;
    }

    private JPanel buildContentPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(buildInfoPanel(), BorderLayout.NORTH);
        pnl.add(buildMainPanel(), BorderLayout.CENTER);
        pnl.add(buildControlButtonPanel(), BorderLayout.SOUTH);
        return pnl;
    }

    protected void build() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);

        getRootPane().registerKeyboardAction(actRun,
            KeyStroke.getKeyStroke("ctrl ENTER"),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        setTitle(tr("Run a script"));
        setSize(600, 180);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                pnlScriptFileInput.requestFocusInWindow();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            final var lastFile = Preferences.main().get(PREF_KEY_LAST_FILE);
            if (lastFile != null && !lastFile.trim().isEmpty()) {
                pnlScriptFileInput.setFileName(lastFile.trim());
            }
            WindowGeometry.centerInWindow(getParent(), new Dimension(600, 400))
                .applySafe(this);
        } else {
            /*
             * Persist the file history script file name
             * in the preferences
             */
            final var currentFile = pnlScriptFileInput.getFileName();
            Preferences.main().put(PREF_KEY_LAST_FILE, currentFile.trim());
        }
        super.setVisible(visible);
    }

    private class CancelAction extends AbstractAction {
        CancelAction() {
            putValue(NAME, tr("Cancel"));
            putValue(SHORT_DESCRIPTION, tr("Cancel"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            setVisible(false);
        }
    }

    public class RunAction extends JosmAction implements ParameterizedAction {
        private static final String SCRIPTING_FILENAME = "scriptingFilename";

        RunAction() {
            super(
                tr("Run"),
                "run",
                tr("Run the script"),
                null, // no shortcut
                true, // do register in toolbar
                "scripting/run", // the toolbar id
                true // do install adapters
            );
        }

        @Override
        public List<ActionParameter<?>> getActionParameters() {
            return Collections.singletonList(
                new ActionParameter.StringActionParameter(SCRIPTING_FILENAME));
        }

        @Override
        public void actionPerformed(ActionEvent evt, Map<String, Object> parameters) {
            if (parameters.containsKey(SCRIPTING_FILENAME)) {
                doRun((String) parameters.get(SCRIPTING_FILENAME), false);
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            doRun(pnlScriptFileInput.getFileName().trim(), addOnToolbar.isSelected());
        }

        private void doRun(String fileName, boolean addToToolbar) {
            RunScriptService service = new RunScriptService();
            if (!service.canRunScript(fileName, RunScriptDialog.this)) {
                return;
            }
            if (addToToolbar) {
                ToolbarPreferences.ActionDefinition aDef =
                        new ToolbarPreferences.ActionDefinition(this);
                aDef.getParameters().put(SCRIPTING_FILENAME, fileName);

                // Display filename as tooltip instead of generic one
                aDef.setName(tr("Run script ''{0}''",
                        Utils.shortenString(fileName, 100)));

                // parametrized action definition is now composed
                ToolbarPreferences.ActionParser actionParser =
                        new ToolbarPreferences.ActionParser(null);
                String res = actionParser.saveAction(aDef);

                // add custom scripting button to toolbar preferences
                MainApplication.getToolbar().addCustomButton(
                    res,
                    -1,   // at end of toolbar
                    false // don't remove if exists
                );
            }
            ScriptEngineDescriptor engine =
                service.deriveOrAskScriptEngineDescriptor(
                    fileName, RunScriptDialog.this
                );
            if (engine == null) return;
            setVisible(false);
            service.runScript(fileName, engine, RunScriptDialog.this);
        }
    }
}
