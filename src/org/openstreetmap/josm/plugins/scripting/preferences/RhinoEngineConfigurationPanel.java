package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.scripting.model.CommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.tools.ImageProvider;


public class RhinoEngineConfigurationPanel extends VerticallyScrollablePanel{
    private static final Logger logger = Logger.getLogger(RhinoEngineConfigurationPanel.class.getName());

    private RepositoriesListModel mdlRepositories;
    private JList<URL> lstRepositories;
    private RemoveAction actRemove;
    private UpAction actUp;
    private DownAction actDown;

    protected JPanel buildInfoPanel() {
        HtmlPanel info = new HtmlPanel();
        info.setText(
                "<html>"
                + tr("The scripting plugin includes an embedded scripting engine for JavaScript based on Mozilla Rhino. "
                    + "It can load CommonJS modules either from the local filesystem or from jar/zip "
                    + "files.<br><br>"
                    + "Per default, it loads CommonJS modules from the directory <strong>/js</strong> in the plugin jar, "
                    + "but you can add additional directories and jar files.<br><br>"
                    + "Configure them in the list below."
                )
                + "</html>"
        );
        return info;
    }

    protected JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
        lstRepositories = new JList<>(mdlRepositories = new RepositoriesListModel(selectionModel));
        lstRepositories.setCellRenderer(new RepositoryCellRenderer());
        lstRepositories.setSelectionModel(selectionModel);
        JScrollPane sp = new JScrollPane(lstRepositories);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    protected JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc().fillHorizontal().weight(1.0, 0.0).constraints();
        panel.add(new JButton(new AddAction()), gbc(gc).cell(0, 0).constraints());
        panel.add(new JButton(actRemove = new RemoveAction()), gbc(gc).cell(0, 1).constraints());
        panel.add(new JButton(actUp = new UpAction()), gbc(gc).cell(0,2).constraints());
        panel.add(new JButton(actDown  =new DownAction()), gbc(gc).cell(0,3).constraints());
        panel.add(new JPanel(), gbc().cell(0, 4).fillboth().weight(1.0, 1.0).constraints());

        lstRepositories.addListSelectionListener(actRemove);
        lstRepositories.addListSelectionListener(actUp);
        lstRepositories.addListSelectionListener(actDown);

        return panel;
    }

    protected void build() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(3,3,3,3);
        JPanel p1 = buildTablePanel();
        JPanel p2 = buildActionPanel();
        add(buildInfoPanel(), gbc().cell(0,0,2,1).fillHorizontal().weight(1.0, 0.0).insets(insets).constraints());
        add(p2, gbc().cell(0,1).fillVertical().weight(0.0,1.0).insets(insets).constraints());
        add(p1, gbc().cell(1,1).fillboth().weight(1.0,1.0).insets(insets).constraints());
    }

    public RhinoEngineConfigurationPanel() {
        build();
    }

    public void persistToPreferences() {
        mdlRepositories.saveToPreferences();
    }

    static public class RepositoriesListModel extends AbstractListModel<URL> implements PreferenceKeys {

        private final List<URL> repositories = new ArrayList<>();
        private DefaultListSelectionModel selectionModel;

        public RepositoriesListModel(DefaultListSelectionModel selectionModel) {
            loadFromPreferences();
            this.selectionModel = selectionModel;
        }

        public void loadFromPreferences() {
            Assert.assertState(Main.pref != null, "Main.pref is not initialized. Can''t load preferences."); // do not translate
            loadFromPreferences(Main.pref);
        }

        public void loadFromPreferences(@NotNull Preferences prefs) {
            Assert.assertArgNotNull(prefs);
            Collection<String> entries = prefs.getCollection(PREF_KEY_COMMONJS_MODULE_REPOSITORIES);
            for (Iterator<String> it = entries.iterator(); it.hasNext();) {
                String entry = it.next().trim();
                try {
                    repositories.add(new CommonJSModuleRepository(entry).getURL());
                } catch(IllegalArgumentException e) {
                    logger.log(Level.WARNING, MessageFormat.format("Failed to create a module repository from preferences value <{0}>. Skipping it.", entry),e);
                    continue;
                }
            }
        }

        public void saveToPreferences(Preferences pref) {
            List<String> entries = repositories.stream()
                    .map(url -> url.toString())
                    .collect(Collectors.toList());
            pref.putCollection(PREF_KEY_COMMONJS_MODULE_REPOSITORIES,entries);
        }

        public void saveToPreferences() {
            Assert.assertState(Main.pref != null, "Main.pref is not initialized. Can''t save preferences."); // do not translate
            saveToPreferences(Main.pref);
        }

        public void remove(int i) {
            repositories.remove(i);
            fireIntervalRemoved(this,i,i);
        }


        public void up(int i) {
            URL tomove = repositories.remove(i);
            repositories.add(i-1, tomove);
            selectionModel.setSelectionInterval(i-1, i-1);
            fireContentsChanged(this,0,getSize());
        }

        public void down(int i) {
            URL tomove = repositories.remove(i);
            repositories.add(i+1, tomove);
            selectionModel.setSelectionInterval(i+1, i+1);
            fireContentsChanged(this,0,getSize());
        }

        public void add(CommonJSModuleRepository repository) {
            if (repository == null) return;
            repositories.add(repository.getURL());
            fireContentsChanged(this,0,getSize());
        }

        @Override
        public URL getElementAt(int index) {
            return repositories.get(index);
        }

        @Override
        public int getSize() {
            return repositories.size();
        }
    }

    static public class RepositoryCellRenderer extends JLabel implements ListCellRenderer<URL> {

        private Icon jarIcon;
        private Icon dirIcon;
        public RepositoryCellRenderer() {
            setOpaque(true);
            jarIcon = ImageProvider.get("jar");
            dirIcon = ImageProvider.get("directory");
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends URL> list, URL url, int index, boolean isSelected, boolean hasFocus) {
            setText(url.toString());
            if (isSelected) {
                setForeground(UIManager.getColor("List.selectionForeground"));
                setBackground(UIManager.getColor("List.selectionBackground"));
            } else {
                setForeground(UIManager.getColor("List.foreground"));
                setBackground(UIManager.getColor("List.background"));
            }
            if (url.getProtocol().equals("jar")) {
                setIcon(jarIcon);
            } else if (url.getProtocol().equals("file")) {
                setIcon(dirIcon);
            }
            return this;
        }
    }

    private class AddAction extends AbstractAction {

        public AddAction() {
            //putValue(Action.NAME, tr("Add"));
            putValue(Action.SHORT_DESCRIPTION, tr("Add an additional repository"));
            putValue(Action.SMALL_ICON, ImageProvider.get("dialogs", "add"));
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            ModuleRepositoryDialog dialog = new ModuleRepositoryDialog(RhinoEngineConfigurationPanel.this);
            dialog.setVisible(true);
            CommonJSModuleRepository repository = dialog.getRepository();
            if (repository != null) {
                mdlRepositories.add(repository);
            }
        }
    }

    private class RemoveAction extends AbstractAction implements ListSelectionListener {

        public RemoveAction() {
            //putValue(Action.NAME, tr("Remove"));
            putValue(Action.SHORT_DESCRIPTION, tr("Remove a repository"));
            putValue(Action.SMALL_ICON, ImageProvider.get("dialogs", "delete"));
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstRepositories.getSelectedIndex();
            mdlRepositories.remove(selIdx);
        }

        protected void updateEnabledState() {
            int selIdx = lstRepositories.getSelectedIndex();
            setEnabled(selIdx != -1);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    private class UpAction extends AbstractAction implements ListSelectionListener {

        public UpAction() {
            //putValue(Action.NAME, tr("Up"));
            putValue(Action.SHORT_DESCRIPTION, tr("Move the selected repository up by one position"));
            putValue(Action.SMALL_ICON, ImageProvider.get("dialogs", "moveup"));
            updateEnabledState();
        }


        protected void updateEnabledState() {
            int selIdx = lstRepositories.getSelectedIndex();
            setEnabled(selIdx != -1 && selIdx != 0);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstRepositories.getSelectedIndex();
            mdlRepositories.up(selIdx);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    private class DownAction extends AbstractAction implements ListSelectionListener {

        public DownAction() {
            //putValue(Action.NAME, tr("Down"));
            putValue(Action.SHORT_DESCRIPTION, tr("Move the selected repository down by one position"));
            putValue(Action.SMALL_ICON, ImageProvider.get("dialogs", "movedown"));
            updateEnabledState();
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstRepositories.getSelectedIndex();
            mdlRepositories.down(selIdx);
        }

        protected void updateEnabledState() {
            int selIdx = lstRepositories.getSelectedIndex();
            setEnabled(selIdx != -1 && selIdx != lstRepositories.getModel().getSize() - 1);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }
}
