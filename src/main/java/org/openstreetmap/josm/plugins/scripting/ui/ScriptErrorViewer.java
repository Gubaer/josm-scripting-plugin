package org.openstreetmap.josm.plugins.scripting.ui;


import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;

import javax.script.ScriptException;
import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A {@link JPanel} which displays an error thrown during the
 * execution of a script.
 */
public class ScriptErrorViewer extends JPanel {
    static private final String PREF_KEY_SHOW_STACKTRACE = ScriptErrorViewer.class.getName() + ".show-stack-trace-enabled";

    private boolean loadFromPrefShowStackTraceEnabled() {
        final var prefs = Preferences.main();
        return prefs.getBoolean(PREF_KEY_SHOW_STACKTRACE);
    }

    private void saveToPrefShowStackTraceEnabled(boolean value) {
        final var prefs = Preferences.main();
        prefs.putBoolean(PREF_KEY_SHOW_STACKTRACE, value);
    }

    private JTextPane paneOutput;
    private final ScriptErrorViewerModel model;
    private boolean showStackTrace = false;

    /**
     * Creates a new viewer with a new view model.
     *
     * @see #ScriptErrorViewer(ScriptErrorViewerModel)
     */
    public ScriptErrorViewer() {
        this(new ScriptErrorViewerModel());
    }

    /**
     * Creates a new viewer with a supplied model.
     *
     * @param model the model
     * @throws NullPointerException thrown if <code>model</code> is null
     */
    public ScriptErrorViewer(@NotNull final ScriptErrorViewerModel model) {
        Objects.requireNonNull(model);
        this.model = model;
        build();
    }

    /**
     * Replies the view model
     *
     * @return the model
     */
    public @NotNull ScriptErrorViewerModel getModel() {
        return model;
    }

    protected JPanel buildOptionsPanel() {
        final var p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        final var cb = new JCheckBox();
        p.add(cb);
        showStackTrace = loadFromPrefShowStackTraceEnabled();
        cb.setSelected(showStackTrace);
        cb.addChangeListener(event -> {
            showStackTrace = cb.isSelected();
            saveToPrefShowStackTraceEnabled(showStackTrace);
            refreshView();
        });
        p.add(new JLabel(tr("Show stack trace")));
        return p;
    }

    protected JComponent buildViewerPanel() {
        paneOutput = new JTextPane();
        paneOutput.setEditable(false);
        final var fontSize = paneOutput.getFont().getSize();
        paneOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        final var editorScrollPane = new JScrollPane(paneOutput);
        editorScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(editorScrollPane, BorderLayout.CENTER);
        model.addPropertyChangeListener(new ErrorModelChangeListener());
        return editorScrollPane;
    }

    protected void build() {
        setLayout(new BorderLayout());
        add(buildOptionsPanel(), BorderLayout.NORTH);
        add(buildViewerPanel(), BorderLayout.CENTER);
    }

    protected String formatScriptException(ScriptException exception) {
        return exception.getMessage();
    }

    protected String formatGeneralException(Throwable exception) {
        final var builder = new StringBuilder();
        builder.append(exception.getMessage());
        builder.append("\n");
        final var writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        builder.append(writer.getBuffer());
        return builder.toString();
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
        var builder = new StringBuilder();
        var scriptException =
            lookupCauseByExceptionType(exception, ScriptException.class);

        if (scriptException != null) {
            builder.append(formatScriptException((ScriptException) scriptException));
        } else if (GraalVMFacadeFactory.isGraalVMPresent()) {
            try {
                // dynamic lookup necessary
                var clazz = Class.forName("org.graalvm.polyglot.PolyglotException");
                var polyglotException = lookupCauseByExceptionType(exception, clazz);
                if (polyglotException != null) {
                    builder.append(formatPolyglotException(polyglotException));
                }
            } catch(ClassNotFoundException e) {
                // ignore
            }
        } else {
            builder.append(formatGeneralException(exception));
        }

        if (showStackTrace) {
            builder
                .append("\n")
                .append("--------------------------------------------------------")
                .append("\n");
            var sb = new StringWriter();
            var writer = new PrintWriter(sb);
            exception.printStackTrace(writer);
            builder.append(sb);
        }
        paneOutput.setText(builder.toString());
        paneOutput.setCaretPosition(0);
    }

    private void refreshView() {
        final var exception = model.getError();
        displayException(exception);
    }

    protected @Null Throwable lookupCauseByExceptionType(Throwable t, Class<?> clazz) {
        while(t != null) {
            if (clazz.isInstance(t)) {
            // if (clazz.getName().equals(t.getClass().getName())) {
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

    class ErrorModelChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (! ScriptErrorViewerModel.PROP_ERROR.equals(event.getPropertyName())) {
                return;
            }
            if (event.getNewValue() == null) {
                displayException(null);
            } else if (event.getNewValue() instanceof Throwable) {
                displayException((Throwable) event.getNewValue());
            }
        }
    }
}
