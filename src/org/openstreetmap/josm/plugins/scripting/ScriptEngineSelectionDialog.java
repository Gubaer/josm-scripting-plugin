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
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.swing.AbstractAction;
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
import org.openstreetmap.josm.plugins.scripting.ui.ScriptEngineCellRenderer;

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
	private ScriptEngineProvider model;
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
		lstEngines = new JList(model = ScriptEngineProvider.getInstance());
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
}
