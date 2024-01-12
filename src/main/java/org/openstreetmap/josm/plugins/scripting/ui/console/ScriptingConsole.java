package org.openstreetmap.josm.plugins.scripting.ui.console;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.util.WindowGeometry;

@SuppressWarnings("unused")
public class ScriptingConsole extends JFrame {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(ScriptingConsole.class.getName());

    static public final BooleanProperty PREF_ALWAYS_ON_TOP =
        new BooleanProperty(
            ScriptingConsole.class.getName() + ".alwaysOnTop",
            true
        );

    private JCheckBox cbAlwaysOnTop;

    private static ScriptingConsole instance = null;

    public static ScriptingConsole getInstance() {
        return instance;
    }

    /**
     * Displays the scripting console and puts it to front. Creates a console
     * if no console exists yet.
     */
    public static void showScriptingConsole() {
        synchronized (ScriptingConsole.class) {
            if (instance == null) {
                instance = new ScriptingConsole();
                instance.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        final ScriptingConsole old = instance;
                        instance = null;
                        fireScriptingConsoleChanged(old, instance);
                    }
                });
            }
            instance.setVisible(true);
            instance.toFront();
        }
    }

    /**
     * Hides and destroys the current scripting console.
     */
    public static void hideScriptingConsole() {
        synchronized (ScriptingConsole.class) {
            if (instance != null) {
                WindowEvent wev = new WindowEvent(instance, WindowEvent.WINDOW_CLOSING);
                Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
            }
        }
    }

    /**
     * Toggles whether there is a scripting console or not
     */
    public static void toggleScriptingConsole() {
        if (instance == null) {
            showScriptingConsole();
        } else {
            hideScriptingConsole();
        }
    }

    private ScriptingConsolePanel pnlScriptingConsole;

    private ScriptingConsole() {
        super(tr("Scripting Console"));
        build();
    }

    protected JMenuBar buildMenuBar() {
        // create the file menu
        //
        final JMenu mnuFile = new JMenu(tr("File"));
        mnuFile.add(new OpenAction());
        final SaveAction act = new SaveAction();
        getScriptEditorModel().addPropertyChangeListener(act);
        mnuFile.add(act);
        mnuFile.add(new SaveAsAction());
        mnuFile.addSeparator();
        mnuFile.add(new CloseAction());

        // create the edit menu
        //
        final JMenu mnuEdit = new JMenu(tr("Edit"));
        mnuEdit.add(pnlScriptingConsole.getScriptLog().getClearAction());

        final JMenuBar bar = new JMenuBar();
        bar.add(mnuFile);
        bar.add(mnuEdit);
        return bar;
    }

    protected JPanel buildControlPanel() {
        final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbAlwaysOnTop = new JCheckBox(new ToggleAlwaysOnTopAction());
        cbAlwaysOnTop.setFont(UIManager.getFont("Label.font").deriveFont(10f));
        pnl.add(cbAlwaysOnTop);
        return pnl;
    }

    protected void build() {
        final Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(pnlScriptingConsole = new ScriptingConsolePanel(),
                BorderLayout.CENTER);
        c.add(buildControlPanel(), BorderLayout.SOUTH);

        setJMenuBar(buildMenuBar());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setIconImage(ImageProvider.get("script-engine").getImage());
    }

    public void setVisible(boolean visible) {
        if (visible) {
            new WindowGeometry(
                ScriptingConsole.class.getName() + ".geometry",
                WindowGeometry.centerInWindow(this, new Dimension(500, 800))
            ).applySafe(this);
        } else {
            new WindowGeometry(this).remember(ScriptingConsole.class.getName() + ".geometry");
        }
        super.setVisible(visible);
    }

    /**
     * Loads the file {@code file} in the script editor.
     *
     * @param file the file. Must not be null
     */
    public void open(@NotNull File file) {
        Objects.requireNonNull(file);
        Assert.assertArg(file.isFile(), "Expected a file, got a directory. File is: {0}", file);
        Assert.assertArg(file.canRead(), "Expected a readable file, but can''t read file. File is: {0}", file);
        pnlScriptingConsole.open(file);
    }

    /**
     * Saves the content of the script editor to the file {@code file}.
     *
     * @param file the file. Must not be null
     */
    public void save(@NotNull File file) {
        Objects.requireNonNull(file);
        pnlScriptingConsole.save(file);
    }

    public void save() {
        pnlScriptingConsole.save();
    }

    public ScriptEditorModel getScriptEditorModel() {
        return pnlScriptingConsole.getScriptEditorModel();
    }

    /**
     * Replies the script log.
     *
     * @return the script log
     */
    public IScriptLog getScriptLog() {
        return pnlScriptingConsole.getScriptLog();
    }

    private class ToggleAlwaysOnTopAction extends AbstractAction {
        public ToggleAlwaysOnTopAction() {
            putValue(NAME, tr("Always on top"));
            putValue(SELECTED_KEY, PREF_ALWAYS_ON_TOP.get());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        }
    }

    /* --------------------------------------------------------------------- */
    /* listening to scripting console events                                 */
    /* --------------------------------------------------------------------- */
    public interface ScriptingConsoleListener {
        /**
         * Notifies listeners when the scripting console instance changes.
         * If {@code newValue} is null, no scripting console is open.
         *
         * @param oldValue old value
         * @param newValue new value
         */
        void scriptingConsoleChanged(ScriptingConsole oldValue, ScriptingConsole newValue);
    }

    private static final CopyOnWriteArrayList<ScriptingConsoleListener>
            listeners = new CopyOnWriteArrayList<>();

    public static void addScriptingConsoleListener(ScriptingConsoleListener l) {
        if (l == null) return;
        listeners.addIfAbsent(l);
    }

    public static void removeScriptingConsoleListener(ScriptingConsoleListener l) {
        if (l == null) return;
        listeners.remove(l);
    }

    protected static void fireScriptingConsoleChanged(ScriptingConsole oldValue, ScriptingConsole newValue) {
        for (ScriptingConsoleListener l : listeners) {
            l.scriptingConsoleChanged(oldValue, newValue);
        }
    }
}
