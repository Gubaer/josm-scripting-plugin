package org.openstreetmap.josm.plugins.scripting.ui;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.openstreetmap.josm.tools.I18n.tr;

public class ScriptErrorDialog  {

    private static JPanel buildTopPanel() {
        final JPanel pnl = new JPanel(new BorderLayout());
        final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
        final String infoTxt = tr("<html>" +
            "An error occurred when executing a script. This is most likely " +
            "a bug in the script, and not in JOSM.<p>" +
            "Please get in touch with the script author before you file " +
            "a bug in JOSMs bug tracker." +
            "</html>");
        pane.setText(infoTxt);
        pnl.add(pane, BorderLayout.CENTER);
        return pnl;
    }

    private static String dumpStackTrace(Throwable t) {
        final StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static Component buildStacktracePane(Throwable forException) {
        // build  text pane
        final JTextPane tp = new JTextPane();
        tp.setText(dumpStackTrace(forException));
        tp.setEditable(false);
        tp.setCaretPosition(0); // scroll to top

        // build scroll pane
        final JScrollPane sp = new JScrollPane();
        sp.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getViewport().add(tp);
        return sp;
    }

    private static JPanel buildContentPanel(Throwable forException) {
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(buildTopPanel(), BorderLayout.NORTH);
        pnl.add(buildStacktracePane(forException), BorderLayout.CENTER);
        return pnl;
    }

    /**
     * Display an error dialog with the stack trace for the exception.
     *
     * @param forException the exception whose stack trace is displayed
     */
    @SuppressWarnings("unused")
    public static void showErrorDialog(@NotNull Throwable forException) {
        showErrorDialog(tr("Script Error - Stack Trace"), forException);
    }

    /**
     * Display an error dialog with the stack trace for the exception and
     * a custom title.
     *
     * @param title the dialog title
     * @param forException the exception whose stack trace is displayed
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public static void showErrorDialog(@NotNull String title,
                                       @NotNull Throwable forException) {
        final JOptionPane pane = new JOptionPane(
                buildContentPanel(forException),
                JOptionPane.ERROR_MESSAGE
        );
        final JDialog dialog = pane.createDialog(title);
        dialog.setSize(600,400);
        dialog.setResizable(true);
        dialog.setVisible(true);
    }
}
