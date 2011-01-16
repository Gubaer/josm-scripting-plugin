package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.openstreetmap.josm.gui.util.TableCellEditorSupport;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.SelectAllOnFocusGainedDecorator;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.scripting.ScriptEngineProvider;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineCellRenderer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * <p><strong>ScriptEnginesConfigurationPanel</strong> allows to configure 
 * the script engines available in JOSM.</p>
 * 
 */
public class ScriptEnginesConfigurationPanel extends VerticallyScrollablePanel{
	
	private ScriptEngineJarTableModel model;
	private JTable tblJarFiles;
	private RemoveJarAction actDelete;
	
	public ScriptEnginesConfigurationPanel() {
		build();
		model.restoreFromPreferences();
	}
	
	protected JPanel buildScriptEnginesInfoPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		JList lstEngines = new JList(ScriptEngineProvider.getInstance());
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
	
		JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		AddJarAction actAdd = new AddJarAction();
		ctrlPanel.add(new JButton(actAdd));
		actDelete = new RemoveJarAction();
		ctrlPanel.add(new JButton(actDelete));
		model.getSelectionModel().addListSelectionListener(actDelete);
		tblJarFiles.getActionMap().put("deleteSelection", actDelete);
		tblJarFiles.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),"deleteSelection");
		tblJarFiles.getActionMap().put("insertRow", actAdd);
		tblJarFiles.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),"insertRow");
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
			ScriptEngineJarCellRenderer renderer = new ScriptEngineJarCellRenderer();
			
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
					setText(parentName.substring(0, 10) + "..."  + File.pathSeparator + f.getName());										
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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			ScriptEngineJarInfo jar = (ScriptEngineJarInfo)value;
			reset();
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
	
	private class RemoveJarAction extends AbstractAction implements ListSelectionListener{

		public RemoveJarAction() {
			putValue(NAME, tr("Remove"));
			putValue(SHORT_DESCRIPTION, tr("Remove the selected jar files"));
			putValue(SMALL_ICON, ImageProvider.get("dialogs","delete"));
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
			putValue(SHORT_DESCRIPTION, tr("Add a jar file providing a script engine"));
			putValue(SMALL_ICON, ImageProvider.get("add"));
		}		
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			model.addNew();		
			tblJarFiles.editCellAt(tblJarFiles.getRowCount()-1, 1);
		}
	}
	
	private static class JarFileNameEditor extends JPanel implements TableCellEditor {
		static private final Logger logger = Logger.getLogger(JarFileNameEditor.class.getName());

		private JTextField tfJarFile;
		private JButton btnLauchFileChooser;
		private TableCellEditorSupport tableCellEditorSupport;
		private ScriptEngineJarInfo info;
		
		public JarFileNameEditor() {
			tableCellEditorSupport = new TableCellEditorSupport(this);
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
			add(btnLauchFileChooser = new JButton(new LaunchFileChooserAction()), gc);
			
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
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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
