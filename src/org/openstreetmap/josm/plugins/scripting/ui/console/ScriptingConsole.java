package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.tools.WindowGeometry;

public class ScriptingConsole extends JFrame {
	
	static public final BooleanProperty PREF_ALWAYS_ON_TOP = new BooleanProperty(
			ScriptingConsole.class.getName() + ".alwaysOnTop", 
			true
	 );

	private JCheckBox cbAlwaysOnTop;
	
	private static ScriptingConsole instance = null;
	public static ScriptingConsole getScriptingConsole() {
		return instance;
	}
	
	public static void showScriptingConsole() {
		synchronized (ScriptingConsole.class) {
			if (instance == null){
				instance = new ScriptingConsole();
				instance.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {	
						ScriptingConsole old = instance;
						instance = null;
						fireScriptingConsoleChanged(old, instance);
					}
				});
			}
			instance.setVisible(true);
		}
	}
	
	
	public static void hideScriptingConsole() {
		synchronized(ScriptingConsole.class){
			if (instance != null){
				instance.setVisible(false);
				// this will trigger a window closing event, handled above
			}
		}
	}
	
	public static void toggleScriptingConsole() {
		if (instance == null){
			showScriptingConsole();
		} else {
			hideScriptingConsole();
		}
	}

	public static interface ScriptingConsoleListener {
		void scriptingConsoleChanged(ScriptingConsole oldValue, ScriptingConsole newValue);
	}
	
	private static final CopyOnWriteArrayList<ScriptingConsoleListener> listeners = new CopyOnWriteArrayList<ScriptingConsole.ScriptingConsoleListener>();
	
	public static void addScriptingConsoleListener(ScriptingConsoleListener l){
		if (l == null) return;
		listeners.addIfAbsent(l);
	}
	
	public static void removeScriptingConsoleListener(ScriptingConsoleListener l){
		if (l == null)return;
		listeners.remove(l);
	}
	
	protected static void fireScriptingConsoleChanged(ScriptingConsole oldValue, ScriptingConsole newValue){
		for (ScriptingConsoleListener l: listeners) {
			l.scriptingConsoleChanged(oldValue, newValue);
		}
	}
	
	private ScriptingConsolePanel pnlScriptingConsole = null;
	
	private ScriptingConsole(){
		super(tr("Scripting Console"));
		build();
	}
	
	protected JMenuBar buildMenuBar() {
		JMenu mnuFile = new JMenu(tr("File"));
		mnuFile.add(new OpenAction());
		SaveAction act = new SaveAction();
		getScriptEditorModel().addPropertyChangeListener(act);
		mnuFile.add(act);
		mnuFile.add(new SaveAsAction());
		mnuFile.addSeparator();
		mnuFile.add(new CloseAction());
		JMenuBar bar = new JMenuBar();
		bar.add(mnuFile);
		return bar;
	}
	
	protected JPanel buildControlPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		cbAlwaysOnTop = new JCheckBox(new ToggleAlwaysOnTopAction()); 
		cbAlwaysOnTop.setFont(UIManager.getFont("Label.font").deriveFont(10));
		pnl.add(cbAlwaysOnTop);
		return pnl;
	}
	
	protected void build(){
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(pnlScriptingConsole = new ScriptingConsolePanel(), BorderLayout.CENTER);
		c.add(buildControlPanel(), BorderLayout.SOUTH);
		
		setJMenuBar(buildMenuBar());
	}	
	
	public void setVisible(boolean visible){
		if (visible) {
			new WindowGeometry(
					ScriptingConsole.class.getName()+ ".geometry",
					WindowGeometry.centerInWindow(this, new Dimension(500, 800))
			).applySafe(this);
		} else {
			new WindowGeometry(this).remember(ScriptingConsole.class.getName()+ ".geometry");
		}
		super.setVisible(visible);
	}
	
	/**
	 * <p>Loads the file {@code file} in the script editor.</p>
	 * 
	 * @param file the file. Must not be null
	 */
	public void open(File file){
    	Assert.assertArgNotNull(file, "file");
    	Assert.assertArg(file.isFile(), "Expected a file, got a directory. File is: {0}", file);
    	Assert.assertArg(file.canRead(), "Expected a readable file, but can''t read file. File is: {0}", file);
		pnlScriptingConsole.open(file);
	}
	
	public void save(File file){
    	Assert.assertArgNotNull(file, "file");
    	pnlScriptingConsole.save(file);
	}	
	
	public void save() {
		pnlScriptingConsole.save();
	}
	
	public ScriptEditorModel getScriptEditorModel() {
		return pnlScriptingConsole.getScriptEditorModel();
	}
	
	private class ToggleAlwaysOnTopAction extends AbstractAction {
		public ToggleAlwaysOnTopAction() {		
			putValue(NAME, tr("Always on top"));
			putValue(SELECTED_KEY, PREF_ALWAYS_ON_TOP.get());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setAlwaysOnTop(cbAlwaysOnTop.isSelected());
		}
	}
	
	
}
