package org.openstreetmap.josm.plugins.scripting.ui.console;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * <p><strong>DocumentWriter</strong> is a writer which appends text to a {@link Document}.</p>
 *
 */
public class DocumentWriter extends Writer {
	private Document doc;
	public DocumentWriter(Document doc){
		this.doc = doc; 
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		String msg = new String(Arrays.copyOfRange(cbuf, off, off + len));
		try {
			doc.insertString(doc.getLength(), msg, null);
		} catch(BadLocationException e){
			e.printStackTrace();				
		}
	}
	@Override
	public void flush() throws IOException {
		// nothing to flush		
	}
	
	@Override
	public void close() throws IOException {
		// nothing to close 
	}
}
