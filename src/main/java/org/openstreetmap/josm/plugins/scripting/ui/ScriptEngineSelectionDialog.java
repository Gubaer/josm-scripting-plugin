package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * <strong>ScriptEngineSelectionDialog</strong> allows to select one of the
 * pluggable JSR223 compatible script engines, one of the embedded script
 * engines, or GraalJS supported by the GraalVM.
 */
public class ScriptEngineSelectionDialog extends JDialog {

    private static final Logger logger =
        Logger.getLogger(ScriptEngineSelectionDialog.class.getName());

    /**
     * Launches a modal dialog for selecting a script engine.
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
     * Launches a modal dialog for selecting a script engine. The dialog
     * is opened with {@code Main.parent} as owner.
     *
     * @return descriptor for the selected script engine, or null, if the user
     *  didn't select an engine
     */
    static public ScriptEngineDescriptor select(){
        return select(MainApplication.getMainFrame(), null);
    }

    /**
     * Launches a modal dialog for selecting a script engine. The dialog is
     * opened with {@code parent} as owner. If available, the factory
     * {@code currentFactory} is selected in the list.
     *
     * @return the selected script engine, or null, if the user didn't select
     *  an engine
     */
    static public @Null ScriptEngineDescriptor select(Component parent, ScriptEngineDescriptor current){
        if (parent == null) {
            parent = MainApplication.getMainFrame();
        }
        final var dialog = new ScriptEngineSelectionDialog(parent);
        dialog.setSelectedScriptEngine(current);
        dialog.setVisible(true);
        return dialog.selectedEngine;
    }

    private JList<ScriptEngineDescriptor> lstPluggedEngines;
    private JList<ScriptEngineDescriptor> lstGraalVMEngines;

    private JButton btnOK;
    private ScriptEngineDescriptor selectedEngine;

    private ButtonGroup bgScriptingEngineType;
    private JRadioButton rbPluggableScriptingEngine;
    private JRadioButton rbGraalVMScriptingEngine;

    /**
     * Creates a new dialog.
     *
     * @param parent the parent. Uses
     *  {@link JOptionPane#getFrameForComponent(Component)} to determine the
     *   owner frame.
     */
    public ScriptEngineSelectionDialog(Component parent) {
        super(JOptionPane.getFrameForComponent(parent), ModalityType.APPLICATION_MODAL);
        build();
        HelpUtil.setHelpContext(getRootPane(), HelpUtil.ht("/Plugin/Scripting"));

    }

    private JPanel buildControlButtonPanel() {
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

    private void setSelectedEngine(final JList<ScriptEngineDescriptor> list, final ScriptEngineDescriptor selected) {
        if (selected == null) {
            return;
        }
        final var model = list.getModel();
        IntStream.range(0,model.getSize())
            .mapToObj(model::getElementAt)
            .filter(selected::equals)
            .findFirst()
            .ifPresentOrElse(
                (desc) -> list.setSelectedValue(desc, true /* scroll to selected */),
                () -> list.setSelectedIndex(0)
            );
    }

    /**
     * Prepares the dialog for the script engine described by
     * <code>selected</code>. If <code>selected</code> is <code>null</code>,
     * assumes the default scripting engine.
     *
     * @param selected the descriptor for the selected scripting engine. If null,
     *                 no scripting engine is selected
     */
    public void setSelectedScriptEngine(@Null ScriptEngineDescriptor selected) {
        if (selected == null) {
            rbPluggableScriptingEngine.setSelected(false);
            setSelectedEngine(lstPluggedEngines, null /* nothing selected */);
            if (rbGraalVMScriptingEngine != null) {
                rbGraalVMScriptingEngine.setSelected(false);
                setSelectedEngine(lstGraalVMEngines, null /* nothing selected */);
            }
            return;
        }
        switch(selected.getEngineType()){
            case PLUGGED:
                rbPluggableScriptingEngine.setSelected(true);
                setSelectedEngine(lstPluggedEngines, selected);
                break;
            case GRAALVM:
                rbGraalVMScriptingEngine.setSelected(true);
                setSelectedEngine(lstGraalVMEngines, selected);
                break;
        }
    }

    private JPanel buildPluggableScriptEngineListPanel() {
        if (bgScriptingEngineType == null) {
            bgScriptingEngineType = new ButtonGroup();
        }
        final JPanel pnl = new JPanel(new GridBagLayout());
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
        ht.setText("<html>" + tr(
              "Use one of the available pluggable scripting engines "
            + "(see <i>Preferences</i> to configure additional engines)."
            ) + " </html>"
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

    private JPanel buildGraalVMScriptEnginePanel() {
        if (bgScriptingEngineType == null) {
            bgScriptingEngineType = new ButtonGroup();
        }
        final JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gc  = gbc().cell(0, 0).weight(0.0, 0.0)
            .anchor(GridBagConstraints.NORTHWEST)
            .fill(GridBagConstraints.VERTICAL)
            .constraints();
        rbGraalVMScriptingEngine = new JRadioButton();
        rbGraalVMScriptingEngine.addChangeListener(clEngineTypeChanged);
        rbGraalVMScriptingEngine.addChangeListener(actOK);
        bgScriptingEngineType.add(rbGraalVMScriptingEngine);
        pnl.add(rbGraalVMScriptingEngine, gc);

        gc = gbc(gc).cell(1,0).weight(1.0,0.0).fillboth().constraints();
        HtmlPanel ht = new HtmlPanel();
        ht.setText("<html>" + tr(
                  "Use GraalJS, the JavaScript engine provided by the "
                + "GraalVM (see <i>Preferences</i> to configure GraalJS)."
                ) + "</html>"
        );
        pnl.add(ht, gc);
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

    private static class GraalVMEngineListModel
                    extends AbstractListModel<ScriptEngineDescriptor> {

        private final List<ScriptEngineDescriptor> descriptors;

        GraalVMEngineListModel() {
            if (GraalVMFacadeFactory.isGraalVMPresent()) {
                descriptors = GraalVMFacadeFactory
                    .getOrCreateGraalVMFacade()
                    .getScriptEngineDescriptors()
                    // only consider GraalJS support by the GraalVM. GraalVM
                    // support for other languages (LLVM, Python, etc.) is not
                    // yet possible for other languages.
                    .stream().filter(desc -> desc
                        .getLanguageName()
                        .filter("javascript"::equalsIgnoreCase)
                        .isPresent()
                     )
                    .collect(Collectors.toList());
            } else {
                descriptors = Collections.emptyList();
            }
        }

        @Override
        public int getSize() {
            return descriptors.size();
        }

        @Override
        public ScriptEngineDescriptor getElementAt(int index) {
            return descriptors.get(index);
        }
    }

    protected void build() {
        lstPluggedEngines = new JList<>(JSR223ScriptEngineProvider.getInstance());
        lstGraalVMEngines = new JList<>(new GraalVMEngineListModel());

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        //NORTH: display the default embedded script language
        // c.add(buildEmbeddedScriptingEnginePanel(), BorderLayout.NORTH);

        //CENTER: always display the list of plugged engines. Optionally,
        // if a GraalVM is present, display the list of languages
        // it supports
        if (GraalVMFacadeFactory.isGraalVMPresent()) {
            logger.info("GraalVM is present ...");
            final JPanel pnl = new JPanel(new GridBagLayout());
            GridBagConstraints gc  = gbc().cell(0, 0).weight(0.0, 0.5)
                    .anchor(GridBagConstraints.NORTHWEST)
                    .fillboth()
                    .constraints();
            pnl.add(buildPluggableScriptEngineListPanel(),gc);
            gc = gbc(gc).cell(0,1).weight(1.0,0.5).fillboth().constraints();
            pnl.add(buildGraalVMScriptEnginePanel(),gc);
            c.add(pnl, BorderLayout.CENTER);
        } else {
            logger.info("GraalVM is not present ...");
            c.add(buildPluggableScriptEngineListPanel(), BorderLayout.CENTER);
        }

        //SOUTH: the action buttons
        c.add(buildControlButtonPanel(), BorderLayout.SOUTH);

        // double-click on an engine selects the engine and closes
        // the dialog
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
        OKAction() {
            putValue(NAME, tr("OK"));
            putValue(SHORT_DESCRIPTION,
                    tr("Accept the selected scripting engine"));
            putValue(SMALL_ICON, ImageProvider.get("ok"));
        }

        public void execute() {
           if (rbPluggableScriptingEngine.isSelected()) {
                selectedEngine = lstPluggedEngines.getSelectedValue();
            } else if (rbGraalVMScriptingEngine.isSelected()) {
                selectedEngine = lstGraalVMEngines.getSelectedValue();
            }
            setVisible(false);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            execute();
        }

        private void updateEnabledState() {
            if (rbPluggableScriptingEngine.isSelected()) {
                setEnabled(lstPluggedEngines.getSelectedIndex() >= 0);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateEnabledState();
        }
    }

    private class CancelAction extends AbstractAction {
        CancelAction() {
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
                .centerInWindow(getParent(), new Dimension(400, 600))
                .applySafe(this);
        }
        super.setVisible(visible);
    }

    private final ChangeListener clEngineTypeChanged =
        (ChangeEvent evt) -> {
            lstPluggedEngines.setEnabled(rbPluggableScriptingEngine.isSelected());
            if (rbGraalVMScriptingEngine != null) {
                lstGraalVMEngines.setEnabled(rbGraalVMScriptingEngine.isSelected());
            }
        };
}
