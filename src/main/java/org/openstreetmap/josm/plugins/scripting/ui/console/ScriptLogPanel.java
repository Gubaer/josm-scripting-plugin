package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openstreetmap.josm.tools.ImageProvider;

@SuppressWarnings("unused")
public class ScriptLogPanel extends JPanel implements IScriptLog{
    @SuppressWarnings("unused")
    static private final Logger logger =
            Logger.getLogger(ScriptLogPanel.class.getName());

    private JTextPane epOutput;
    private final Action actClear = new ClearAction();
    private JPopupMenu popupMenu;

    protected void build() {
        setLayout(new BorderLayout());
        epOutput = new JTextPane();
        epOutput.setEditable(false);
        final JScrollPane editorScrollPane = new JScrollPane(epOutput);
        editorScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(editorScrollPane, BorderLayout.CENTER);

        popupMenu = buildPopupMenu();
        epOutput.addMouseListener(new PopupMenuLauncher());
    }

    protected JPopupMenu buildPopupMenu() {
        final JPopupMenu mnu = new JPopupMenu();
        mnu.add(getClearAction());
        return mnu;
    }

    public ScriptLogPanel(){
        build();
    }

    /**
     * Dumps an exception to the log. Text is displayed in red.
     *
     * @param t the exception
     */
    public void dumpException(Throwable t){
        if (t == null) return;
        final StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        Document doc = epOutput.getDocument();
        try {
            SimpleAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setForeground(set, Color.RED);
            doc.insertString(doc.getLength(), w.getBuffer().toString(), set);
        } catch(BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replies a writer which can be used to append text to the log.
     *
     * @return the writer
     */
    public PrintWriter getLogWriter() {
        return new PrintWriter(new DocumentWriter(epOutput.getDocument()));
    }

    public Action getClearAction() {
        return actClear;
    }

    private class ClearAction extends AbstractAction {

        public ClearAction() {
            putValue(NAME, tr("Clear log"));
            putValue(SHORT_DESCRIPTION, tr("Clear the log content"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete",
                ImageProvider.ImageSizes.MENU));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                epOutput.getDocument().remove(0,
                        epOutput.getDocument().getLength());
            } catch(BadLocationException ex){
                // ignore
            }
        }
    }

    private class PopupMenuLauncher extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (! e.isPopupTrigger()) return;
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (! e.isPopupTrigger()) return;
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
