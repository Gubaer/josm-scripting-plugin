package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import jsyntaxpane.DefaultSyntaxKit;

import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.tools.ImageProvider;

public class ScriptingConsolePanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ScriptingConsolePanel.class.getName());
	
	private JSplitPane spConsole;
	private JTextPane epOutput;
	private ScriptEditor editor;
	
	protected JPanel buildControlPanel() {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		pnl.setBorder(null);
		pnl.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		JButton btn = new SideButton(new RunScriptAction(editor.getModel()));
		pnl.add(btn);		
		return pnl;		
	}
	
	protected JPanel buildInputPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.add(editor = new ScriptEditor(), BorderLayout.CENTER);
		pnl.add(buildControlPanel(), BorderLayout.SOUTH);
		return pnl;
	}
	
	protected JPanel buildOutputPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		epOutput = new JTextPane();
		epOutput.setEditable(false);
		JScrollPane editorScrollPane = new JScrollPane(epOutput);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnl.add(editorScrollPane, BorderLayout.CENTER);
		
		return pnl;
	}
	
	protected JSplitPane buildSplitPane() {
		final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setDividerSize(5);
		sp.setTopComponent(buildInputPanel());
		sp.setBottomComponent(buildOutputPanel());
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				sp.setDividerLocation(0.7);
			}
		});
		return sp;
	}
	
	protected void build() {		
		DefaultSyntaxKit.initKit();		

		spConsole = buildSplitPane();
		setLayout(new BorderLayout());
		add(spConsole, BorderLayout.CENTER);
		editor.getModel().addPropertyChangeListener(
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (! evt.getPropertyName().equals(ScriptEditorModel.PROP_SCRIPT_ENGINE_FACTORY)) return;
						ScriptEngineFactory sef = (ScriptEngineFactory)evt.getNewValue();
						updateScriptContentType(sef);
					}
				}
		);
		updateScriptContentType(editor.getModel().getScriptEngineFactory());
	}


	protected void warnMissingSyntaxKit(ScriptEngineFactory factory) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append(tr("Didn''t find a suitable syntax kit for the script engine <strong>{0}</strong>.", factory.getEngineName()));
		sb.append("<p>");
		sb.append(tr("No syntax kit is configured for either of the following content types:"));
		sb.append("<ul>");
		for(String mt: factory.getMimeTypes()) {
			sb.append("<li><tt>").append(mt).append("</tt></li>");
		}
		sb.append("</ul>");
		sb.append(tr("Syntax highlighting is going to be disabled."));
		sb.append("<p>");
		sb.append(tr("Refer to the online help for information on how to configure syntax kits for specific content types."));
		sb.append("</html>");
		
		ButtonSpec[] btns = new ButtonSpec[] {
				new ButtonSpec(
						tr("OK"),
						ImageProvider.get("ok"),
						"",
						null // no specific help topic
				)
		};
		
		HelpAwareOptionPane.showOptionDialog(
				this, 
				sb.toString(),
				tr("No syntax kit"),
				JOptionPane.WARNING_MESSAGE,
				null,
				btns,
				btns[0],
				null //FIXME: help topic
		);	
	}
	
	protected void updateScriptContentType(ScriptEngineFactory factory) {
		if (factory == null){
			editor.changeContentType("text/plain");
		} else {
			List<String> mimeTypes = factory.getMimeTypes();	
			for (String mt: mimeTypes) {
				if (MimeTypeToSyntaxKitMap.getInstance().isSupported(mt)) {
					editor.changeContentType(mt);
					return;
				}
			}			
			editor.changeContentType("text/plain");
			warnMissingSyntaxKit(factory);
		}
	}
	
	public ScriptingConsolePanel() {
		build();
	}

	
	/**
	 * <p>Reads the script from file {@code file}</p>
	 * 
	 * @param file the file. Must not be null. A readable file is expected.
	 */
    public void open(File file) {    	
    	editor.open(file);
    }
    
    public void save(File file) {
    	editor.save(file);
    }
    
    public void save()  {
    	File f = editor.getModel().getScriptFile();
    	if (f == null) return;
    	editor.save(f);
    }
    
    public ScriptEditorModel getScriptEditorModel() {
    	return editor.getModel();
    }

	class RunScriptAction extends AbstractAction implements PropertyChangeListener {
		private ScriptEditorModel model;
		public RunScriptAction(ScriptEditorModel model) {
			this.model = model;
			putValue(SMALL_ICON, ImageProvider.get("media-playback-start"));
			putValue(SHORT_DESCRIPTION, tr("Execute the script"));
			putValue(NAME, tr("Run"));
			model.addPropertyChangeListener(this);
			updateEnabledState();
		}
		
		protected void dumpException(Throwable t){
			StringWriter w = new StringWriter();
			t.printStackTrace(new PrintWriter(w));
			Document doc = epOutput.getDocument();
			try {
				SimpleAttributeSet set = new SimpleAttributeSet();
				StyleConstants.setForeground(set, Color.RED);
				doc.insertString(doc.getLength(), w.getBuffer().toString(), set);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String source = editor.getScript();
			ScriptEngine engine = model.getScriptEngineFactory().getScriptEngine();
			try {		
				engine.eval(source, new JOSMScriptContext(epOutput.getDocument()));
			} catch(ScriptException ex){
				dumpException(ex);

			}
		}

		protected void updateEnabledState() {
			setEnabled(model.getScriptEngineFactory() != null);
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (!evt.getPropertyName().equals(ScriptEditorModel.PROP_SCRIPT_ENGINE_FACTORY)) return;
			updateEnabledState();
		}
	}
}
