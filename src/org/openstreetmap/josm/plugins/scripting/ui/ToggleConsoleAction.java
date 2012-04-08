package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole;
import org.openstreetmap.josm.plugins.scripting.ui.console.ScriptingConsole.ScriptingConsoleListener;

@SuppressWarnings("serial")
public class ToggleConsoleAction extends AbstractAction implements ScriptingConsoleListener {
	@SuppressWarnings("unused")
	static private final Logger logger = Logger.getLogger(ToggleConsoleAction.class.getName());
	
	public ToggleConsoleAction(){
		putValue(NAME, tr("Show scripting console"));
		putValue(SHORT_DESCRIPTION, tr("Select to display the scripting console"));
		putValue(SELECTED_KEY, ScriptingConsole.getScriptingConsole() != null);
		
		ScriptingConsole.addScriptingConsoleListener(this);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		ScriptingConsole.toggleScriptingConsole();
	}

	@Override
	public void scriptingConsoleChanged(ScriptingConsole oldValue, ScriptingConsole newValue) {
		putValue(SELECTED_KEY, newValue != null);
	}
}
