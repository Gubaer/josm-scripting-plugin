package org.openstreetmap.josm.plugins.scripting.preferences.common;

import org.openstreetmap.josm.gui.widgets.HtmlPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.preferences.common.IconUtil.saveImageGet;
import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

public class ImportPathsEditorPanel extends JPanel {
    static private Logger logger = Logger.getLogger(
            ImportPathsEditorPanel.class.getName());

    private JList<File> lstPaths;
    private ImportPathModel mdlPaths;

    private AddAction actAdd;
    private RemoveAction actRemove;
    private UpAction actUp;
    private DownAction actDown;

    protected JPanel buildInfoPanel() {
        HtmlPanel info = new HtmlPanel();
        info.setText(
                "<html>"
                + tr("Add directories and/or jar files where the Jython engine shall look for "
                    + "Python packages or modules."
                )
                + "</html>"
        );
        return info;
    }

    protected JPanel buildListPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        lstPaths = new JList<>(mdlPaths = new ImportPathModel(selectionModel));
        lstPaths.setCellRenderer(new ImportPathCellRenderer());
        lstPaths.setSelectionModel(selectionModel);
        JScrollPane sp = new JScrollPane(lstPaths);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnl.add(sp, BorderLayout.CENTER);
        return pnl;
    }

    protected void initAndWireAction() {
        actAdd = new AddAction();
        actRemove = new RemoveAction();
        actUp = new UpAction();
        actDown  =new DownAction();

        lstPaths.addListSelectionListener(actRemove);
        lstPaths.addListSelectionListener(actUp);
        lstPaths.addListSelectionListener(actDown);

        SysPathPopUp popup = new SysPathPopUp();
        lstPaths.setComponentPopupMenu(popup);
        // keyboard bindings
        int condition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        InputMap inputMap = lstPaths.getInputMap(condition);
        ActionMap actionMap = lstPaths.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", actRemove);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insert");
        actionMap.put("insert", actAdd);
    }

    protected void build() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(3,3,3,3);
        JPanel p1 = buildListPanel();
        initAndWireAction();
        add(buildInfoPanel(), gbc().cell(0,0).fillHorizontal().weight(1.0, 0.0).insets(insets).constraints());
        add(p1, gbc().cell(0,1).fillboth().weight(1.0,1.0).insets(insets).constraints());
    }

    public ImportPathsEditorPanel() {
        build();
    }

    /**
     * Replies the sys paths model used by this editor
     *
     * @return
     */
    public ImportPathModel getModel() {
        return mdlPaths;
    }

    private class AddAction extends AbstractAction {

        public AddAction() {
            putValue(Action.NAME, tr("Add"));
            putValue(Action.SHORT_DESCRIPTION, tr("Add a path or jar file"));
            putValue(Action.SMALL_ICON, saveImageGet("dialogs", "add"));
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            ImportPathDialog dialog = new ImportPathDialog(
                ImportPathsEditorPanel.this);
            dialog.setVisible(true);
            File path  = dialog.getPath();
            if (path != null) {
                mdlPaths.add(path);
            }
        }
    }

    private class RemoveAction extends AbstractAction implements ListSelectionListener {

        public RemoveAction() {
            putValue(Action.NAME, tr("Remove"));
            putValue(Action.SHORT_DESCRIPTION, tr("Remove a path"));
            putValue(Action.SMALL_ICON, saveImageGet("dialogs", "delete"));
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstPaths.getSelectedIndex();
            mdlPaths.remove(selIdx);
        }

        protected void updateEnabledState() {
            int selIdx = lstPaths.getSelectedIndex();
            setEnabled(selIdx != -1);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    private class UpAction extends AbstractAction implements ListSelectionListener {

        public UpAction() {
            putValue(Action.NAME, tr("Up"));
            putValue(Action.SHORT_DESCRIPTION, tr("Move the selected path up by one position"));
            putValue(Action.SMALL_ICON, saveImageGet("dialogs/conflict", "moveup"));
            updateEnabledState();
        }

        protected void updateEnabledState() {
            int selIdx = lstPaths.getSelectedIndex();
            setEnabled(selIdx != -1 && selIdx != 0);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstPaths.getSelectedIndex();
            mdlPaths.up(selIdx);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    private class DownAction extends AbstractAction implements ListSelectionListener {

        public DownAction() {
            putValue(Action.NAME, tr("Down"));
            putValue(Action.SHORT_DESCRIPTION, tr("Move the selected repository down by one position"));
            putValue(Action.SMALL_ICON, saveImageGet("dialogs", "movedown"));
            updateEnabledState();
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstPaths.getSelectedIndex();
            mdlPaths.down(selIdx);
        }

        protected void updateEnabledState() {
            int selIdx = lstPaths.getSelectedIndex();
            setEnabled(selIdx != -1 && selIdx != lstPaths.getModel().getSize() - 1);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    private class SysPathPopUp extends JPopupMenu {
        public SysPathPopUp() {
            add(actAdd);
            add(actRemove);
            add(actUp);
            add(actDown);
        }
    }
}
