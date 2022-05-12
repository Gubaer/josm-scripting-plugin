package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryRegistry;
import org.openstreetmap.josm.plugins.scripting.graalvm.ICommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.graalvm.IllegalCommonJSModuleBaseURI;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

public class CommonJSRepoConfigurationPanel extends JPanel  {
    private static final Logger logger =
        Logger.getLogger(CommonJSRepoConfigurationPanel.class.getName());

    private RepositoriesListModel mdlRepositories;
    private JList<URL> lstRepositories;

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
            + tr(
                "<p>"
                + "The embedded GraalVM can load <strong>CommonJS modules</strong> "
                + "with the function <code>require()</code>. It resolves CommonJS modules "
                + "in the directories or jar files configured below."
                + "</p>"
            )
            + "</html>";
        pane.setText(text);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected JPanel buildTablePanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        DefaultListSelectionModel selectionModel =
            new DefaultListSelectionModel();
        lstRepositories = new JList<>(mdlRepositories =
            new RepositoriesListModel(selectionModel));
        lstRepositories.setCellRenderer(new RepositoryCellRenderer());
        lstRepositories.setSelectionModel(selectionModel);
        JScrollPane sp = new JScrollPane(lstRepositories);
        sp.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    protected JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc().fillHorizontal().weight(1.0, 0.0)
            .constraints();
        panel.add(new JButton(
            new AddAction()), gbc(gc).cell(0, 0).constraints());
        RemoveAction actRemove;
        panel.add(new JButton(actRemove = new RemoveAction()),
            gbc(gc).cell(0, 1).constraints());
        UpAction actUp;
        panel.add(new JButton(actUp = new UpAction()),
            gbc(gc).cell(0,2).constraints());
        DownAction actDown;
        panel.add(new JButton(actDown =new DownAction()),
            gbc(gc).cell(0,3).constraints());
        panel.add(new JPanel(),
            gbc().cell(0, 4).fillboth().weight(1.0, 1.0).constraints());

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
        add(buildInfoPanel(), gbc().cell(0,0,2,1).fillHorizontal()
            .weight(1.0, 0.0).insets(insets).constraints());
        add(p2, gbc().cell(0,1).fillVertical()
            .weight(0.0,1.0).insets(insets).constraints());
        add(p1, gbc().cell(1,1).fillboth()
            .weight(1.0,1.0).insets(insets).constraints());
    }

    public CommonJSRepoConfigurationPanel() {
        build();
    }

    static public class RepositoriesListModel extends AbstractListModel<URL>
            implements PreferenceKeys {

        private final List<URL> repositories = new ArrayList<>();
        private final DefaultListSelectionModel selectionModel;

        public RepositoriesListModel(DefaultListSelectionModel selectionModel) {
            loadFromModuleRepositories();
            this.selectionModel = selectionModel;
        }

        public void loadFromModuleRepositories() {
            CommonJSModuleRepositoryRegistry.getInstance().getUserDefinedRepositories()
                .stream()
                .map(repo -> {
                    try {
                        return repo.getBaseURI().toURL();
                    } catch (MalformedURLException e) {
                        logger.log(Level.WARNING,
                            "failed to convert CommonJS module base URI to URL",
                            e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(repositories::add);
        }

        public void saveToModuleRepositories() {
            final List<ICommonJSModuleRepository> repos = repositories.stream()
                .map(url -> {
                    try {
                        return CommonJSModuleRepositoryFactory
                            .getInstance()
                            .build(url.toURI());
                    } catch(URISyntaxException | IllegalCommonJSModuleBaseURI e){
                        final String message = String.format(
                              "failed to create CommonJS module repository for "
                            + "url %s. Ignoring it.",
                            url
                        );
                        logger.log(Level.WARNING, message, e);
                        return null;
                    }
                })
                .collect(Collectors.toList());

            CommonJSModuleRepositoryRegistry.getInstance()
                .setUserDefinedRepositories(repos);
        }

        public void remove(int i) {
            repositories.remove(i);
            fireIntervalRemoved(this,i,i);
        }

        public void add(final URL url) {
            repositories.add(url);
            fireIntervalAdded(this, 0, getSize());
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

        @Override
        public URL getElementAt(int index) {
            return repositories.get(index);
        }

        @Override
        public int getSize() {
            return repositories.size();
        }
    }

    static public class RepositoryCellRenderer extends JLabel
            implements ListCellRenderer<URL> {

        private final Icon jarIcon;
        private final Icon dirIcon;

        public RepositoryCellRenderer() {
            setOpaque(true);
            jarIcon = ImageProvider.get("jar");
            dirIcon = ImageProvider.get("directory");
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends URL> list, URL url, int index,
                boolean isSelected, boolean hasFocus) {
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
            putValue(Action.SHORT_DESCRIPTION,
                    tr("Add an additional repository"));
            putValue(Action.SMALL_ICON, ImageProvider.get("dialogs", "add",
                ImageProvider.ImageSizes.SMALLICON));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            ModuleRepositoryDialog dialog = new ModuleRepositoryDialog(
                    CommonJSRepoConfigurationPanel.this);
            dialog.setVisible(true);
            final ICommonJSModuleRepository repository = dialog.getRepository();
            if (repository != null) {
                try {
                    final URL url = repository.getBaseURI().toURL();
                    mdlRepositories.add(url);
                } catch(MalformedURLException e) {
                    // should not happen
                    logger.log(Level.WARNING, "", e);
                }
            }
        }
    }

    private class RemoveAction extends AbstractAction
            implements ListSelectionListener {

        public RemoveAction() {
            putValue(SHORT_DESCRIPTION, tr("Remove a repository"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete",
                ImageProvider.ImageSizes.SMALLICON));
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

    private class UpAction extends AbstractAction
            implements ListSelectionListener {

        public UpAction() {
            putValue(Action.SHORT_DESCRIPTION,
                tr("Move the selected repository up by one position"));
            putValue(Action.SMALL_ICON,
                ImageProvider.get("dialogs/conflict", "moveup",
                    ImageProvider.ImageSizes.SMALLICON));
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

    private class DownAction extends AbstractAction
            implements ListSelectionListener {

        public DownAction() {
            putValue(Action.SHORT_DESCRIPTION,
                tr("Move the selected repository down by one position"));
            putValue(Action.SMALL_ICON,
                ImageProvider.get("dialogs/conflict", "movedown",
                    ImageProvider.ImageSizes.SMALLICON));
            updateEnabledState();
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            int selIdx = lstRepositories.getSelectedIndex();
            mdlRepositories.down(selIdx);
        }

        protected void updateEnabledState() {
            int selIdx = lstRepositories.getSelectedIndex();
            setEnabled(selIdx != -1
                && selIdx != lstRepositories.getModel().getSize() - 1);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            updateEnabledState();
        }
    }

    /**
     * Persist the configured values to preferences
     */
    public void persistToPreferences() {
        // will also persist the configured CommonJS module base URIs to
        // preferences
        mdlRepositories.saveToModuleRepositories();
    }
}
