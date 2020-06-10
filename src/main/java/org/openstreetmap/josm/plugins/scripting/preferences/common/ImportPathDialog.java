package org.openstreetmap.josm.plugins.scripting.preferences.common;

import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

public class ImportPathDialog extends JDialog {

    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(
            ImportPathDialog.class.getName());

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

    public ImportPathDialog(Component parent){
        super(JOptionPane.getFrameForComponent(parent),
                Dialog.ModalityType.DOCUMENT_MODAL);
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
            WindowGeometry.centerInWindow(getParent(),new Dimension(600,150))
                .applySafe(this);
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
            int answer = fc.showOpenDialog(ImportPathDialog.this);
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
