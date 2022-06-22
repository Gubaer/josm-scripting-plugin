package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptExecutor;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The panel displaying the script editor and the console log in a split pane.
 */
@SuppressWarnings({"WeakerAccess"})
public class ScriptingConsolePanel extends JPanel {
    @SuppressWarnings("unused")
    private static final Logger logger =
        Logger.getLogger(ScriptingConsolePanel.class.getName());

    private ScriptLogPanel log;
    private ScriptEditor editor;

    protected JPanel buildControlPanel() {
        final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        pnl.setBorder(null);
        pnl.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        JButton btn = new JButton(new RunScriptAction(editor.getModel()));
        pnl.add(btn);
        return pnl;
    }

    protected JPanel buildInputPanel() {
        final JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(editor = new ScriptEditor(), BorderLayout.CENTER);
        pnl.add(buildControlPanel(), BorderLayout.SOUTH);
        return pnl;
    }

    protected JPanel buildOutputTabPanel() {
        final var tabPane = new JTabbedPane();
        final var errorOutput = new ErrorOutputPanel();
        tabPane.addTab(tr("Console"), log = new ScriptLogPanel());
        tabPane.setToolTipTextAt(0, tr("Displays script output"));
        tabPane.addTab(tr("Errors"), errorOutput);
        tabPane.setIconAt(1, ImageProvider.get(
            "circle-check-solid",
            ImageProvider.ImageSizes.SMALLICON));
        tabPane.setToolTipTextAt(1, tr("Displays scripting errors"));

        ErrorModel.getInstance().addPropertyChangeListener(
            new ErrorModelChangeListener(tabPane, errorOutput)
        );

        final var p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(tabPane, BorderLayout.CENTER);
        return p;
    }

    protected JSplitPane buildSplitPane() {
        final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setDividerSize(5);
        sp.setTopComponent(buildInputPanel());
        sp.setBottomComponent(buildOutputTabPanel());
        SwingUtilities.invokeLater(() -> sp.setDividerLocation(0.7));
        return sp;
    }

    protected void build() {
        JSplitPane spConsole = buildSplitPane();
        setLayout(new BorderLayout());
        add(spConsole, BorderLayout.CENTER);
        editor.getModel().addPropertyChangeListener(evt -> {
            if (! evt.getPropertyName()
                    .equals(ScriptEditorModel.PROP_SCRIPT_ENGINE)) {
                return;
            }
            final ScriptEngineDescriptor desc =
                    (ScriptEngineDescriptor)evt.getNewValue();
            updateScriptContentType(desc);
        });
        updateScriptContentType(editor.getModel().getScriptEngineDescriptor());
    }


    protected void warnMissingSyntaxStyle(@Null ScriptEngineDescriptor desc) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(tr(
            "Didn''t find a suitable syntax style for the script engine " +
            "<strong>{0}</strong>.",
            desc.getEngineName().orElse(tr("unknown"))));
        sb.append("<p>");
        sb.append(tr(
            "No syntax style is available for either of the following " +
            "content types:"));
        sb.append("<ul>");
        for(String mt: desc.getContentMimeTypes()) {
            sb.append("<li><tt>").append(mt).append("</tt></li>");
        }
        sb.append("</ul>");
        sb.append(tr("Syntax highlighting is disabled."));
        sb.append("<p>");
        sb.append(tr(
            "Refer to the online help on how to configure the syntax style " +
            "for specific content types."));
        sb.append("</html>");

        final ButtonSpec[] btns = new ButtonSpec[] {
                new ButtonSpec(
                    tr("OK"),
                    ImageProvider.get("ok"),
                    "",
                    null // no specific help topic
                )
        };

        HelpAwareOptionPane.showOptionDialog(
                this,
                sb.toString(),
                tr("No syntax kit"),
                JOptionPane.WARNING_MESSAGE,
                null,
                btns,
                btns[0],
                null // no help topic
        );
    }

    protected void updateScriptContentType(ScriptEngineDescriptor desc) {
        final Stream<String> contentTypes;
        contentTypes = desc == null
                ? Stream.of("text/plain")
                : desc.getContentMimeTypes().stream();
        final String syntaxStyle = contentTypes.map(editor::lookupSyntaxConstants)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (syntaxStyle != null) {
            editor.changeSyntaxEditingStyle(syntaxStyle);
        } else {
            //noinspection ConstantConditions
            warnMissingSyntaxStyle(desc);
            editor.changeSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }

    public ScriptingConsolePanel() {
        build();
    }

    /**
     * Reads the script from file {@code file}
     *
     * @param file the file. Must not be null. A readable file is expected.
     */
    public void open(@NotNull File file) {
        editor.open(file);
    }

    public void save(@NotNull File file) {
        editor.save(file);
    }

    public void save()  {
        editor.getModel()
            .getScriptFile()
            .ifPresent(f-> editor.save(f));
    }

    public ScriptEditorModel getScriptEditorModel() {
        return editor.getModel();
    }

    /**
     * Replies the script log
     *
     * @return the script log
     */
    public IScriptLog getScriptLog() {
        return log;
    }

    class RunScriptAction extends AbstractAction
        implements PropertyChangeListener {
        final private ScriptEditorModel model;
        public RunScriptAction(ScriptEditorModel model) {
            this.model = model;
            putValue(SMALL_ICON, ImageProvider.get("media-playback-start",
                ImageProvider.ImageSizes.SMALLICON));
            putValue(SHORT_DESCRIPTION, tr("Execute the script"));
            putValue(NAME, tr("Run"));
            model.addPropertyChangeListener(this);
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ErrorModel.getInstance().clearError();
            final String source = editor.getScript();
            switch(model.getScriptEngineDescriptor().getEngineType()) {
            case EMBEDDED:
                new ScriptExecutor(ScriptingConsolePanel.this)
                    .runScriptWithEmbeddedEngine(source, ErrorModel.getInstance());
                break;
            case PLUGGED:
                new ScriptExecutor(ScriptingConsolePanel.this)
                    .runScriptWithPluggedEngine(
                        model.getScriptEngineDescriptor(),
                        source,
                        ErrorModel.getInstance()
                    );
                break;
            case GRAALVM:
                try {
                    new ScriptExecutor(ScriptingConsolePanel.this)
                        .runScriptWithGraalEngine(
                            model.getScriptEngineDescriptor(),
                            source,
                            ErrorModel.getInstance()
                        );
                } catch(Throwable ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                    throw ex;
                }
                break;
            }
        }

        protected void updateEnabledState() {
            setEnabled(model.getScriptEngineDescriptor() != null);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName()
                    .equals(ScriptEditorModel.PROP_SCRIPT_ENGINE)) {
                return;
            }
            updateEnabledState();
        }
    }

    /**
     * Listens to property changes in the editor model and updates the view
     * accordingly
     * <ul>
     *     <li>updates the content of the error output panel</li>
     *     <li>updates the visual feedback on the tab pane</li>
     * </ul>
     */
    private static class ErrorModelChangeListener implements PropertyChangeListener {
        final JTabbedPane outputTabs;
        final ErrorOutputPanel outputPanel;

        ErrorModelChangeListener(@NotNull final JTabbedPane pane, @NotNull final ErrorOutputPanel outputPanel) {
            Objects.requireNonNull(pane);
            Objects.requireNonNull(outputPanel);
            this.outputTabs = pane;
            this.outputPanel = outputPanel;
        }
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (! ErrorModel.PROP_ERROR.equals(event.getPropertyName())) {
                return;
            }
            if (event.getNewValue() != null) {
                outputTabs.setIconAt(1, ImageProvider.get("bug-solid",
                    ImageProvider.ImageSizes.SMALLICON));
            } else {
                outputTabs.setIconAt(1, ImageProvider.get("circle-check-solid",
                    ImageProvider.ImageSizes.SMALLICON));
            }
            if (event.getNewValue() == null) {
                // clear the panel
                outputPanel.displayException(null);
            } if (event.getNewValue() instanceof Throwable) {
                outputPanel.displayException((Throwable) event.getNewValue());
            }
        }
    }
}
