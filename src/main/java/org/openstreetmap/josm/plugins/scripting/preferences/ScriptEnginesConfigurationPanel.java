package org.openstreetmap.josm.plugins.scripting.preferences;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.DownloadFileTask;
import org.openstreetmap.josm.gui.util.CellEditorSupport;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.model.JSR223ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineCellRenderer;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys.PREF_KEY_SCRIPTING_ENGINE_JARS;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * <p><strong>ScriptEnginesConfigurationPanel</strong> allows to configure
 * the script engines available in JOSM.</p>
 *
 */
@SuppressWarnings("unused")
public class ScriptEnginesConfigurationPanel extends VerticallyScrollablePanel{
    static private final Logger logger =
        Logger.getLogger(ScriptEnginesConfigurationPanel.class.getName());

    private final static String RES_SCRIPT_ENGINE_JARS
        = "/resources/script-engine-jars.properties";

    /**
     * Describes the jar file for scripting engine which JOSM can load
     * download and install automatically
     */
    @SuppressWarnings("unused")
    public static class EngineJarDescriptor {
        private final String name;
        private final String downloadUrl;
        EngineJarDescriptor(String name, String downloadUrl){
            this.name = name;
            this.downloadUrl = downloadUrl;
        }

        /**
         * Replies a file object representing a local copy of the jar
         * file for this script engine.
         *
         * @return the file; null, if the local name can't be derived from
         *    the descriptor
         */
        public File getLocalJarFile() {
            if (downloadUrl == null) return null;
            if (downloadUrl.lastIndexOf("/") < 0) return null;
            return new File(
                ScriptingPlugin.getInstance().getPluginDirs().getUserDataDirectory(false),
                downloadUrl.substring(downloadUrl.lastIndexOf("/"))
            );
        }

        /**
         * Replies the local path of the engine jar file, relative to the
         * home directory of the scripting plugin
         * @return the relative plugin path
         */
        public String getRelativePluginPath() {
            if (downloadUrl == null) return null;
            return ScriptingPlugin.getInstance()
                    .getPluginInformation().getName()
                    + "/" + getLocalJarFile();
        }

        /**
         * Replies true if the local jar file exists and can be read; false
         * otherwise
         *
         */
        public boolean hasLocalJarFile() {
            File jar = getLocalJarFile();
            if (jar == null) {
                logger.warning(String.format(
                    "no jar file for scripting engine '%s' locally available",
                    getName()
                ));
                return false;
            }
            return jar.isFile() && jar.canRead();
        }

        public String getName() {
            return name;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }
    }


    private static List<EngineJarDescriptor> downloadableEngines = null;

    /**
     * Replies the unmodifiable list of downloadable engines
     */
    public static List<EngineJarDescriptor> getDownloadableEngines() {
        if (downloadableEngines == null) {
            readDownloadableEngines();
        }
        return Collections.unmodifiableList(downloadableEngines);
    }

    public static EngineJarDescriptor getEngineDescriptor(String name) {
        return getDownloadableEngines().stream()
            .filter(desc -> name.equalsIgnoreCase(desc.getName()))
            .findFirst()
            .orElse(null);
    }


    private static void readDownloadableEngines() {
        downloadableEngines = new ArrayList<>();

        final Properties prop = new Properties();
        try (InputStream in = ScriptEnginesConfigurationPanel.class
                .getResourceAsStream(RES_SCRIPT_ENGINE_JARS)) {
            if (in == null) {
                logger.severe(tr("resource file ''{0}'' not found",
                RES_SCRIPT_ENGINE_JARS));
            }

            prop.load(in);
        } catch(IOException e) {
            logger.log(Level.SEVERE,
                tr("failed to load resource file ''{0}''",
                RES_SCRIPT_ENGINE_JARS), e);
        }
        String value = prop.getProperty("engines");
        if (value == null) {
            logger.warning(tr(
                "property ''{0}'' in resource file ''{1}'' not found",
                "engines", RES_SCRIPT_ENGINE_JARS));
            return;
        }
        String[] engines = value.split(",");
        for (String engine: engines) {
            engine = engine.trim().toLowerCase();
            String name = prop.getProperty(engine + ".name");
            String url = prop.getProperty(engine + ".download-url");
            if (name == null || url == null) continue;
            name = name.trim();
            url = url.trim();
            if (name.isEmpty() || url.isEmpty()) continue;
            EngineJarDescriptor desc = new EngineJarDescriptor(name, url);
            downloadableEngines.add(desc);
        }
    }

    private ScriptEngineJarTableModel model;
    private JTable tblJarFiles;
    private JComboBox<String> cbDownloadableEngines;

    public ScriptEnginesConfigurationPanel() {
        if (downloadableEngines == null) readDownloadableEngines();
        build();
        model.restoreFromPreferences();
    }

    protected JPanel buildScriptEnginesInfoPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        JList<ScriptEngineDescriptor> lstEngines =
                new JList<>(JSR223ScriptEngineProvider
                    .getInstance());
        lstEngines.setCellRenderer(new ScriptEngineCellRenderer());
        lstEngines.setVisibleRowCount(3);
        pnl.add(lstEngines, BorderLayout.CENTER);
        lstEngines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return pnl;
    }

    protected JPanel buildScriptEngineJarsPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3,3,3,3),
                BorderFactory.createTitledBorder(tr("JAR files"))
            )
        );

        GridBagConstraints gc = new GridBagConstraints();

        HtmlPanel info = new HtmlPanel();
        info.setText(
            "<html>"
            + tr("Enter additional JAR files which provide script engines.")
            + "</html>"
        );
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gc.fill = GridBagConstraints.BOTH;
        pnl.add(info, gc);

        model = new ScriptEngineJarTableModel();
        tblJarFiles= new JTable(model, new ColumnModel());
        tblJarFiles.setSelectionModel(model.getSelectionModel());
        tblJarFiles.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblJarFiles.setRowHeight(new JButton("...").getPreferredSize().height);

        JScrollPane jp = new JScrollPane(tblJarFiles);
        jp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jp.setMinimumSize(new Dimension(0, 100));

        gc.gridx = 0; gc.gridy = 1;
        gc.weightx = 1.0; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        pnl.add(jp, gc);

        JPanel ctrlPanel = new JPanel(new BorderLayout());
        JPanel p = new JPanel(new FlowLayout());
        AddJarAction actAdd = new AddJarAction();
        p.add(new JButton(actAdd));
        RemoveJarAction actDelete = new RemoveJarAction();
        p.add(new JButton(actDelete));
        ctrlPanel.add(p, BorderLayout.WEST);
        p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(new JLabel(tr("Available engines:")));
        p.add(cbDownloadableEngines = new JComboBox<>());
        for (EngineJarDescriptor desc: downloadableEngines) {
            cbDownloadableEngines.addItem(desc.getName());
        }
        p.add(new JButton(new DownloadEngineAction()));
        ctrlPanel.add(p, BorderLayout.EAST);

        model.getSelectionModel().addListSelectionListener(actDelete);
        tblJarFiles.getActionMap().put("deleteSelection", actDelete);
        tblJarFiles.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_DELETE, 0),"deleteSelection");
        tblJarFiles.getActionMap().put("insertRow", actAdd);
        tblJarFiles.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_INSERT, 0),"insertRow");
        gc.gridx = 0; gc.gridy = 2;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        pnl.add(ctrlPanel, gc);
        return pnl;
    }

    protected JPanel buildScriptEnginesPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3,3,3,3),
                BorderFactory.createTitledBorder(tr("Available script engines"))
            )
        );
        GridBagConstraints gc = new GridBagConstraints();

        HtmlPanel info = new HtmlPanel();
        info.setText(
            "<html>"
            + tr("JOSM currently supports the following script engines:")
            + "</html>"
        );
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3,3,3,3);
        pnl.add(info, gc);

        gc.gridx = 0; gc.gridy = 1;
        gc.weightx = 1.0; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(3,3,3,3);
        pnl.add(buildScriptEnginesInfoPanel(), gc);
        return pnl;
    }

    protected void build() {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(buildScriptEnginesPanel(), gc);

        gc.gridx = 0; gc.gridy = 1;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        add(buildScriptEngineJarsPanel(), gc);

        // filler
        gc.gridx = 0; gc.gridy = 2;
        gc.weightx = 1.0; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gc);
    }

    public void persistToPreferences() {
        model.persistToPreferences();
    }

    public void restoreFromPreferences() {
        model.restoreFromPreferences();
    }

    static private class ColumnModel extends DefaultTableColumnModel {
        public ColumnModel() {
            TableColumn tc;
            ScriptEngineJarCellRenderer renderer =
                new ScriptEngineJarCellRenderer();

            tc = new TableColumn(0);
            tc.setHeaderValue("");
            tc.setMaxWidth(30);
            tc.setPreferredWidth(30);
            tc.setMinWidth(30);
            tc.setResizable(false);
            tc.setCellRenderer(renderer);
            addColumn(tc);

            tc = new TableColumn(1);
            tc.setHeaderValue(tr("JAR file"));
            tc.setCellRenderer(renderer);
            tc.setCellEditor(new JarFileNameEditor());
            tc.setResizable(true);
            addColumn(tc);
        }
    }

    static private class ScriptEngineJarCellRenderer extends JLabel implements TableCellRenderer {

        public ScriptEngineJarCellRenderer(){
            setOpaque(true);
        }

        protected void reset() {
            setIcon(null);
            setText("");
            setForeground(UIManager.getColor("Table.foreground"));
            setBackground(UIManager.getColor("Table.background"));
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        protected void renderColors(boolean selected){
            if (!selected){
                setForeground(UIManager.getColor("Table.foreground"));
                setBackground(UIManager.getColor("Table.background"));
            } else {
                setForeground(UIManager.getColor("Table.selectionForeground"));
                setBackground(UIManager.getColor("Table.selectionBackground"));
            }
        }

        protected void renderJarName(ScriptEngineJarInfo jar) {
            String fileName = jar.getJarFilePath();
            File f = new File(fileName.trim());
            File parent = f.getParentFile();
            if (parent != null){
                String parentName= parent.getName();
                if (parentName.length() > 15){
                    setText(parentName.substring(0, 10) + "..."
                            + File.pathSeparator + f.getName());
                } else {
                    setText(f.toString());
                }
            } else {
                setText(f.toString());
            }
        }

        protected void renderJarStatus(ScriptEngineJarInfo jar){
            setHorizontalAlignment(SwingConstants.CENTER);
            String msg = jar.getStatusMessage();
            if (jar.getJarFilePath().trim().isEmpty()) {
                setIcon(null);
                setToolTipText("");
            } else if (msg.equals(ScriptEngineJarInfo.OK_MESSAGE)){
                setIcon(ImageProvider.get("valid"));
                setToolTipText("");
            } else {
                setIcon(ImageProvider.get("error"));
                setToolTipText(msg);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            ScriptEngineJarInfo jar = (ScriptEngineJarInfo)value;
            // shouldn't happen, but see #6 reported on a Mac
            reset();
            if (jar == null) return this;
            switch(column){
            case 0: renderJarStatus(jar); break;
            case 1:
                renderColors(isSelected);
                renderJarName(jar);
                break;
            }
            return this;
        }
    }

    private class RemoveJarAction extends AbstractAction
        implements ListSelectionListener{

        public RemoveJarAction() {
            putValue(NAME, tr("Remove"));
            putValue(SHORT_DESCRIPTION, tr("Remove the selected jar files"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs","delete",
                ImageProvider.ImageSizes.SMALLICON));
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            model.deleteSelected();
        }

        public void updateEnabledState() {
            setEnabled(!model.getSelectionModel().isSelectionEmpty());
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateEnabledState();
        }
    }

    private class AddJarAction extends AbstractAction {

        public AddJarAction() {
            putValue(NAME, tr("Add"));
            putValue(SHORT_DESCRIPTION,
                tr("Add a jar file providing a script engine"));
            putValue(SMALL_ICON, ImageProvider.get("add",
                ImageProvider.ImageSizes.SMALLICON));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            model.addNew();
            tblJarFiles.editCellAt(tblJarFiles.getRowCount()-1, 1);
        }
    }

    private class DownloadEngineAction extends AbstractAction {

        public DownloadEngineAction() {
            putValue(
                SHORT_DESCRIPTION,
                tr("Download and install scripting engine"));
            putValue(SMALL_ICON, ImageProvider.get("download",
                ImageProvider.ImageSizes.SMALLICON));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = (String)cbDownloadableEngines.getSelectedItem();
            if (name == null) return;
            final EngineJarDescriptor desc = getEngineDescriptor(name);
            if (desc == null) return;

            //TODO: DownloadFileTask doesn't follow redirects and saves the
            // error message for the 30x status as jar file
            // Either fix DownloadFileTask or replace it with a more robust
            // implementation for this plugin.
            final DownloadFileTask downloadFileTask = new DownloadFileTask(
                    MainApplication.getMainFrame(),
                    desc.getDownloadUrl(),
                    desc.getLocalJarFile(),
                    true  /* mkdir */,
                    false /* unzip */
            );
            final Future<?> future = MainApplication.worker.submit(downloadFileTask);
            MainApplication.worker.submit(() -> {
                    try {
                        future.get();
                    } catch(InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        return;
                    }
                    ArrayList<String> jars = new ArrayList<>(Preferences.main()
                            .getList(PREF_KEY_SCRIPTING_ENGINE_JARS));
                    if (!jars.contains(desc.getLocalJarFile().toString())){
                        jars.add(desc.getLocalJarFile().toString());
                        Preferences.main().putList(
                                PREF_KEY_SCRIPTING_ENGINE_JARS, jars);
                    }
                    //refresh on the UI thread
                    SwingUtilities.invokeLater(() ->
                        model.restoreFromPreferences()
                    );
            });
        }
    }

    private static class JarFileNameEditor extends JPanel implements TableCellEditor {
        @SuppressWarnings("unused")
        static private final Logger logger = Logger.getLogger(
            JarFileNameEditor.class.getName());

        private JTextField tfJarFile;
        private JButton btnLauchFileChooser;
        private final CellEditorSupport tableCellEditorSupport;
        private ScriptEngineJarInfo info;

        public JarFileNameEditor() {
            tableCellEditorSupport = new CellEditorSupport(this);
            build();
        }

        protected void build() {
            setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0; gc.gridy = 0;
            gc.weightx = 1.0; gc.weighty = 1.0;
            gc.fill = GridBagConstraints.BOTH;
            add(tfJarFile = new JTextField(), gc);
            SelectAllOnFocusGainedDecorator.decorate(tfJarFile);

            gc.gridx = 1; gc.gridy = 0;
            gc.weightx = 0.0; gc.weighty = 1.0;
            gc.fill = GridBagConstraints.VERTICAL;
            add(btnLauchFileChooser = new JButton(
                new LaunchFileChooserAction()), gc
            );
        }

        public void addCellEditorListener(CellEditorListener l) {
            tableCellEditorSupport.addCellEditorListener(l);
        }

        public void cancelCellEditing() {
            tfJarFile.setText(info.getJarFilePath());
            tableCellEditorSupport.fireEditingCanceled();
        }

        public Object getCellEditorValue() {
            return tfJarFile.getText();
        }

        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent)anEvent).getClickCount() == 2;
            }
            return false;
        }

        public void removeCellEditorListener(CellEditorListener l) {
            tableCellEditorSupport.removeCellEditorListener(l);
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        public boolean stopCellEditing() {
            tableCellEditorSupport.fireEditingStopped();
            return true;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {
            info = (ScriptEngineJarInfo)value;
            tfJarFile.setText(info.getJarFilePath());
            tfJarFile.selectAll();
            return this;
        }

        private class LaunchFileChooserAction extends AbstractAction {
            public LaunchFileChooserAction() {
                putValue(NAME, "...");
                putValue(SHORT_DESCRIPTION, tr("Launch file chooser"));
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String fileName = tfJarFile.getText().trim();
                File currentFile = null;
                if (! fileName.isEmpty()) {
                    currentFile = new File(fileName);
                }
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(tr("Select a jar file"));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                if (currentFile != null){
                    chooser.setCurrentDirectory(currentFile);
                    chooser.setSelectedFile(currentFile);
                }
                int ret = chooser.showOpenDialog(btnLauchFileChooser);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    currentFile = chooser.getSelectedFile();
                    tfJarFile.setText(currentFile.toString());
                    stopCellEditing();
                } else {
                    tfJarFile.requestFocusInWindow();
                }
            }
        }
    }
}
