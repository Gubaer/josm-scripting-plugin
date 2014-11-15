package org.openstreetmap.josm.plugins.scripting.preferences;

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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.plugins.scripting.model.CommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

@SuppressWarnings("serial")
public class ModuleRepositoryDialog extends JDialog {
	
	@SuppressWarnings("unused")
	static private final Logger logger = Logger.getLogger(ModuleRepositoryDialog.class.getName());
	
	private JTextField tfRepositoryUrl;
	private OKAction actOK;
	private CommonJSModuleRepository repository;
	
	protected JPanel buildInfoPanel() {
		HtmlPanel info = new HtmlPanel();
		info.setText(
				"<html>"
				+ tr("Please enter or paste a valide file or jar URL. HTTP URLs are not supported."
				)
				+ "</html>"
		);
		return info;
	}
	
	protected class DocumentAdapter implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {validateRepository();};
		@Override
		public void insertUpdate(DocumentEvent e) {validateRepository();};
		@Override
		public void removeUpdate(DocumentEvent e) {validateRepository();};
	}
	
	protected JPanel buildEntryPanel() {
		JPanel pnl = new JPanel(new GridBagLayout());
		pnl.add(new JLabel("Repository URL:"), gbc().cell(0, 0).anchor(GridBagConstraints.WEST).insets(0,2,0,2).constraints());
		pnl.add(tfRepositoryUrl = new JTextField(), gbc().cell(1,0).fillboth().weightx(1.0).insets(0,2,0,2).constraints());
		pnl.add(new JButton(new LookupFileAction()), gbc().cell(2, 0).insets(0,2,0,2).constraints());
		
		tfRepositoryUrl.getDocument().addDocumentListener(new DocumentAdapter());
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
		setTitle(tr("Create or edit a CommonJS module repository"));
		setIconImage(ImageProvider.get("script-engine").getImage());
	}
	
	public void setRepository(CommonJSModuleRepository repo) {
		tfRepositoryUrl.setText("");
		if (repo != null) {
			tfRepositoryUrl.setText(repo.getURL().toString());
		}
		this.repository = repo;
		validateRepository();
	}
	
	public CommonJSModuleRepository getRepository() {
		return repository;
	}
		
	public ModuleRepositoryDialog(Component parent){
		super(JOptionPane.getFrameForComponent(parent), ModalityType.DOCUMENT_MODAL);
		build();
		validateRepository();
	}
	
	protected boolean isExistingJarFile(File f) {
		JarFile jar = null;
		try {
			jar = new JarFile(f);
			return true;
		} catch(IOException e) {
			return false;
		} finally {
			IOUtil.close(jar);
		}
	}
	
	protected void validateRepository() {
		boolean valid = true;
		String s = tfRepositoryUrl.getText().trim();
		String msg = "";
		if (!s.isEmpty()) {
			if (!s.matches("^[a-zA-Z]+:.*")) {
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
			} else {
				try {
					CommonJSModuleRepository repo = new CommonJSModuleRepository(s);
					File f = repo.getFile();
					if (f.isDirectory()) {
						valid = true;
					} else if (f.isFile()) {
						valid = isExistingJarFile(f);
						if (!valid) {
							msg = tr("URL ''{0}'' doesn''t refer to an existing local jar file",s);
						}
					} else {
						valid = false;
						msg = tr("URL ''{0}'' doesn''t refer to an existing local directory or jar file",s);
					}
				} catch(IllegalArgumentException e){
					e.printStackTrace();
					valid = false; 
				}
			}
		}		
		if (valid){
			tfRepositoryUrl.setBackground(UIManager.getColor("TextField.background"));
			actOK.setEnabled(!s.isEmpty());
		} else {
			tfRepositoryUrl.setBackground(new Color(255, 199, 210));
			actOK.setEnabled(false);
		}	
		tfRepositoryUrl.setToolTipText(msg);
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
			int answer = fc.showOpenDialog(ModuleRepositoryDialog.this);
			if (answer != JFileChooser.APPROVE_OPTION) return;			
			File f = fc.getSelectedFile();	
			if (f.isDirectory()) {
				tfRepositoryUrl.setText(new CommonJSModuleRepository(f).getURL().toString());
			} else if (f.isFile()) {				
				try {
					JarFile jar = new JarFile(f);	
					tfRepositoryUrl.setText(new CommonJSModuleRepository(jar).getURL().toString());
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
			} catch(IllegalArgumentException e) {
				// fall through
			}
			if (!s.matches("^[a-zA-Z]+/")) {
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
			String s = tfRepositoryUrl.getText().trim();
			try {
				repository = new CommonJSModuleRepository(s);
			} catch(IllegalArgumentException e){
				// should not happen, because input is already validated
				repository = null;
			}
			setVisible(false);
		}
	}
}
