package org.openstreetmap.josm.plugins.scripting.ui.console;


import org.graalvm.polyglot.PolyglotException;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Displays errors when the execution of a script fails
 */
public class ErrorOutputPanel extends JPanel {

    private JTextPane paneOutput;
    private JScrollPane editorScrollPane;

    public ErrorOutputPanel() {
        build();
    }

    protected void build() {
        setLayout(new BorderLayout());
        paneOutput = new JTextPane();
        paneOutput.setEditable(false);
        editorScrollPane = new JScrollPane(paneOutput);
        editorScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(editorScrollPane, BorderLayout.CENTER);
    }

    protected void displayPolyglotException(PolyglotException exception) {
        paneOutput.setText(formatPolyglotException(exception));
    }

    protected void displayGeneralException(Throwable exception) {
        final var builder = new StringBuilder();
        builder.append(exception.getMessage());
        builder.append("\n");
        final var writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        builder.append(writer.getBuffer());
        paneOutput.setText(builder.toString());    }


    /**
     * Displays an exception
     *
     * @param exception the exception
     */
    public void displayException(@NotNull Throwable exception) {
        if (GraalVMFacadeFactory.isGraalVMPresent()) {
            final var polyglotException = lookupPolyglotException(exception);
            if (polyglotException != null) {
                displayPolyglotException(polyglotException);
                return;
            }
        }
        displayGeneralException(exception);
        // scroll to top
        paneOutput.setCaretPosition(0);
    }

    protected @Null PolyglotException lookupPolyglotException(Throwable t) {
        while(t != null) {
            if (t instanceof  PolyglotException) {
                break;
            }
            t = t.getCause();
        }
        if (t == null) {
            return null;
        } else {
            return (PolyglotException) t;
        }
    }

    protected String formatPolyglotException(@NotNull final PolyglotException exception) {
        return exception.getMessage() +
            "\n" +
            Arrays.stream(exception.getStackTrace())
                .filter(element -> element.getClassName().contains("<js>"))
                .map(element -> MessageFormat.format(
                    "{0}: line={1}",
                    element.getFileName(),
                    element.getLineNumber()
                ))
                .collect(Collectors.joining("\n"));
    }
}
