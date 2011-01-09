package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * <strong>ScriptEngineSelectionDialog</strong> allows to select one of the available
 * JSR-223 compatible script engines.
 *
 */
public class ScriptEngineSelectionDialog extends JDialog {

	/**
	 * <p>Launches a modal dialog for selecting a script engine.</p>
	 * 
	 * @param parent the parent component for the dialog. Assumes {@code Main.parent} if
	 * null
	 *  
	 * @return the selected script engine, or null, if the user didn't select an engine
	 */
	static public ScriptEngine select(Component parent){
		if (parent == null) parent = Main.parent;
		ScriptEngineSelectionDialog dialog = new ScriptEngineSelectionDialog(parent);
		dialog.setVisible(true);
		return dialog.selectedEngine;
	}

	/**
	 * <p>Launches a modal dialog for selecting a script engine. The dialog is opend
	 * with {@code Main.parent} as owner.</p>
	 *  
	 * @return the selected script engine, or null, if the user didn't select an engine
	 */
	static public ScriptEngine select(){
		return select(Main.parent);
	}
	
	private JList lstEngines;
	private JButton btnOK;
	private ScriptEngine selectedEngine;
	private ScriptEngineListModel model;
	private OKAction actOK;
	
	/**
	 * <p>Creates a new dialog.</p>
	 * 
	 * @param parent the parent. Uses {@link JOptionPane#getFrameForComponent(Component)} to 
	 * determine the owner frame.
	 */
	public ScriptEngineSelectionDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), true /* modal */);
		build();
	}
	
	protected JPanel buildInfoPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		HtmlPanel info = new HtmlPanel();
		info.setText(
				"<html>"
				+ tr("Please select a scripting engine to execute the selected script.")
				+ "</html>"
		);
		pnl.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		pnl.add(info, BorderLayout.CENTER);
		return pnl;
	}
	
	protected JPanel buildControlButtonPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		SideButton btn;
		pnl.add(btnOK = new SideButton(actOK = new OKAction()));
		btnOK.setFocusable(true);
		CancelAction actCancel;
		pnl.add(btn = new SideButton(actCancel = new CancelAction()));
		btn.setFocusable(true);
		pnl.add(btn = new SideButton(new ContextSensitiveHelpAction(HelpUtil.ht("/Plugins/Scripting#SelectScriptingEngine"))));
		btn.setFocusable(true);
		
		// Ctrl-Enter triggers OK
		getRootPane().registerKeyboardAction(
				actOK,  
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), 
				JComponent.WHEN_IN_FOCUSED_WINDOW
		);
		
		// ESC triggers Cancel 
		getRootPane().registerKeyboardAction(
				actCancel,  
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
				JComponent.WHEN_IN_FOCUSED_WINDOW
		);
		return pnl;
	}
	
	protected JPanel buildScriptEngineListPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		lstEngines = new JList(model = new ScriptEngineListModel());
		lstEngines.setCellRenderer(new ScriptEngineCellRenderer());
		pnl.add(lstEngines, BorderLayout.CENTER);
		lstEngines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstEngines.setSelectedIndex(0);
		
		lstEngines.addMouseListener(
				new MouseAdapter() {					
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() >= 2) {
							actOK.execute();
						}
					}
				}
		);
		
		return pnl;			
	}
	
	protected void build() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(buildInfoPanel(), BorderLayout.NORTH);
		c.add(buildScriptEngineListPanel(), BorderLayout.CENTER);
		c.add(buildControlButtonPanel(), BorderLayout.SOUTH);
		
		lstEngines.getSelectionModel().addListSelectionListener((OKAction)btnOK.getAction());
		
		// Respond to 'Enter' in the list
		lstEngines.registerKeyboardAction(
				actOK, 
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
				JComponent.WHEN_FOCUSED
		);
	}
	
	private class OKAction extends AbstractAction implements ListSelectionListener {
		public OKAction() {
			putValue(NAME, tr("OK"));
			putValue(SHORT_DESCRIPTION, tr("Accept the selected scripting engine"));
			putValue(SMALL_ICON, ImageProvider.get("ok"));
		}
		
		public void execute() {
			int selIndex = lstEngines.getSelectedIndex();
			selectedEngine = selIndex < 0 ? null: model.getScriptEngine(selIndex) ;
			setVisible(false);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			execute();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			setEnabled(lstEngines.getSelectedIndex() >=0);
		}
	}

	private class CancelAction extends AbstractAction {
		public CancelAction() {
			putValue(NAME, tr("Cancel"));
			putValue(SHORT_DESCRIPTION, tr("cancel"));
			putValue(SMALL_ICON, ImageProvider.get("cancel"));			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			selectedEngine = null;
			setVisible(false);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			btnOK.requestFocusInWindow();
			WindowGeometry
				.centerInWindow(getParent(), new Dimension(250, 300))
				.applySafe(this);
		}
		super.setVisible(visible);
	}

	/**
	 * <p>Provides a list model for the list of available script engines.</p>
	 */
	private static class ScriptEngineListModel extends AbstractListModel {
		
		private List<ScriptEngineFactory> factories;
		
		public ScriptEngineListModel(){
			ScriptEngineManager mgr = new ScriptEngineManager(getClass().getClassLoader());
			factories = new ArrayList<ScriptEngineFactory>(mgr.getEngineFactories());
			Collections.sort(factories,
					new Comparator<ScriptEngineFactory>() {
						@Override
						public int compare(ScriptEngineFactory f1, ScriptEngineFactory f2) {
							return f1.getEngineName().compareTo(f2.getEngineName());
						}
					}
			);	
		}
				
		/**
		 * <p>Replies a script engine created by the i-th script engine factory.</p>
		 * 
		 * @param i the index
		 * @return the engine
		 */
		public ScriptEngine getScriptEngine(int i){
			ScriptEngine engine = factories.get(i).getScriptEngine();
			return engine;
		}

		@Override
		public Object getElementAt(int i) {
			return factories.get(i);
		}

		@Override
		public int getSize() {
			return factories.size();
		}		
	}
	
	/**
	 * <p>Implements a list cell renderer for the list of scripting engines.</p>
	 *
	 */
	private static class ScriptEngineCellRenderer implements ListCellRenderer {

		private final JLabel lbl = new JLabel();
		
		protected String getDisplayName(ScriptEngineFactory factory){
			return tr("{1} (with engine {0})", factory.getEngineName(), factory.getLanguageName());
		}
		
		protected String getTooltipText(ScriptEngineFactory factory){
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<strong>").append(tr("Name:")).append("</strong> ").append(factory.getEngineName()).append("<br>");
			sb.append("<strong>").append(tr("Version:")).append("</strong> ").append(factory.getEngineVersion()).append("<br>");
			sb.append("<strong>").append(tr("Language:")).append("</strong> ").append(factory.getLanguageName()).append("<br>");
			sb.append("<strong>").append(tr("Language version:")).append("</strong> ").append(factory.getLanguageVersion()).append("<br>");
			sb.append("<strong>").append(tr("MIME-Types:")).append("</strong> ");
			List<String> types = factory.getMimeTypes();
			for(int i=0; i<types.size(); i++){
				if (i > 0 )sb.append(", ");
				sb.append(types.get(i));
			}
			sb.append("<br>");
			sb.append("</html>");
			
			return sb.toString();
		}
		
		protected void renderColors(boolean selected){
			if (!selected){
				lbl.setForeground(UIManager.getColor("List.foreground"));
				lbl.setBackground(UIManager.getColor("List.background"));
			} else {
				lbl.setForeground(UIManager.getColor("List.selectionForeground"));
				lbl.setBackground(UIManager.getColor("List.selectionBackground"));
			}
		}
		
		public ScriptEngineCellRenderer() {		
			lbl.setOpaque(true);
			lbl.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
			lbl.setIcon(ImageProvider.get("script-engine"));
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object obj,int index, boolean isSelected, boolean cellHasFocus) {
			ScriptEngineFactory factory = (ScriptEngineFactory)obj;
			renderColors(isSelected);
			lbl.setText(getDisplayName(factory));
			lbl.setToolTipText(getTooltipText(factory));
			return lbl;
		}		
	}
}
