package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.ui.mru.MostRecentlyRunScriptsComboBox;
import org.openstreetmap.josm.plugins.scripting.ui.mru.MostRecentlyRunScriptsModel;
import org.openstreetmap.josm.plugins.scripting.ui.mru.Script;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Provides a modal dialog for selecting and running a script.
 */
public class RunScriptDialog extends JDialog implements PreferenceKeys {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(RunScriptDialog.class.getName());

    static private final RunScriptDialog instance = new RunScriptDialog(MainApplication.getMainFrame());
    /**
     * the input field for the script file name
     */
    private MostRecentlyRunScriptsComboBox cbScriptFile;

    /**
     * Constructor
     *
     * @param parent the dialog owner
     */
    public RunScriptDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent), ModalityType.DOCUMENT_MODAL);
        build();
        HelpUtil.setHelpContext(this.getRootPane(), HelpUtil.ht("/Plugin/Scripting"));
    }

    static public RunScriptDialog getInstance() {
        return instance;
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
        final var pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final var actRun = new RunAction();
        final var btn = new JButton(actRun);
        pnl.add(btn);
        btn.setFocusable(true);
        btn.registerKeyboardAction(actRun, KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_FOCUSED);
        getRootPane().registerKeyboardAction(actRun,
            KeyStroke.getKeyStroke("ctrl ENTER"),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        pnl.add(new JButton(new CancelAction()));
        final var helpAction = new ContextSensitiveHelpAction(HelpUtil.ht("/Plugin/Scripting#Run"));
        pnl.add(new JButton(helpAction));
        return pnl;
    }

    private JPanel buildMacroFileInputPanel() {
        JPanel pnl = new JPanel();

        JPanel filePnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc().cell(0, 0).weight(0, 0).fillboth()
            .insets(3, 3, 3, 3).constraints();
        filePnl.add(new JLabel(tr("File:")), gc);

        cbScriptFile = new MostRecentlyRunScriptsComboBox(MostRecentlyRunScriptsModel.getInstance());
        SelectAllOnFocusGainedDecorator.decorate((JTextField) cbScriptFile.getEditor().getEditorComponent());
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

        pnl.add(filePnl);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        return pnl;
    }

    private JPanel buildContentPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(buildInfoPanel(), BorderLayout.NORTH);
        pnl.add(buildMacroFileInputPanel(), BorderLayout.CENTER);
        pnl.add(buildControlButtonPanel(), BorderLayout.SOUTH);
        return pnl;
    }

    protected void build() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
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
            WindowGeometry
                .centerInWindow(getParent(), new Dimension(600, 180))
                .applySafe(this);
        } else {
            /*
             * Persist the file history script file name
             * in the preferences
             */
            Preferences.main().put(PREF_KEY_LAST_FILE, cbScriptFile.getText().trim());
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

    private class RunAction extends AbstractAction {
        RunAction() {
            putValue(NAME, tr("Run"));
            putValue(SHORT_DESCRIPTION, tr("Run"));
            putValue(SMALL_ICON, ImageProvider.get("run"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final var scriptFile = cbScriptFile.getText();
            final var service = new RunScriptService();
            if (!service.canRunScript(scriptFile, RunScriptDialog.this)) {
                return;
            }
            final var engine = service.deriveOrAskScriptEngineDescriptor(scriptFile, RunScriptDialog.this);
            if (engine == null) {
                return;
            }
            setVisible(false);
            service.runScript(scriptFile, engine, MainApplication.getMainFrame());

            final var updatedScript = new Script(new File(scriptFile).getAbsolutePath(), engine.getFullEngineId());
            final var model = MostRecentlyRunScriptsModel.getInstance();
            model.remember(updatedScript);
            model.saveToPreferences(Preferences.main());
        }
    }

    private class SelectScriptFileAction extends AbstractAction {
        SelectScriptFileAction() {
            putValue(SHORT_DESCRIPTION, tr("Launch file selection dialog"));
            putValue(SMALL_ICON, ImageProvider.get("open", ImageProvider.ImageSizes.SMALLICON));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final var fileName = cbScriptFile.getText().trim();
            File currentFile = null;
            if (!fileName.isEmpty()) {
                currentFile = new File(fileName);
            }
            final var chooser = new JFileChooser();
            chooser.setDialogTitle(tr("Select a script file"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileHidingEnabled(false);
            if (currentFile != null) {
                chooser.setCurrentDirectory(currentFile);
                chooser.setSelectedFile(currentFile);
            }
            final int ret = chooser.showOpenDialog(RunScriptDialog.this);
            if (ret != JFileChooser.APPROVE_OPTION) return;

            currentFile = chooser.getSelectedFile();
            cbScriptFile.setText(currentFile.toString());
        }
    }
}
