package org.openstreetmap.josm.plugins.scripting.ui.console;


import org.mozilla.javascript.EcmaError;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;

import javax.script.ScriptException;
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

    public ErrorOutputPanel() {
        build();
    }

    protected void build() {
        setLayout(new BorderLayout());
        paneOutput = new JTextPane();
        paneOutput.setEditable(false);
        JScrollPane editorScrollPane = new JScrollPane(paneOutput);
        editorScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(editorScrollPane, BorderLayout.CENTER);
    }

    protected void displayPolyglotException(Throwable exception) {
        paneOutput.setText(formatPolyglotException(exception));
        paneOutput.setCaretPosition(0);
    }

    protected void displayMozillaEcmaError(EcmaError exception) {
        paneOutput.setText(exception.getMessage());
        paneOutput.setCaretPosition(0);
    }

    protected void displayScriptException(ScriptException exception) {
        paneOutput.setText(exception.getMessage());
        paneOutput.setCaretPosition(0);
    }

    protected void displayGeneralException(Throwable exception) {
        final var builder = new StringBuilder();
        builder.append(exception.getMessage());
        builder.append("\n");
        final var writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        builder.append(writer.getBuffer());
        paneOutput.setText(builder.toString());
        paneOutput.setCaretPosition(0);
    }

    /**
     * Displays an exception. If <code>exception</code> is null, an empty
     * string is displayed.
     *
     * @param exception the exception
     */
    public void displayException(@Null Throwable exception) {
        if (exception == null) {
            paneOutput.setText("");
            paneOutput.setCaretPosition(0);
            return;
        }
        if (GraalVMFacadeFactory.isGraalVMPresent()) {
            try {
                // dynamic lookup necessary
                final var clazz = Class.forName("org.graalvm.polyglot.PolyglotException");
                final var polyglotException = lookupCauseByExceptionType(exception, clazz);
                if (polyglotException != null) {
                    displayPolyglotException(polyglotException);
                    return;
                }

            } catch(ClassNotFoundException e) {
                return;
            }
        }
        final var mozillaEcmaError = lookupCauseByExceptionType(exception, EcmaError.class);
        if (mozillaEcmaError != null) {
            displayMozillaEcmaError((EcmaError) mozillaEcmaError);
            return;
        }
        final var scriptException = lookupCauseByExceptionType(exception, ScriptException.class);
        if (scriptException != null) {
            displayScriptException((ScriptException) scriptException);
            return;
        }
        displayGeneralException(exception);
    }

    protected @Null Throwable lookupCauseByExceptionType(Throwable t, Class<?> clazz) {
        while(t != null) {
            if (clazz.isInstance(t)) {
                break;
            }
            t = t.getCause();
        }
        return t;
    }

    protected String formatPolyglotException(@NotNull final Throwable exception) {
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
