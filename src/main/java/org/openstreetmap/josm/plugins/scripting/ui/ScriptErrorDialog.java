package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.WindowGeometry;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.Objects;

import static org.openstreetmap.josm.tools.I18n.tr;

public class ScriptErrorDialog  {
    private static class ContentPane extends JPanel {

        private ScriptErrorViewer viewer;

        private JPanel buildTopPanel() {
            final JPanel pnl = new JPanel(new BorderLayout());
            final JEditorPane pane = EditorPaneBuilder.buildInfoEditorPane();
            final String infoTxt = "<html>" + tr(
                "An error occurred when executing a script. This is most likely " +
                "a bug in the script, and not in JOSM.<p>" +
                "Please get in touch with the script author before you file " +
                "a bug in JOSMs bug tracker."
            ) + "</html>";
            pane.setText(infoTxt);
            pnl.add(pane, BorderLayout.CENTER);
            return pnl;
        }

        private void build() {
            setLayout(new BorderLayout());
            add(buildTopPanel(), BorderLayout.NORTH);
            add(viewer = new ScriptErrorViewer(), BorderLayout.CENTER);
        }

        public ContentPane() {
            build();
        }

        public ScriptErrorViewerModel getModel() {
            return viewer.getModel();
        }
    }

    /**
     * Display an error dialog with the error information
     *
     * @param forException the exception
     * @throws NullPointerException thrown if <code>forException</code> is null
     */
    @SuppressWarnings("unused")
    public static void showErrorDialog(@NotNull Throwable forException) {
        showErrorDialog(tr("Script Error - Stack Trace"), forException);
    }

    /**
     * Display an error dialog with the error information and
     * a custom title.
     *
     * @param title the dialog title
     * @param forException the exception whose stack trace is displayed
     * @throws NullPointerException thrown if <code>title</code> is null
     * @throws NullPointerException thrown if <code>forException</code> is null
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public static void showErrorDialog(@NotNull String title,
                                       @NotNull Throwable forException) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(forException);
        final var contentPane = new ContentPane();
        final JOptionPane pane = new JOptionPane(
            contentPane,
            JOptionPane.ERROR_MESSAGE
        );
        contentPane.getModel().setError(forException);
        final JDialog dialog = pane.createDialog(title);
        dialog.setResizable(true);
        WindowGeometry.centerInWindow(
            MainApplication.getMainFrame(),
            new Dimension(600, 400)
        ).applySafe(dialog);
        dialog.setVisible(true);
    }
}
