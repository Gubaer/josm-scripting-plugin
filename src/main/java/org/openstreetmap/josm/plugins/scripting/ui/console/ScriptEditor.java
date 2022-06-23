package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.scripting.ui.GridBagConstraintBuilder.gbc;
import static org.openstreetmap.josm.plugins.scripting.util.FileUtils.buildTextFileReader;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Editor for scripts. Basically a minimal text editor with syntax
 * highlighting for various scripting languages, based on
 * <a href="https://bobbylight.github.io/RSyntaxTextArea/">RSyntaxPane</a>.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ScriptEditor extends JPanel implements PropertyChangeListener {

    static private final Logger logger = Logger.getLogger(ScriptEditor.class.getName());

    private static java.util.List<String> getAvailableSyntaxConstants() {
        return Arrays.stream(SyntaxConstants.class.getDeclaredFields())
            .filter(field ->
                Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("SYNTAX_STYLE_")
            )
            .map(field -> {
                final String value = "";
                field.setAccessible(true);
                try {
                    return (String) field.get(value);
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, MessageFormat.format(
                        "Failed to read syntax style constant '{0}'",
                        field.getName()
                    ), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private ScriptEditorModel model;
    private JLabel lblScriptFile;
    private JPanel pnlScriptFile;
    private RSyntaxTextArea editor;

    private JPanel buildNorthPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        ScriptEngineInfoPanel p  = new ScriptEngineInfoPanel(model);
        pnl.add(p, gbc().cell(0,0).weight(1.0,0.0).constraints());

        // the label where we display the current file name
        pnlScriptFile = new JPanel(new GridBagLayout());
        pnlScriptFile.add(new JLabel(tr("Current file:") + " "),
                gbc().cell(0,0).weight(0.0, 1.0).constraints());
        pnlScriptFile.add(lblScriptFile = new JLabel(""),
                gbc().cell(1,0).weight(1.0, 1.0).constraints());
        lblScriptFile.setFont(Font.decode("DialogInput-PLAIN"));
        pnl.add(pnlScriptFile, gbc().cell(0,1).weight(1.0,0.0).constraints());
        return pnl;
    }

    protected void build() {
        model = new ScriptEditorModel();
        setLayout(new BorderLayout());
        add(buildNorthPanel(), BorderLayout.NORTH);

        editor = new RSyntaxTextArea();
        add(new RTextScrollPane(editor), BorderLayout.CENTER);
        // for context type text/plain
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        editor.setCodeFoldingEnabled(false);
        editor.setWhitespaceVisible(true);
    }

    public @NotNull String lookupSyntaxConstants(
            @NotNull String mimeType) {
        Objects.requireNonNull(mimeType);
        var normalizedMimeType = mimeType.toLowerCase();
        return SyntaxConstantsEngine
            .getInstance().deriveSyntaxStyle(normalizedMimeType);
    }

    public void changeSyntaxEditingStyle(@NotNull String syntaxStyle){
        editor.setSyntaxEditingStyle(syntaxStyle);
    }

    protected void refreshScriptFile() {
        final  Optional<File> file = model.getScriptFile();
        lblScriptFile.setText(
           file.map(File::getAbsolutePath).orElse(""));
        pnlScriptFile.setVisible(file.isPresent());
    }

    public ScriptEditor() {
        build();
        model.addPropertyChangeListener(this);
        refreshScriptFile();
    }

    public Document getDocument() {
        return editor.getDocument();
    }

    /**
     * <p>Replies the currently edited script.</p>
     *
     * @return the currently edited script
     */
    public String getScript() {
        return editor.getText();
    }

    /**
     * Replies the script editor model.
     *
     * @return the script editor model
     */
    public ScriptEditorModel getModel() {
        return model;
    }


    protected void alertIOExceptionWhenLoading(File file, IOException e){
        HelpAwareOptionPane.showOptionDialog(
            this,
            tr("Failed to load file ''{0}''.", file),
            tr("IO exception"),
            JOptionPane.ERROR_MESSAGE,
            null // no help topic
        );
    }

    protected void alertIOExceptionWhenSaving(File file, IOException e){
        HelpAwareOptionPane.showOptionDialog(
            this,
            tr("Failed to save file ''{0}''.", file),
            tr("IO exception"),
            JOptionPane.ERROR_MESSAGE,
            null // no help topic
        );
    }

    /**
     * Loads the script from file {@code file} into the text editor.
     *
     * @param file the file. Must not be null. A readable file is expected.
     */
    public void open(File file) {
        final Document doc = editor.getDocument();
        try(final BufferedReader reader =
                    new BufferedReader(buildTextFileReader(file))) {
            doc.remove(doc.getStartPosition().getOffset(), doc.getLength());
            for(Iterator<String> it = reader.lines().iterator();it.hasNext();) {
                String line = it.next();
                doc.insertString(doc.getLength(), line, null);
                doc.insertString(doc.getLength(), "\n", null);
            }
        } catch(BadLocationException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
            alertIOExceptionWhenLoading(file, e);
        }
        model.setScriptFile(file);
    }

    /**
     * Saves the content of the script editor to the file {@code file}.
     * <p>
     * Exceptions are handled internally.
     *
     * @param file the output file. Must not be null.
     */
    public void save(@NotNull File file) {
        Objects.requireNonNull(file);
        String script = editor.getText();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))){
            writer.print(script);
        } catch(IOException e){
            e.printStackTrace();
            alertIOExceptionWhenSaving(file, e);
        }
        model.setScriptFile(file);
    }

    /* ---------------------------------------------------------------------- */
    /* interface PropertyChangeListener                                       */
    /* ---------------------------------------------------------------------- */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ScriptEditorModel.PROP_SCRIPT_FILE)) {
            refreshScriptFile();
        }
    }
}
