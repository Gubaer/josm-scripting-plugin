package org.openstreetmap.josm.plugins.scripting.ui.release;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A modal dialog which shows release notes for the current release.
 */
public class ReleaseNotes extends JDialog implements HyperlinkListener {
    static public final String RESOURCE_NAME_STYLE_SHEET = "/release-notes/release-notes.css";
    static public final String RESOURCE_NAME_RELEASE_IDS = "/release-notes/release-notes.properties";
    static public final String PREF_LAST_SEEN_RELEASE_NOTE = ReleaseNotes.class.getName() + ".last-seen-release-note";
    static private final Logger logger = Logger.getLogger(ReleaseNotes.class.getName());

    private JButton btnClose;
    private JEditorPane editorPane;
    private String latestReleaseId;

    /**
     * Replies true if the current user has already seen the release note
     * for the release <code>releaseId</code>.
     *
     * @param releaseId the release id
     * @return if the current user has already seen the release note
     *  for the release <code>releaseId</code>; false, otherwise
     */
    public static boolean hasSeenReleaseNote(@NotNull final String releaseId) {
        Objects.requireNonNull(releaseId);
        var preferences = Preferences.main();
        if (preferences == null) {
            return false;
        }
        var idSeen = preferences.get(PREF_LAST_SEEN_RELEASE_NOTE);
        if (idSeen == null) {
            return false;
        }
        return idSeen.equals(releaseId);
    }

    /**
     * Replies true if the current user has seen the release notes of
     * the latest release
     *
     * @return true if the current user has seen the release notes of
     *   the latest release; false, otherwise
     */
    public static boolean hasSeenLatestReleaseNotes() {
        var ids = loadReleaseIdsWithReleaseNotes();
        if (ids.isEmpty()) {
            return true;
        }
        var id = ids.get(0);
        return hasSeenReleaseNote(id);
    }

    /**
     * Remember that the current user has seen the release notes for release
     * <code>releaseId</code>.
     *
     * @param releaseId the release id
     */
    public static void rememberReleaseNoteSeen(@NotNull final String releaseId) {
        Objects.requireNonNull(releaseId);
        var preferences = Preferences.main();
        if (preferences == null) {
            return;
        }
        preferences.put(PREF_LAST_SEEN_RELEASE_NOTE, releaseId);
    }

    private static @NotNull java.util.List<String> loadReleaseIdsWithReleaseNotes() {
        final java.util.List<String> empty = java.util.List.of();
        var in = ReleaseNotes.class.getResourceAsStream(RESOURCE_NAME_RELEASE_IDS);
        if (in == null) {
            logger.warning(MessageFormat.format("Resource '{0}' not found", RESOURCE_NAME_RELEASE_IDS));
            return empty;
        }
        try {
            var properties = new Properties();
            properties.load(in);
            var ids = properties.getProperty("release-notes");
            if (ids == null) {
                logger.warning(MessageFormat.format("Property '{0}' not found in resource '{1}'",
                    "release-notes", RESOURCE_NAME_RELEASE_IDS));
                return empty;
            }
            return Arrays.stream(ids.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        } catch(IOException e) {
            logger.log(Level.WARNING, MessageFormat.format("Failed to load resource '{0}'", RESOURCE_NAME_RELEASE_IDS), e);
            return empty;
        }
    }

    private static @Null String loadReleaseNote(final String releaseId) {
        final var resourceName = MessageFormat.format("/release-notes/{0}.html", releaseId);
        var in = ReleaseNotes.class.getResourceAsStream(resourceName);
        if (in == null) {
            logger.warning(MessageFormat.format("Resource '{0}' not found", resourceName));
            return null;
        }
        try {
            try (var reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            logger.log(Level.WARNING,
                MessageFormat.format("Failed to read content of resource '{0}'", resourceName), e);
            return null;
        }
    }

    private static @Null StyleSheet loadStyleSheet() {
        var styleSheet = new StyleSheet();
        final var styleSheetUri =
            ReleaseNotes.class.getResource(RESOURCE_NAME_STYLE_SHEET);
        if (styleSheetUri == null) {
            logger.warning(String.format(
                "failed to load default CSS style sheet from resource '%s'",
                RESOURCE_NAME_STYLE_SHEET
            ));
            return null;
        }
        styleSheet.importStyleSheet(styleSheetUri);
        return styleSheet;
    }

    public ReleaseNotes(Component parent) {
        super(JOptionPane.getFrameForComponent(parent),
                ModalityType.DOCUMENT_MODAL);
        build();
        // load the release notes for the newest release
        var ids =loadReleaseIdsWithReleaseNotes();
        if (! ids.isEmpty()) {
            latestReleaseId = ids.get(0);
            var content = loadReleaseNote(latestReleaseId);
            if (content != null) {
                editorPane.setText(content);
            }
        }
    }

    private JEditorPane buildReleaseNotesPanel() {

        // prepare editor pane
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        // prepare editor kit
        var kit = new HTMLEditorKit();
        // load and set style sheet
        var styleSheet = loadStyleSheet();
        if (styleSheet != null) {
            kit.setStyleSheet(styleSheet);
        }
        editorPane.setEditorKit(kit);
        editorPane.addHyperlinkListener(this);
        return editorPane;
    }

    private JPanel buildButtonPanel() {
        var pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER));
        btnClose = new JButton(new CloseAction());
        pnl.add(btnClose);
        return pnl;
    }

    private void build() {
        setTitle(tr("Scripting Plugin - Release Notes"));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(
            new JScrollPane(buildReleaseNotesPanel()),
            BorderLayout.CENTER
        );

        contentPane.add(buildButtonPanel(), BorderLayout.SOUTH);
        addWindowListener(new OnCloseAdapter());
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            btnClose.requestFocusInWindow();
            WindowGeometry
                .centerInWindow(getParent(), new Dimension(400, 600))
                .applySafe(this);
        }
        super.setVisible(visible);
    }

    private void onDialogClosing() {
        setVisible(false);
        if (latestReleaseId != null) {
            rememberReleaseNoteSeen(latestReleaseId);
        }
    }

    class CloseAction extends AbstractAction {
        CloseAction() {
            putValue(NAME, tr("Close"));
            putValue(SHORT_DESCRIPTION, tr("Close the release notes"));
            putValue(SMALL_ICON, ImageProvider.get("ok", ImageProvider.ImageSizes.SMALLICON));
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            onDialogClosing();
        }
    }

    class OnCloseAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            onDialogClosing();
        }
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ex) {
                    logger.log(Level.WARNING,
                        MessageFormat.format("Failed to open desktop browser with URL '{0}'", e.getURL()),
                        ex);
                }
            }
        }
    }
}
