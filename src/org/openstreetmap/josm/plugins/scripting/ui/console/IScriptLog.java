package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.io.PrintWriter;

import javax.swing.Action;

public interface IScriptLog {

	/**
	 * Replies the action for clearing the log content.
	 * 
	 * @return
	 */
	Action getClearAction();
	
	/**
	 * Replies a writer for appending text to the log.
	 * 
	 * @return the writer 
	 */
	PrintWriter getLogWriter();	
}
