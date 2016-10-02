package org.openstreetmap.josm.plugins.scripting.preferences;

import static java.awt.GridBagConstraints.NORTHWEST;
import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import javax.script.ScriptEngineFactory;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineCellRenderer;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * <p>A modal dialog for creating and editing script descriptors.</p>
 *
 */
public class ScriptDescriptorEditorDialog extends JDialog {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(ScriptDescriptorEditorDialog.class.getName());

    private JTextField tfId;
    private JTextField tfDisplayName;
    private JTextField tfScriptFile;
    private JButton btnLaunchFileChooser;
    private ScriptingEngineEditorPanel pnlScriptEngineEditor;
    private OKAction actOK;
    private boolean canceled = false;

    /**
     *
     * @param parent the parent component which {@link JOptionPane#getFrameForComponent(Component) the parent frame is derived from}
     */
    public ScriptDescriptorEditorDialog(Component parent){
        super(JOptionPane.getFrameForComponent(parent), ModalityType.DOCUMENT_MODAL);
        build();
    }

    protected JPanel buildScriptFileInputPanel() {
        JPanel pnl = new JPanel( new GridBagLayout());
        GridBagConstraints gbc = gbc().cell(0,0).weight(1.0,0.0).constraints();
        pnl.add(tfScriptFile = new JTextField(), gbc);
        tfScriptFile.setToolTipText(tr("Full local path to the script file"));
        SelectAllOnFocusGainedDecorator.decorate(tfScriptFile);

        gbc = gbc().cell(1,0).weight(0.0,0.0).constraints();
        pnl.add(btnLaunchFileChooser = new JButton(new SelectScriptFileAction()), gbc);
        btnLaunchFileChooser.setFocusable(false);
        btnLaunchFileChooser.setBorder(null);
        return pnl;
    }

    protected JPanel buildMainPropertiesPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = gbc().cell(0,0).weight(0.0,0.0).spacingright(5).constraints();
        pnl.add(new JLabel(tr("ID:")), gbc);

        gbc = gbc().cell(1,0).weight(1.0,0.0).constraints();
        pnl.add(tfId = new JTextField(), gbc);
        tfId.setToolTipText(tr("The unique internal ID used to identify the script."));
        SelectAllOnFocusGainedDecorator.decorate(tfId);

        gbc = gbc().cell(2,0).weight(0.0,0.0).constraints();
        JButton btn;
        pnl.add(btn = new JButton(new GenerateIdAction()), gbc);
        btn.setBorder(null);
        btn.setFocusable(false);

        gbc = gbc().cell(0,1).weight(0.0,0.0).spacingright(5).constraints();
        pnl.add(new JLabel(tr("Display name:")), gbc);

        gbc = gbc().cell(1,1,2,1).weight(1.0,0.0).constraints();
        pnl.add(tfDisplayName = new JTextField(), gbc);
        tfDisplayName.setToolTipText(tr("The display name of the script. Used in menu entries."));
        SelectAllOnFocusGainedDecorator.decorate(tfDisplayName);

        gbc = gbc().cell(0,2).weight(0.0,0.0).spacingright(5).constraints();
        pnl.add(new JLabel(tr("Script file:")), gbc);

        gbc = gbc().cell(1,2, 2, 1).weight(1.0,0.0).constraints();
        pnl.add(buildScriptFileInputPanel(), gbc);

        gbc = gbc().cell(0,3,3,1).weight(1.0,0.0).anchor(NORTHWEST).constraints();
        pnl.add(pnlScriptEngineEditor = new ScriptingEngineEditorPanel(), gbc);

        gbc = gbc().cell(0,4,3,1).weight(1.0,1.0).constraints();
        pnl.add(new JPanel(), gbc);
        return pnl;
    }

    protected JPanel buildControlButtonPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnl.add(new SideButton(actOK = new OKAction()));
        pnl.add(new SideButton(new CancelAction()));
        // FIXME: adjust help topic
        pnl.add(new SideButton(new ContextSensitiveHelpAction(HelpUtil.ht("/Plugins/Scripting"))));
        return pnl;
    }

    protected void build() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel pnl  =new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        pnl.add(buildMainPropertiesPanel(), BorderLayout.CENTER);
        pnl.add(buildControlButtonPanel(), BorderLayout.SOUTH);
        c.add(pnl, BorderLayout.CENTER);

        addWindowListener(new WindowEventHandler());

        tfId.getDocument().addDocumentListener(actOK);
        tfScriptFile.getDocument().addDocumentListener(actOK);

        tfScriptFile.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {propagate();}
            public void insertUpdate(DocumentEvent e) {propagate();}
            public void changedUpdate(DocumentEvent e) {propagate();}
            public void propagate() {
                pnlScriptEngineEditor.refreshAutomaticallyDerivedScriptingEngine();
            }
        });
    }

    public void setVisible(boolean visible){
        if (visible){
            new WindowGeometry(
                    getClass().getName() + ".geometry",
                    WindowGeometry.centerInWindow(getParent(), new Dimension(400,400))
            ).applySafe(this);
        } else {
            new WindowGeometry(this).remember(getClass().getName() + ".geometry");
        }
        super.setVisible(visible);
    }

    /**
     * <p>Populates the editor with the content of a script descriptor {@code sd}.
     * If {@code sd} is null, the editor is initialized with default values.</p>
     *
     * @param sd the script descriptor. May be null.
     */
    public void populate(ScriptDescriptor sd){
        if (sd == null){
            tfDisplayName.setText("");
            tfId.setText("");
            tfScriptFile.setText("");
            setTitle(tr("Define a new script"));
        } else {
            tfId.setText(sd.getId());
            tfDisplayName.setText(sd.getDisplayName() == null ? "" : sd.getDisplayName());
            tfScriptFile.setText(sd.getScriptFile() == null ? "" : sd.getScriptFile().toString());
            setTitle(tr("Edit a script"));
        }
        pnlScriptEngineEditor.populate(sd);
    }

    /**
     * Grabs the edited content in the script descriptor editor and assigns
     * it to the script descriptor {@code sd}.
     *
     * @param sd the script descriptor. Must not be null.
     * @throws IllegalArgumentException thrown if {@code sd} is null
     * @throws IllegalStateException thrown if the editor content isn't in a state which
     * can be assigned to a script descriptor (example: the id consist of white space only)
     */
    public void grab(@NotNull ScriptDescriptor sd) throws IllegalArgumentException, IllegalStateException {
        Assert.assertArgNotNull(sd);
        if (tfId.getText().trim().isEmpty()) {
            throw new IllegalStateException("id must not be empty or consists of white space only");
        }
        if (tfScriptFile.getText().trim().isEmpty()) {
            throw new IllegalStateException("display name must not be empty or consists of white space only");
        }
        sd.setId(tfId.getText());
        sd.setDisplayName(tfDisplayName.getText());
        sd.setScriptFile(new File(tfScriptFile.getText().trim()));
        pnlScriptEngineEditor.grab(sd);
    }

    /**
     * Replies true if the dialog was canceled.
     *
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    private class GenerateIdAction extends AbstractAction {
        public GenerateIdAction() {
            putValue(SMALL_ICON, ImageProvider.get("new"));
            putValue(SHORT_DESCRIPTION, tr("Generate a globally unique ID"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tfId.setText(UUID.randomUUID().toString());
        }
    }

    private class OKAction extends AbstractAction implements DocumentListener {
        public OKAction() {
            putValue(NAME, tr("OK"));
            putValue(SHORT_DESCRIPTION, tr("Accept script descriptor and close dialog"));
            putValue(SMALL_ICON, ImageProvider.get("ok"));
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canceled = false;
            setVisible(false);
        }

        protected void updateEnabledState(){
            if (tfId.getText().trim().isEmpty()) {
                setEnabled(false);
                return;
            }
            if (tfScriptFile.getText().trim().isEmpty()) {
                setEnabled(false);
                return;
            }
            setEnabled(true);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateEnabledState();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateEnabledState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateEnabledState();
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(NAME, tr("Cancel"));
            putValue(SHORT_DESCRIPTION, tr("Cancel and close dialog"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canceled = true;
            setVisible(false);
        }
    }

    class WindowEventHandler extends WindowAdapter {
        @Override
        public void windowActivated(WindowEvent e) {
            tfDisplayName.requestFocusInWindow();
            canceled = false;
        }
    }

    private class SelectScriptFileAction extends AbstractAction {
        public SelectScriptFileAction() {
            putValue(SMALL_ICON, ImageProvider.get("open"));
            putValue(SHORT_DESCRIPTION, tr("Launch file selection dialog"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String fileName = tfScriptFile.getText().trim();
            File currentFile = null;
            if (! fileName.isEmpty()) {
                currentFile = new File(fileName);
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(tr("Select a script"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            if (currentFile != null){
                chooser.setCurrentDirectory(currentFile);
                chooser.setSelectedFile(currentFile);
            }
            int ret = chooser.showOpenDialog(ScriptDescriptorEditorDialog.this);
            if (ret != JFileChooser.APPROVE_OPTION) return;

            currentFile = chooser.getSelectedFile();
            tfScriptFile.setText(currentFile.toString());
        }
    }

    private class ScriptingEngineEditorPanel extends JPanel {

        private JRadioButton rbDeriveFromFileName;
        private JRadioButton rbManuallySelectScriptEngine;
        private ButtonGroup bgMethodToSelectScriptEngine;
        private JComboBox<ScriptEngineFactory> cbScriptEngines;
        private JPanel  pnlSelectScriptEngine;
        private HtmlPanel pnlAutomaticallyDeriveEngine;

        public ScriptingEngineEditorPanel() {
            build();
        }

        public void populate(ScriptDescriptor sd){
            if (sd == null){
                rbDeriveFromFileName.setSelected(true);
            } else {
                if (sd.getScriptEngineName() == null){
                    rbDeriveFromFileName.setSelected(true);
                } else {
                    rbManuallySelectScriptEngine.setSelected(true);
                    cbScriptEngines.getModel().setSelectedItem(
                        JSR223ScriptEngineProvider.getInstance().getScriptFactoryByName(sd.getScriptEngineName())
                    );
                }
            }
        }

        public void grab(@NotNull ScriptDescriptor sd) throws IllegalArgumentException{
            Assert.assertArgNotNull(sd);
            if (rbDeriveFromFileName.isSelected()) {
                sd.setScriptEngineName(null);
            } else if (cbScriptEngines.getSelectedItem() == null){
                sd.setScriptEngineName(null);
            } else {
                ScriptEngineFactory f = (ScriptEngineFactory)cbScriptEngines.getSelectedItem();
                sd.setScriptEngineName(f.getEngineName());
            }
        }

        public void refreshAutomaticallyDerivedScriptingEngine() {
            String msg = tr("Automatically derive a script engine from the file name.");
            if (rbDeriveFromFileName.isSelected()) {
// FIXME:
//                if (! file.trim().isEmpty()) {
//                    ScriptEngine engine = JSR223ScriptEngineProvider.getInstance().getEngineForFile(new File(file));
//                    if (engine == null) {
//                        msg = msg + "<br>\n"
//                        + "<font color=\"red\">"
//                        + tr("No scripting engine found for file ''{0}''.", file)
//                        + "</font>";
//                    } else {
//                        msg = msg + "<br>\n"
//                        + tr("Using scripting engine <strong>{0}</strong>.", engine.getFactory().getEngineName());
//                    }
//                }
            }
            pnlAutomaticallyDeriveEngine.setText(
                    "<html>" + msg + "</html>"
            );
        }

        protected void build(){
            setBorder(BorderFactory.createTitledBorder(tr("Script Engine")));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = gbc().cell(0, 0).weight(0.0, 0.0).anchor(NORTHWEST).constraints();
            add(rbDeriveFromFileName = new JRadioButton(), gbc);

            gbc = gbc().cell(1, 0).weight(1.0, 0.0).anchor(NORTHWEST).constraints();
            add(pnlAutomaticallyDeriveEngine = new HtmlPanel(""), gbc);

            gbc = gbc().cell(0, 1).weight(0.0, 0.0).anchor(NORTHWEST).constraints();
            add(rbManuallySelectScriptEngine = new JRadioButton(), gbc);

                pnlSelectScriptEngine= new JPanel(new GridBagLayout());
                gbc = gbc().cell(0,0,2,1).weight(1.0, 0.0).anchor(NORTHWEST).constraints();
                pnlSelectScriptEngine.add(new HtmlPanel(tr("Select one of the available script engines for executing this script.")), gbc);
                gbc = gbc().cell(0, 1).weight(0.0, 0.0).anchor(NORTHWEST).spacingright(5).constraints();
                pnlSelectScriptEngine.add(new JLabel(tr("Script engines:")), gbc);
                gbc = gbc().cell(1, 1).weight(0.0, 0.0).anchor(NORTHWEST).constraints();
                pnlSelectScriptEngine.add(cbScriptEngines = new JComboBox<>(new ScriptEngineComboBoxModel()), gbc);
                cbScriptEngines.setRenderer(new ScriptEngineCellRenderer());
                cbScriptEngines.setToolTipText(tr("Choose a script engine"));

                gbc = gbc().cell(0, 2,2,1).weight(1.0, 0.0).constraints();
                pnlSelectScriptEngine.add(new JPanel(), gbc);

            gbc = gbc().cell(1, 1).weight(1.0, 0.0).anchor(NORTHWEST).constraints();
            add(pnlSelectScriptEngine, gbc);

            // created and configure the button group for the radio buttons
            bgMethodToSelectScriptEngine = new ButtonGroup();
            bgMethodToSelectScriptEngine.add(rbDeriveFromFileName);
            bgMethodToSelectScriptEngine.add(rbManuallySelectScriptEngine);

            // make sure we respond to clicks on the radio buttons
            ItemListener l = new RadioToggleListener();
            rbDeriveFromFileName.addItemListener(l);
            rbManuallySelectScriptEngine.addItemListener(l);
            rbDeriveFromFileName.setSelected(true);

            refreshAutomaticallyDerivedScriptingEngine();
        }

        /**
         * Recursively enables/disables a container and its child components.
         *
         * @param parent the parent container
         * @param enabled true, if the components are to enable; false, otherwise
         */
        protected void setEnabled(Container parent, boolean enabled){
            if (parent == null) return;
            parent.setEnabled(enabled);
            for (Component c: parent.getComponents()) {
                if (c instanceof Component) {
                    c.setEnabled(enabled);
                } else {
                    setEnabled((Container)c, enabled);
                }
            }
        }

        class RadioToggleListener implements ItemListener{
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object source = e.getSource();
                if (source == rbDeriveFromFileName){
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        setEnabled(pnlSelectScriptEngine, false);
                    }
                } else if (source == rbManuallySelectScriptEngine) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        setEnabled(pnlSelectScriptEngine, true);
                    }
                }
                refreshAutomaticallyDerivedScriptingEngine();
            }
        }
    }
}
