package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.graalvm.ICommonJSModuleRepository;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

public abstract class AbstractRepoConfigurationPanel extends JPanel  {
    private static final Logger logger = Logger.getLogger(AbstractRepoConfigurationPanel.class.getName());

    protected RepositoriesListModel mdlRepositories;
    protected JList<URL> lstRepositories;

    /* ------------------------------------------------------------------------------------ */
    /* abstract methods to be implemented in subclasses                                     */
    /* ------------------------------------------------------------------------------------ */
    protected abstract JPanel buildInfoPanel();

    protected abstract RepositoriesListModel buildRepositoriesListModel(@NotNull final ListSelectionModel selectionModel);

    public abstract void persistToPreferences();
    /* ------------------------------------------------------------------------------------ */

    protected JPanel buildTablePanel() {
        final var panel = new JPanel(new BorderLayout());
        final var selectionModel = new DefaultListSelectionModel();
        mdlRepositories = buildRepositoriesListModel(selectionModel);
        lstRepositories = new JList<>(mdlRepositories);
        lstRepositories.setCellRenderer(new RepositoryCellRenderer());
        lstRepositories.setSelectionModel(selectionModel);
        final var sp = new JScrollPane(lstRepositories);
        sp.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    protected JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = gbc().fillHorizontal().weight(1.0, 0.0).constraints();
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

    public AbstractRepoConfigurationPanel() {
        build();
    }

    static public class RepositoryCellRenderer extends JLabel
            implements ListCellRenderer<URL> {

        private final Icon jarIcon;
        private final Icon dirIcon;

        public RepositoryCellRenderer() {
            setOpaque(true);
            jarIcon = ImageProvider.get("jar", ImageProvider.ImageSizes.SMALLICON);
            dirIcon = ImageProvider.get("directory",  ImageProvider.ImageSizes.SMALLICON);
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
                    AbstractRepoConfigurationPanel.this);
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

}
