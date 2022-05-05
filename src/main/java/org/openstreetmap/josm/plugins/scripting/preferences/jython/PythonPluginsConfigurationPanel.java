package org.openstreetmap.josm.plugins.scripting.preferences.jython;

import static org.openstreetmap.josm.plugins.scripting.ui
    .GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.tools.ImageProvider;

public class PythonPluginsConfigurationPanel extends JPanel {

    private PythonPluginsTable tblPlugins;
    private PythonPluginsModel mdlPlugins;

    private AddAction actAdd;
    private DeleteAction actDelete;

    protected JPanel buildInfoPanel() {
        String text = "<html>"
            + tr("Add a list of fully qualified names of Python plugin "
               + "classes the scripting plugin shall load and install "
               + "at startup time. <br>Example: ''my_module.MyPlugin'' or "
               + "''my_package.my_module.MyPlugin''"
            )
            + "</html>";
        HtmlPanel info = new HtmlPanel();
        info.setText(text);
        return info;
    }

    protected JPanel buildTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        mdlPlugins = new PythonPluginsModel();
        tblPlugins = new PythonPluginsTable(mdlPlugins);
        JScrollPane pane = new JScrollPane(tblPlugins);
        pane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected void initAndWireAction() {
        actAdd = new AddAction();
        actDelete = new DeleteAction();

        mdlPlugins.getSelectionModel().addListSelectionListener(actDelete);

        // keyboard bindings
        InputMap inputMap = tblPlugins.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = tblPlugins.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", actDelete);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insert");
        actionMap.put("insert", actAdd);
    }

    protected void initAndWirePopup() {
        PluginTablePopUp popupPluginTable = new PluginTablePopUp();
        tblPlugins.setComponentPopupMenu(popupPluginTable);
    }

    protected void build() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(3,3,3,3);
        add(buildInfoPanel(),
                gbc().cell(0,0).fillHorizontal()
                .weight(1.0, 0.0).insets(insets).constraints());
        add(buildTablePanel(),
                gbc().cell(0,1).fillboth().weight(1.0, 1.0)
                .insets(insets).constraints());
    }

    public PythonPluginsConfigurationPanel() {
        build();
        initAndWireAction();
        initAndWirePopup();
    }

    public PythonPluginsModel getModel() {
        return mdlPlugins;
    }

    static public class PythonPluginsModel extends AbstractTableModel
        implements PreferenceKeys {
        private final List<String> plugins = new ArrayList<>();
        private final DefaultListSelectionModel selectionModel =
                new DefaultListSelectionModel();

        public DefaultListSelectionModel getSelectionModel() {
            return selectionModel;
        }

        /**
         * Adds an additional empty plugin
         */
        public void addNew(){
            plugins.add("");
            fireTableDataChanged();
        }

        /**
         * Set the list of plugin names to be edited.
         *
         * @param plugins the collecion of plugin names
         */
        public void setPlugins(Collection<String> plugins){
            this.plugins.clear();
            this.plugins.addAll(plugins);
            this.plugins.remove(null);
            Collections.sort(this.plugins);
            fireTableDataChanged();
        }

        /**
         * Replies the list of plugin names in this model (ignoring any
         * empty plugin name consisting of white space only)
         *
         * @return the list of plugin names
         */
        public List<String> getPlugins() {
            return plugins.stream()
                .map(String::trim)
                .filter(plugin -> !plugin.isEmpty())
                .collect(Collectors.toList());
        }

        /**
         * Removes the currently selected plugins
         */
        public void removeSelectedPlugins() {
          int deleted = 0;
          for (int i = 0; i< plugins.size(); i++) {
              if (selectionModel.isSelectedIndex(i)) {
                  plugins.remove(i-deleted);
                  deleted++;
              }
          }
          fireTableDataChanged();
        }

        public void loadFromPreferences(Preferences pref) {
            Collection<String> c = pref.getList(
               PREF_KEY_JYTHON_PLUGINS,
               null
            );
            if (c == null) return;
            setPlugins(c);
        }

        public void persistToPreferences(Preferences pref) {
            List<String> plugins = getPlugins();
            pref.putList(
              PREF_KEY_JYTHON_PLUGINS,
              plugins
            );
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return plugins.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return plugins.get(row);
        }

        public void setValueAt(Object value, int row, int col) {
            String plugin = (String)value;
            plugins.set(row,plugin);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    }

    static public class PythonPluginsTable extends JTable {
        public PythonPluginsTable(PythonPluginsModel model) {
            super(model);
            setTableHeader(null);
            setSelectionModel(model.getSelectionModel());
        }

        //hack: enables popup on empty table
        public boolean getScrollableTracksViewportHeight() {
            return getPreferredSize().height < getParent().getHeight();
        }
    }

    private class AddAction extends AbstractAction {
        public AddAction() {
            putValue(NAME, tr("Add"));
            putValue(SHORT_DESCRIPTION, tr("Add a plugin"));
            putValue(SMALL_ICON, ImageProvider.getIfAvailable("add"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            mdlPlugins.addNew();
        }
    }

    private class DeleteAction extends AbstractAction
            implements ListSelectionListener {

        public DeleteAction() {
            putValue(NAME, tr("Remove"));
            putValue(SHORT_DESCRIPTION, tr("Remove selected plugins"));
            putValue(SMALL_ICON,
                    ImageProvider.getIfAvailable("dialogs", "delete"));
        }

        public void updateEnabledState() {
            setEnabled(!mdlPlugins.getSelectionModel().isSelectionEmpty());
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            mdlPlugins.removeSelectedPlugins();
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
          updateEnabledState();
        }
    }

    private class PluginTablePopUp extends JPopupMenu {
        public PluginTablePopUp() {
           add(actAdd);
           add(actDelete);
        }
    }
}
