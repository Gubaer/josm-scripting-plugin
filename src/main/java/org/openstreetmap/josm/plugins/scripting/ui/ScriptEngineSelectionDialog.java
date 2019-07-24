package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.plugins.scripting.ui
    .GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.script.ScriptEngineFactory;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.model
    .JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.util.WindowGeometry;

/**
 * <strong>ScriptEngineSelectionDialog</strong> allows to select one of the
 *  plugable JSR-223 compatible script engines, one of the embedded script
 *  engines, or one of the scripting languages supported by the GraalVM.
 *
 */
public class ScriptEngineSelectionDialog extends JDialog {
    private static final long serialVersionUID = 3359988700106524131L;

    /**
     * <p>Launches a modal dialog for selecting a script engine.</p>
     *
     * @param parent the parent component for the dialog. Assumes
     * {@code Main.parent} if null
     *
     * @return  descriptor for the selected script engine, or null,
     *  if the user didn't select an engine
     */
    static public ScriptEngineDescriptor select(Component parent){
        return select(parent, null);
    }

    /**
     * <p>Launches a modal dialog for selecting a script engine. The dialog
     * is opened with {@code Main.parent} as owner.</p>
     *
     * @return descriptor for the selected script engine, or null, if the user
     *  didn't select an engine
     */
    static public ScriptEngineDescriptor select(){
        return select(MainApplication.getMainFrame(), null);
    }

    /**
     * <p>Launches a modal dialog for selecting a script engine. The dialog is
     * opened with {@code parent} as owner. If available, the factory
     * {@code currentFactory} is selected in the list.</p>
     *
     * @return the selected script engine, or null, if the user didn't select
     *  an engine
     */
    static public ScriptEngineDescriptor select(Component parent,
            ScriptEngineDescriptor current){
        if (parent == null) parent = MainApplication.getMainFrame();
        ScriptEngineSelectionDialog dialog =
                new ScriptEngineSelectionDialog(parent);
        dialog.setSelectedScriptEngine(current);
        dialog.setVisible(true);
        return dialog.selectedEngine;
    }

    private JList<ScriptEngineDescriptor> lstPluggedEngines;
    private JList<ScriptEngineDescriptor> lstGraalVMEngines;
    private JButton btnOK;
    private ScriptEngineDescriptor selectedEngine;
    private JSR223ScriptEngineProvider model;

    private ButtonGroup bgScriptingEngineType;
    private JRadioButton rbEmbeddedScriptingEngine;
    private JRadioButton rbPluggableScriptingEngine;
    private JRadioButton rbGraalVMScriptingEngine;

    /**
     * <p>Creates a new dialog.</p>
     *
     * @param parent the parent. Uses
     *  {@link JOptionPane#getFrameForComponent(Component)} to determine the
     *   owner frame.
     */
    public ScriptEngineSelectionDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent),
                ModalityType.APPLICATION_MODAL);
        build();
        HelpUtil.setHelpContext(getRootPane(),
                HelpUtil.ht("/Plugin/Scripting"));

    }

    protected JPanel buildEmbeddedScriptingEnginePanel() {
        if (bgScriptingEngineType == null) {
            bgScriptingEngineType = new ButtonGroup();
        }
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc  = gbc().cell(0, 0).weight(0.0, 1.0)
                .anchor(GridBagConstraints.NORTHWEST).nofill().constraints();

        rbEmbeddedScriptingEngine = new JRadioButton();
        rbEmbeddedScriptingEngine.addChangeListener(clEngineTypeChanged);
        rbEmbeddedScriptingEngine.addChangeListener(actOK);
        bgScriptingEngineType.add(rbEmbeddedScriptingEngine);
        pnl.add(rbEmbeddedScriptingEngine, gc);

        gc = gbc(gc).cell(1, 0).weight(1.0, 1.0).fillboth().constraints();
        HtmlPanel ht = new HtmlPanel();
        ht.setText("<html>"
                + "Use the embedded scripting engine for "
                + "<strong>JavaScript</strong> (ECMAScript 5.0) based on "
                + "<strong>Mozilla Rhino</strong>."
                + " </html>"
        );
        pnl.add(ht, gc);
        return pnl;
    }

    protected JPanel buildControlButtonPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btn;
        pnl.add(btnOK = new JButton(actOK));
        btnOK.setFocusable(true);
        CancelAction actCancel;
        pnl.add(btn = new JButton(actCancel = new CancelAction()));
        btn.setFocusable(true);
        pnl.add(btn = new JButton(new ContextSensitiveHelpAction(
                HelpUtil.ht("/Plugin/Scripting"))));
        btn.setFocusable(true);

        // Ctrl-Enter triggers OK
        getRootPane().registerKeyboardAction(
                actOK,
                KeyStroke.getKeyStroke("ctrl ENTER"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ESC triggers Cancel
        getRootPane().registerKeyboardAction(
                actCancel,
                KeyStroke.getKeyStroke("ESC"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        return pnl;
    }

    /**
     * Prepares the dialog for the script engine described by
     * <code>selected</code>. If <code>selected</code> is <code>null</code>,
     * assumes the the default scripting engine.
     *
     * @param selected the descriptor for the selected scripting engine
     * @see ScriptEngineDescriptor#DEFAULT_SCRIPT_ENGINE
     */
    public void setSelectedScriptEngine(ScriptEngineDescriptor selected) {
        if (selected == null) {
            selected= ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
        }
        switch(selected.getEngineType()){
            case EMBEDDED:
                rbEmbeddedScriptingEngine.setSelected(true);
                break;
            case PLUGGED:
                rbPluggableScriptingEngine.setSelected(true);
                ScriptEngineFactory factory =
                        model.getScriptFactoryByName(selected.getEngineId());
                if (factory == null){
                    lstPluggedEngines.setSelectedIndex(0);
                } else {
                    lstPluggedEngines.setSelectedValue(factory,
                            true /* scroll to selected */);
                }
                break;
        }
    }

    protected JPanel buildPluggableScriptEngineListPanel() {
        if (bgScriptingEngineType == null) {
            bgScriptingEngineType = new ButtonGroup();
        }
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc  = gbc().cell(0, 0).weight(0.0, 0.0)
                .anchor(GridBagConstraints.NORTHWEST)
                .fill(GridBagConstraints.VERTICAL)
                .constraints();
        rbPluggableScriptingEngine = new JRadioButton();
        rbPluggableScriptingEngine.addChangeListener(clEngineTypeChanged);
        rbPluggableScriptingEngine.addChangeListener(actOK);
        bgScriptingEngineType.add(rbPluggableScriptingEngine);
        pnl.add(rbPluggableScriptingEngine, gc);

        gc = gbc(gc).cell(1,0).weight(1.0,0.0).fillboth().constraints();
        HtmlPanel ht = new HtmlPanel();
        ht.setText("<html>"
                + "Use one of the available pluggable scripting engines "
                + "(see <i>Preferences</i> to configure additional engines)."
                + " </html>"
        );
        pnl.add(ht, gc);

        lstPluggedEngines.setCellRenderer(new ScriptEngineCellRenderer());

        gc = gbc(gc).cell(1,1).weight(1.0,1.0).fillboth().constraints();
        JScrollPane scrollPane = new JScrollPane(lstPluggedEngines);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnl.add(scrollPane, gc);
        lstPluggedEngines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstPluggedEngines.setSelectedIndex(0);

        lstPluggedEngines.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() >= 2) {
                            actOK.execute();
                        }
                    }
                }
        );

        return pnl;
    }

    protected JPanel buildGraalVMScriptEnginePanel() {
        final JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc  = gbc().cell(0, 0).weight(0.0, 0.0)
            .anchor(GridBagConstraints.NORTHWEST)
            .fill(GridBagConstraints.VERTICAL)
            .constraints();
        rbGraalVMScriptingEngine = new JRadioButton();
        rbGraalVMScriptingEngine.addChangeListener(clEngineTypeChanged);
        rbGraalVMScriptingEngine.addChangeListener(actOK);
        bgScriptingEngineType.add(rbGraalVMScriptingEngine);
        pnl.add(rbPluggableScriptingEngine, gc);

        gc = gbc(gc).cell(1,0).weight(1.0,0.0).fillboth().constraints();
        HtmlPanel ht = new HtmlPanel();
        ht.setText("<html>"
                + "Use one of the available scripting languages supported "
                + "by the GraalVM"
                + "(see <i>Preferences</i> to configure the GraalVM)."
                + "</html>"
        );
        pnl.add(ht, gc);

        lstGraalVMEngines.setCellRenderer(new ScriptEngineCellRenderer());

        gc = gbc(gc).cell(1,1).weight(1.0,1.0).fillboth().constraints();
        final JScrollPane scrollPane = new JScrollPane(lstGraalVMEngines);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnl.add(scrollPane, gc);

        lstGraalVMEngines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstGraalVMEngines.setSelectedIndex(0);

        lstGraalVMEngines.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        actOK.execute();
                    }
                }
            }
        );

        return pnl;
    }

    protected void build() {
        lstPluggedEngines = new JList<>(
                model = JSR223ScriptEngineProvider.getInstance());

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(buildEmbeddedScriptingEnginePanel(), BorderLayout.NORTH);
        c.add(buildPluggableScriptEngineListPanel(), BorderLayout.CENTER);
        c.add(buildControlButtonPanel(), BorderLayout.SOUTH);

        lstPluggedEngines.getSelectionModel()
            .addListSelectionListener((OKAction)btnOK.getAction());

        // Respond to 'Enter' in the list
        lstPluggedEngines.registerKeyboardAction(
            actOK,
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_FOCUSED
        );
    }

    private final OKAction actOK = new OKAction();
    private class OKAction extends AbstractAction
            implements ListSelectionListener, ChangeListener {
        public OKAction() {
            putValue(NAME, tr("OK"));
            putValue(SHORT_DESCRIPTION,
                    tr("Accept the selected scripting engine"));
            putValue(SMALL_ICON, ImageProvider.get("ok"));
        }

        public void execute() {
            if (rbEmbeddedScriptingEngine.isSelected()) {
                selectedEngine = ScriptEngineDescriptor.DEFAULT_SCRIPT_ENGINE;
            } else if (rbPluggableScriptingEngine.isSelected()) {
                int selIndex = lstPluggedEngines.getSelectedIndex();
                selectedEngine = new ScriptEngineDescriptor(
                        model.getScriptEngineFactories()
                              .get(selIndex).getNames().get(0)
                );
            }
            setVisible(false);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            execute();
        }

        protected void updateEnabeldState() {
            if (rbEmbeddedScriptingEngine.isSelected()) {
                setEnabled(true);
            } else if (rbPluggableScriptingEngine.isSelected()) {
                setEnabled(lstPluggedEngines.getSelectedIndex() >= 0);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateEnabeldState();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateEnabeldState();
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(NAME, tr("Cancel"));
            putValue(SHORT_DESCRIPTION, tr("cancel"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            selectedEngine = null;
            setVisible(false);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            btnOK.requestFocusInWindow();
            WindowGeometry
                .centerInWindow(getParent(), new Dimension(400, 200))
                .applySafe(this);
        }
        super.setVisible(visible);
    }

    private final ChangeListener clEngineTypeChanged =
        (ChangeEvent evt) ->
        lstPluggedEngines.setEnabled(rbPluggableScriptingEngine.isSelected());
}
