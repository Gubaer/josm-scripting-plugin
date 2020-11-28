package org.openstreetmap.josm.plugins.scripting.preferences.jython;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.util.WindowGeometry;

public class SysPathsEditorPanel extends JPanel {
    static private Logger logger = Logger.getLogger(
            SysPathsEditorPanel.class.getName());

    static private Icon saveImageGet(String name) {
        ImageIcon icon = ImageProvider.getIfAvailable(name);
        if (icon == null) {
           logger.warning(tr("Failed to load icon ''{0}''", name));
        }
        return icon;
    }

    static private Icon saveImageGet(String dir, String name) {
        ImageIcon icon = ImageProvider.getIfAvailable(dir, name);
        if (icon == null) {
           logger.warning(tr("Failed to load icon ''{0}/{1}''", dir, name));
        }
        return icon;
    }

    private JList<File> lstPaths;
    private SysPathsModel mdlPaths;

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
        lstPaths = new JList<>(mdlPaths = new SysPathsModel(selectionModel));
        lstPaths.setCellRenderer(new SysPathCellRenderer());
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

    public SysPathsEditorPanel() {
        build();
    }

    /**
     * Replies the sys paths model used by this editor
     *
     * @return
     */
    public SysPathsModel getModel() {
        return mdlPaths;
    }

    static public class SysPathsModel extends AbstractListModel<File> implements PreferenceKeys {
        @SuppressWarnings("unused")
        static private final Logger logger = Logger.getLogger(SysPathsModel.class.getName());

        private final List<File> paths = new ArrayList<>();
        private DefaultListSelectionModel selectionModel;

        public SysPathsModel(DefaultListSelectionModel selectionModel) {
            this.selectionModel = selectionModel;
        }

        /**
         * Sets the paths this model manages
         *
         * @param paths the paths
         */
        public void setPaths(Collection<String> paths) {
            this.paths.clear();
            paths.stream()
                .filter(path -> path != null)
                .map(String::trim)
                .map(File::new)
                .collect(Collectors.toCollection(() -> this.paths));
            fireContentsChanged(this, 0, this.paths.size());
        }

        /**
         * Replies a list of the paths managed by this model.
         *
         * @return the paths
         */
        public List<String> getPaths() {
            return paths.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
        }

        /**
         * Loads the paths from the preferences with key <code>key</code>
         *
         * @param prefs the preferences
         * @param key  the preference key
         */
        public void loadFromPreferences(@NotNull Preferences prefs,
                String key) {
            Objects.requireNonNull(prefs);
            prefs.getList(key).stream()
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(File::new)
                .collect(Collectors.toCollection(() -> paths));
        }

        public void loadFromPreferences(Preferences prefs) {
            loadFromPreferences(prefs, PREF_KEY_JYTHON_SYS_PATHS);
        }

        /**
         * Saves the paths to the preferences
         *
         * @param prefs the preferences
         * @param key the preference key
         */
        public void persistToPreferences(Preferences prefs, String key) {
            List<String> entries = paths.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            prefs.putList(key, entries);
        }

        /**
         * Saves the paths to the preferences
         *
         * @param prefs the preferences
         */
        public void persistToPreferences(Preferences prefs) {
            persistToPreferences(prefs, PREF_KEY_JYTHON_SYS_PATHS);
        }

        public void remove(int i) {
            paths.remove(i);
            fireIntervalRemoved(this,i,i);
        }


        public void up(int i) {
            File tomove = paths.remove(i);
            paths.add(i-1, tomove);
            selectionModel.setSelectionInterval(i-1, i-1);
            fireContentsChanged(this,0,getSize());
        }

        public void down(int i) {
            File tomove = paths.remove(i);
            paths.add(i+1, tomove);
            selectionModel.setSelectionInterval(i+1, i+1);
            fireContentsChanged(this,0,getSize());
        }

        public void add(File path) {
            if (path == null) return;
            paths.add(path);
            fireContentsChanged(this,0,getSize());
        }

        @Override
        public File getElementAt(int index) {
            return paths.get(index);
        }

        @Override
        public int getSize() {
            return paths.size();
        }
    }

    static public class SysPathCellRenderer extends JLabel
        implements ListCellRenderer<File> {
        @SuppressWarnings("unused")
        static private final Logger logger =
                Logger.getLogger(SysPathCellRenderer.class.getName());

        private Icon jarIcon;
        private Icon dirIcon;


        public SysPathCellRenderer() {
            setOpaque(true);
            jarIcon = saveImageGet("jar");
            dirIcon = saveImageGet("directory");
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends File> list,
                File path,
                int index, boolean isSelected, boolean hasFocus) {
            setText(path.getAbsolutePath());
            if (isSelected) {
                setForeground(UIManager.getColor("List.selectionForeground"));
                setBackground(UIManager.getColor("List.selectionBackground"));
            } else {
                setForeground(UIManager.getColor("List.foreground"));
                setBackground(UIManager.getColor("List.background"));
            }
            if (path.isDirectory()) {
                setIcon(dirIcon);
            } else if (path.isFile() && path.getName().endsWith(".jar")) {
                setIcon(jarIcon);
            } else {
                setIcon(null);
            }
            return this;
        }
    }

    private class AddAction extends AbstractAction {

        public AddAction() {
            putValue(Action.NAME, tr("Add"));
            putValue(Action.SHORT_DESCRIPTION, tr("Add a path or jar file"));
            putValue(Action.SMALL_ICON, saveImageGet("dialogs", "add"));
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            SysPathDialog dialog = new SysPathDialog(SysPathsEditorPanel.this);
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

    static public class SysPathDialog extends JDialog {

        @SuppressWarnings("unused")
        static private final Logger logger = Logger.getLogger(
                SysPathDialog.class.getName());

        private JTextField tfPath;
        private OKAction actOK;
        private File path;

        protected JPanel buildInfoPanel() {
            HtmlPanel info = new HtmlPanel();
            info.setText(
                    "<html>"
                    + tr("Please enter or paste a valid directory path or path to" +
                        " a jar/zip file."
                    )
                    + "</html>"
            );
            return info;
        }

        protected class DocumentAdapter implements DocumentListener {
            @Override
            public void changedUpdate(DocumentEvent e) {validatePath();};
            @Override
            public void insertUpdate(DocumentEvent e) {validatePath();};
            @Override
            public void removeUpdate(DocumentEvent e) {validatePath();};
        }

        protected JPanel buildEntryPanel() {
            JPanel pnl = new JPanel(new GridBagLayout());
            pnl.add(new JLabel("Path:"), gbc().cell(0, 0)
                    .anchor(GridBagConstraints.WEST)
                    .insets(0,2,0,2).constraints());
            pnl.add(tfPath = new JTextField(), gbc().cell(1,0).fillboth()
                    .weightx(1.0).insets(0,2,0,2).constraints());
            pnl.add(new JButton(new LookupFileAction()), gbc().cell(2, 0)
                    .insets(0,2,0,2).constraints());

            tfPath.getDocument().addDocumentListener(new DocumentAdapter());
            return pnl;
        }

        protected JPanel buildCommandPanel() {
            JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
            pnl.add(new JButton(actOK = new OKAction()));
            pnl.add(new JButton(new CancelAction()));
            return pnl;
        }

        protected void build() {
            Container content = getContentPane();
            content.setLayout(new BorderLayout());
            JPanel pnl = new JPanel(new BorderLayout());
            pnl.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            pnl.add(buildInfoPanel(), BorderLayout.NORTH);
            pnl.add(buildEntryPanel(), BorderLayout.CENTER);
            pnl.add(buildCommandPanel(), BorderLayout.SOUTH);
            content.add(pnl, BorderLayout.CENTER);
            setTitle(tr("Create or edit a path"));
            setIconImage(ImageProvider.get("script-engine").getImage());
        }

        public void setPath(File repo) {
            tfPath.setText("");
            if (repo != null) {
                tfPath.setText(repo.getAbsolutePath());
            }
            this.path = repo;
            validatePath();
        }

        public File getPath() {
            return path;
        }

        public SysPathDialog(Component parent){
            super(JOptionPane.getFrameForComponent(parent),
                    ModalityType.DOCUMENT_MODAL);
            build();
            validatePath();
        }

        protected boolean isExistingJarFile(File f) {
            try (JarFile jar = new JarFile(f)) {
                return true;
            } catch(IOException e) {
                return false;
            }
        }

        protected void validatePath() {
            boolean valid = true;
            String s = tfPath.getText().trim();
            String msg = "";
            File f = new File(s);
            if (f.isFile()) {
                valid = isExistingJarFile(f);
                if (!valid) {
                    msg = tr("''{0}'' isn''t an existing jar file", s);
                }
            } else if (f.isDirectory()) {
                valid =  true;
            } else {
                msg = tr("''{0}'' is neither an existing directory nor an existing jar file",s);
                valid = false;
            }

            if (valid){
                tfPath.setBackground(UIManager.getColor("TextField.background"));
                actOK.setEnabled(!s.isEmpty());
            } else {
                tfPath.setBackground(new Color(255, 199, 210));
                actOK.setEnabled(false);
            }
            tfPath.setToolTipText(msg);
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                WindowGeometry.centerInWindow(getParent(),new Dimension(600,150)).applySafe(this);
            }
            super.setVisible(visible);
        }

        private class LookupFileAction extends AbstractAction {

            public LookupFileAction() {
                putValue(Action.NAME, "...");
                putValue(Action.SHORT_DESCRIPTION, tr("Select a directory or a jar file"));
            }
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser fc = new JFileChooser();
                File dir = getCurrentDirectory();
                if (dir != null) {
                    fc.setCurrentDirectory(dir);
                }
                fc.setDialogTitle(tr("Select a directory or jar/zip file"));
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setMultiSelectionEnabled(false);
                int answer = fc.showOpenDialog(SysPathDialog.this);
                if (answer != JFileChooser.APPROVE_OPTION) return;
                File f = fc.getSelectedFile();
                tfPath.setText(f.getAbsolutePath());
            }

            protected File getCurrentDirectory() {
                String s = tfPath.getText().trim();
                if (s.isEmpty()) return new File(".");
                File f = new File(s);
                if (f.isDirectory()) return f;
                return f.getParentFile();
            }
        }

        private class CancelAction extends AbstractAction {
            public CancelAction() {
                putValue(Action.NAME, tr("Cancel"));
                putValue(Action.SMALL_ICON, ImageProvider.getIfAvailable("cancel"));
            }
            @Override
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        }


        private class OKAction extends AbstractAction {
            public OKAction() {
                putValue(Action.NAME, tr("OK"));
                putValue(Action.SMALL_ICON, ImageProvider.getIfAvailable("ok"));

            }
            @Override
            public void actionPerformed(ActionEvent evt) {
                String s = tfPath.getText().trim();
                path = new File(s);
                setVisible(false);
            }
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
