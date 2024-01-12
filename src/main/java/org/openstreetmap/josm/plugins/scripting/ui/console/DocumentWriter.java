package org.openstreetmap.josm.plugins.scripting.ui.console;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * strong>DocumentWriter</strong> is a writer which appends text to
 * a {@link Document}.
 *
 */
public class DocumentWriter extends Writer {
    private final Document doc;
    public DocumentWriter(Document doc){
        this.doc = doc;
    }

    @SuppressWarnings({"RedundantThrows", "NullableProblems"})
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        String msg = new String(Arrays.copyOfRange(cbuf, off, off + len));
        try {
            doc.insertString(doc.getLength(), msg, null);
        } catch(BadLocationException e){
            e.printStackTrace();
        }
    }
    @SuppressWarnings("RedundantThrows")
    @Override
    public void flush() throws IOException {
        // nothing to flush
    }

    @Override
    public void close() throws IOException {
        // nothing to close
    }
}
