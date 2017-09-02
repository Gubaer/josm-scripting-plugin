package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.ScriptExecutor;
import org.openstreetmap.josm.tools.ImageProvider;

import jsyntaxpane.DefaultSyntaxKit;

/**
 * The panel displaying the script editor and the console log in a split pane.
 *
 */
@SuppressWarnings("serial")
public class ScriptingConsolePanel extends JPanel {
    @SuppressWarnings("unused")
    private static final Logger logger =
        Logger.getLogger(ScriptingConsolePanel.class.getName());

    private JSplitPane spConsole;
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

    protected JSplitPane buildSplitPane() {
        final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setDividerSize(5);
        sp.setTopComponent(buildInputPanel());
        sp.setBottomComponent(log = new ScriptLogPanel());
        SwingUtilities.invokeLater(() ->sp.setDividerLocation(0.7));
        return sp;
    }

    protected void build() {
        DefaultSyntaxKit.initKit();

        spConsole = buildSplitPane();
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


    protected void warnMissingSyntaxKit(ScriptEngineDescriptor desc) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append(tr("Didn''t find a suitable syntax kit for the script engine "
                + "<strong>{0}</strong>.",
                desc.getEngineName().orElse(tr("unknown"))));
        sb.append("<p>");
        sb.append(tr("No syntax kit is configured for either of the following "
                + "content types:"));
        sb.append("<ul>");
        for(String mt: desc.getContentMimeTypes()) {
            sb.append("<li><tt>").append(mt).append("</tt></li>");
        }
        sb.append("</ul>");
        sb.append(tr("Syntax highlighting is going to be disabled."));
        sb.append("<p>");
        sb.append(tr("Refer to the online help how to configure syntax kits "
                + "for specific content types."));
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
                null //FIXME: help topic
        );
    }

    protected void updateScriptContentType(ScriptEngineDescriptor desc) {
        if (desc == null){
            editor.changeContentType("text/plain");
            return;
        }
        final Optional<String> mt = desc.getContentMimeTypes().stream()
            .filter(s -> MimeTypeToSyntaxKitMap.getInstance().isSupported(s))
            .findFirst();
        if (mt.isPresent()) {
            editor.changeContentType(mt.get());
        } else {
            editor.changeContentType("text/plain");
            warnMissingSyntaxKit(desc);
        }
    }

    public ScriptingConsolePanel() {
        build();
    }

    /**
     * <p>Reads the script from file {@code file}</p>
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
     * <p>Replies the script log</p>
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
            putValue(SMALL_ICON, ImageProvider.get("media-playback-start"));
            putValue(SHORT_DESCRIPTION, tr("Execute the script"));
            putValue(NAME, tr("Run"));
            model.addPropertyChangeListener(this);
            updateEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final String source = editor.getScript();
            switch(model.getScriptEngineDescriptor().getEngineType()) {
            case EMBEDDED:
                new ScriptExecutor(ScriptingConsolePanel.this)
                    .runScriptWithEmbeddedEngine(source);
                break;
            case PLUGGED:
                new ScriptExecutor(ScriptingConsolePanel.this)
                    .runScriptWithPluggedEngine(
                        model.getScriptEngineDescriptor(),
                        source
                        );
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
}
