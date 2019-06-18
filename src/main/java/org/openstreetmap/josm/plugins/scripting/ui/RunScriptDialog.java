package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.plugins.scripting.ui
        .GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.actions.ActionParameter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.ParameterizedAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.preferences.ToolbarPreferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.tools.Utils;

/**
 * <p>Provides a modal dialog for selecting and running a script.</p>
 */
@SuppressWarnings("serial")
public class RunScriptDialog extends JDialog implements PreferenceKeys {
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(RunScriptDialog.class.getName());

    /**
     * the input field for the script file name
     */
    private MostRecentlyRunScriptsComboBox cbScriptFile;
    private Action actRun;
    private JCheckBox addOnToolbar;

    /**
     * Constructor
     *
     * @param parent the dialog owner
     */
    public RunScriptDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent),
                ModalityType.DOCUMENT_MODAL);
        build();
        HelpUtil.setHelpContext(this.getRootPane(),
                HelpUtil.ht("/Plugin/Scripting"));
    }

    protected JPanel buildInfoPanel() {
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

    protected JPanel buildControlButtonPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btn;

        actRun = new RunAction();
        btn = new JButton(actRun);
        pnl.add(btn);
        btn.setFocusable(true);
        btn.registerKeyboardAction(actRun, KeyStroke.getKeyStroke("ENTER"),
                JComponent.WHEN_FOCUSED);
        pnl.add(new JButton(new CancelAction()));
        pnl.add(new JButton(new ContextSensitiveHelpAction(
                HelpUtil.ht("/Plugin/Scripting#Run"))));
        return pnl;
    }

    protected JPanel buildMacroFileInputPanel() {
        JPanel pnl = new JPanel();

        JPanel filePnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc().cell(0, 0).weight(0, 0).fillboth()
                .insets(3, 3, 3, 3).constraints();
        filePnl.add(new JLabel(tr("File:")), gc);

        cbScriptFile = new MostRecentlyRunScriptsComboBox(
                MostRecentlyRunScriptsModel.getInstance()
        );
        SelectAllOnFocusGainedDecorator.decorate((JTextField) cbScriptFile
                .getEditor().getEditorComponent());
        cbScriptFile.setToolTipText(tr("Enter the name of a script file"));
        gc = gbc(gc).cell(1, 0).weightx(1.0).spacingright(0).constraints();
        filePnl.add(cbScriptFile, gc);

        gc = gbc(gc).cell(2, 0).weightx(0.0).spacingleft(0).constraints();
        JButton btn;
        filePnl.add(btn = new JButton(new SelectScriptFileAction()), gc);
        btn.setFocusable(false);

        // just a filler
        JPanel filler = new JPanel();
        gc = gbc(gc).cell(0, 1, 3, 1).weight(1.0, 1.0).fillboth().constraints();
        filePnl.add(filler, gc);

        JPanel toolbarPnl = new JPanel(new FlowLayout(FlowLayout.LEADING));
        addOnToolbar = new JCheckBox(tr("add toolbar button"), false);
        addOnToolbar.setToolTipText(tr("Add a button with this search expression to the toolbar."));
        toolbarPnl.add(addOnToolbar);

        pnl.add(filePnl);
        pnl.add(toolbarPnl);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        return pnl;
    }

    protected JPanel buildContentPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(buildInfoPanel(), BorderLayout.NORTH);
        pnl.add(buildMacroFileInputPanel(), BorderLayout.CENTER);
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
                cbScriptFile.requestFocusInWindow();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            String lastFile = Preferences.main().get(PREF_KEY_LAST_FILE);
            if (lastFile != null && !lastFile.trim().isEmpty()) {
                cbScriptFile.setText(lastFile.trim());
            }
            WindowGeometry.centerInWindow(getParent(), new Dimension(600, 180))
                    .applySafe(this);
        } else {
            /*
             * Persist the file history script file name
             * in the preferences
             */
            String currentFile = cbScriptFile.getText();
            Preferences.main().put(PREF_KEY_LAST_FILE, currentFile.trim());
        }
        super.setVisible(visible);
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
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

        public RunAction() {
            super(
                    tr("Run"),
                    "run",
                    tr("Run the script"),
                    null,
                    true,
                    "scripting/run",
                    true
            );
        }

        @Override
        public List<ActionParameter<?>> getActionParameters() {
            return Collections.singletonList(new ActionParameter.StringActionParameter(SCRIPTING_FILENAME));
        }

        @Override
        public void actionPerformed(ActionEvent evt, Map<String, Object> parameters) {
            if (parameters.containsKey(SCRIPTING_FILENAME)) {
                doRun((String) parameters.get(SCRIPTING_FILENAME), false);
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            doRun(cbScriptFile.getText().trim(), addOnToolbar.isSelected());
        }

        private void doRun(String fileName, boolean addToolbar) {
            RunScriptService service = new RunScriptService();
            if (!service.canRunScript(fileName, RunScriptDialog.this)) {
                return;
            }
            if (addToolbar) {
                ToolbarPreferences.ActionDefinition aDef =
                        new ToolbarPreferences.ActionDefinition(this);
                aDef.getParameters().put(SCRIPTING_FILENAME, fileName);
                // Display filename as tooltip instead of generic one
                aDef.setName(tr("Run script {0}", Utils.shortenString(fileName, 100)));
                // parametrized action definition is now composed
                ToolbarPreferences.ActionParser actionParser = new ToolbarPreferences.ActionParser(null);
                String res = actionParser.saveAction(aDef);

                // add custom scripting button to toolbar preferences
                MainApplication.getToolbar().addCustomButton(res, -1, false);
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

    private class SelectScriptFileAction extends AbstractAction {
        public SelectScriptFileAction() {
            putValue(NAME, "..."); // don't translate
            putValue(SHORT_DESCRIPTION, tr("Launch file selection dialog"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String fileName = cbScriptFile.getText().trim();
            File currentFile = null;
            if (!fileName.isEmpty()) {
                currentFile = new File(fileName);
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(tr("Select a script file"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileHidingEnabled(false);
            if (currentFile != null) {
                chooser.setCurrentDirectory(currentFile);
                chooser.setSelectedFile(currentFile);
            }
            int ret = chooser.showOpenDialog(RunScriptDialog.this);
            if (ret != JFileChooser.APPROVE_OPTION) return;

            currentFile = chooser.getSelectedFile();
            cbScriptFile.setText(currentFile.toString());
        }
    }
}
