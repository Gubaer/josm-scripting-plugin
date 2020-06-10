package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.ICommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.graalvm.IllegalCommonJSModuleBaseURI;
import org.openstreetmap.josm.plugins.scripting.model.CommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.ui.EditorPaneBuilder;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.tools.I18n.tr;

@SuppressWarnings("serial")
public class ModuleRepositoryDialog extends JDialog {

    @SuppressWarnings("unused")
    static private final Logger logger =
        Logger.getLogger(ModuleRepositoryDialog.class.getName());

    private JTextField tfRepositoryUrl;
    private OKAction actOK;
    private ICommonJSModuleRepository repository;

    protected JPanel buildInfoPanel() {
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String text =
            "<html>"
                + tr("Please enter or paste a valid file or jar URL. "
                + "HTTP URLs are not supported."
            )
            + "</html>";
        pane.setText(text);
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    protected class DocumentAdapter implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {validateRepository();}
        @Override
        public void insertUpdate(DocumentEvent e) {validateRepository();}
        @Override
        public void removeUpdate(DocumentEvent e) {validateRepository();}
    }

    protected JPanel buildEntryPanel() {
        final JPanel pnl = new JPanel(new GridBagLayout());
        pnl.add(new JLabel("Repository URL:"), gbc().cell(0, 0)
                .anchor(GridBagConstraints.WEST).insets(0,2,0,2)
                .constraints());
        pnl.add(tfRepositoryUrl = new JTextField(), gbc().cell(1,0).fillboth()
                .weightx(1.0).insets(0,2,0,2).constraints());
        pnl.add(new JButton(new LookupFileAction()), gbc().cell(2, 0)
                .insets(0,2,0,2).constraints());

        tfRepositoryUrl.getDocument().addDocumentListener(new DocumentAdapter());
        return pnl;
    }

    protected JPanel buildCommandPanel() {
        final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnl.add(new JButton(actOK = new OKAction()));
        pnl.add(new JButton(new CancelAction()));
        return pnl;
    }

    protected void build() {
        final Container content = getContentPane();
        content.setLayout(new BorderLayout());
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        pnl.add(buildInfoPanel(), BorderLayout.NORTH);
        pnl.add(buildEntryPanel(), BorderLayout.CENTER);
        pnl.add(buildCommandPanel(), BorderLayout.SOUTH);
        content.add(pnl, BorderLayout.CENTER);
        setTitle(tr("Create or edit a CommonJS module repository"));
        setIconImage(ImageProvider.get("script-engine").getImage());
    }

    public void setRepository(ICommonJSModuleRepository repo) {
        tfRepositoryUrl.setText("");
        if (repo != null) {
            tfRepositoryUrl.setText(repo.getBaseURI().toString());
        }
        this.repository = repo;
        validateRepository();
    }

    public ICommonJSModuleRepository getRepository() {
        return repository;
    }

    public ModuleRepositoryDialog(Component parent){
        super(JOptionPane.getFrameForComponent(parent),
                ModalityType.DOCUMENT_MODAL);
        build();
        validateRepository();
    }

    protected boolean isExistingJarFile(File f) {
        try(final JarFile ignored = new JarFile(f)) {
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    static private final Color BG_COLOR_ERROR = new Color(255, 199, 210);
    static private final Color BG_COLOR_VALID = UIManager.getColor(
            "TextField.background");

    protected void validateRepository() {
        boolean valid;
        final String repository = tfRepositoryUrl.getText().trim();
        String msg = "";
        if (repository.isEmpty()) {
            tfRepositoryUrl.setBackground(BG_COLOR_VALID);
            actOK.setEnabled(false);
            return;
        }
        URL url;
        try {
            url = new URL(repository);
        } catch(MalformedURLException e) {
            actOK.setEnabled(false);
            tfRepositoryUrl.setBackground(BG_COLOR_ERROR);
            tfRepositoryUrl.setToolTipText(
                tr("''{0}'' isn''t a valid URL", repository)
            );
            return;
        }

        switch(url.getProtocol()) {
        case "jar":
            try {
                final CommonJSModuleRepository repo =
                        new CommonJSModuleRepository(repository);
                final File f = repo.getFile();
                if (f.isDirectory()) {
                    valid = true;
                } else if (f.isFile()) {
                    valid = isExistingJarFile(f);
                    if (!valid) {
                        msg = tr("URL ''{0}'' doesn''t refer to an "
                                + "existing local jar file",repository);
                    }
                } else {
                    valid = false;
                    msg = tr("URL ''{0}'' doesn''t refer to an existing "
                            + "local directory or jar file",repository);
                }
            } catch(MalformedURLException e){
                e.printStackTrace();
                msg = tr("''{0}'' isn''t a valid URL");
                valid = false;
            }
            break;

        case "file":
            final File f = new File(url.getFile());
            if (f.isFile()) {
                valid = isExistingJarFile(f);
                if (!valid) {
                    msg = tr("''{0}'' isn''t an existing jar file", repository);
                }
            } else if (f.isDirectory()) {
                valid =  true;
            } else {
                msg = tr("''{0}'' is neither an existing directory nor an "
                        + "existing jar file",repository);
                valid = false;
            }
            break;

          default:
              msg = tr("''{0}'' is neither a ''jar'' nor a ''file'' URL",
                      repository);
              valid = false;
        }

        if (valid){
            tfRepositoryUrl.setBackground(BG_COLOR_VALID);
            actOK.setEnabled(!repository.isEmpty());
        } else {
            tfRepositoryUrl.setBackground(BG_COLOR_ERROR);
            actOK.setEnabled(false);
        }
        tfRepositoryUrl.setToolTipText(msg);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            WindowGeometry.centerInWindow(getParent(),
                    new Dimension(600,150)).applySafe(this);
        }
        super.setVisible(visible);
    }

    private class LookupFileAction extends AbstractAction {

        public LookupFileAction() {
            putValue(Action.NAME, "...");
            putValue(Action.SHORT_DESCRIPTION,
                    tr("Select a directory or a jar file"));
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
            int answer = fc.showOpenDialog(ModuleRepositoryDialog.this);
            if (answer != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            if (f.isDirectory()) {
                tfRepositoryUrl.setText(
                        new CommonJSModuleRepository(f).getURL().toString());
            } else if (f.isFile()) {
                try {
                    JarFile jar = new JarFile(f);
                    tfRepositoryUrl.setText(
                            new CommonJSModuleRepository(jar)
                                .getURL().toString());
                } catch (IOException e) {
                    try {
                        tfRepositoryUrl.setText(f.toURI().toURL().toString());
                    } catch(MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        protected File getCurrentDirectory() {
            String s = tfRepositoryUrl.getText().trim();
            if (s.isEmpty()) return new File(".");
            try {
                CommonJSModuleRepository repo = new CommonJSModuleRepository(s);
                File f = repo.getFile();
                if (f.isDirectory()) return f;
                if (f.isFile()) return f.getParentFile();
                return new File(".");
            } catch(MalformedURLException e) {
                // fall through
            }
            if (!s.matches("^[a-zA-Z]+:/")) {
                File f= new File(s);
                if (f.isDirectory()) return f;
                if (f.isFile()) return f.getParentFile();
            }
            return new File(".");
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(Action.NAME, tr("Cancel"));
            putValue(Action.SMALL_ICON, ImageProvider.get("cancel"));
        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            setVisible(false);
        }
    }

    private class OKAction extends AbstractAction {
        public OKAction() {
            putValue(Action.NAME, tr("OK"));
            putValue(Action.SMALL_ICON, ImageProvider.get("ok"));

        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            final String s = tfRepositoryUrl.getText().trim();
            try {
                repository = CommonJSModuleRepositoryFactory
                    .getInstance()
                    .build(s);
            } catch(IllegalCommonJSModuleBaseURI e) {
                logger.log(Level.SEVERE, "", e);
                //TODO(karl): display error message with an error dialog
                repository = null;
            }
            setVisible(false);
        }
    }
}
