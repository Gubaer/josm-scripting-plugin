package org.openstreetmap.josm.plugins.scripting.fixtures;

import org.openstreetmap.josm.actions.DeleteAction;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.preferences.JosmBaseDirectories;
import org.openstreetmap.josm.data.preferences.JosmUrls;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainApplicationTest;
import org.openstreetmap.josm.gui.MainInitialization;
import org.openstreetmap.josm.gui.layer.LayerManagerTest;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.CertificateAmendment;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.spi.lifecycle.Lifecycle;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.*;
import org.openstreetmap.josm.tools.date.DateUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

//TODO: should be replaced by josm/test/unit/org.openstreemap.josm.JOSMFixture
//currently partially copy/pasted from this class
public class JOSMFixture {
    static public final String DEFAULT_JOSM_HOME = "build/josm.home";
    static private final Logger logger = Logger.getLogger(JOSMFixture.class.getName());

    private String josmHome;

    public String getJosmHome() {
        return josmHome;
    }

    public JOSMFixture(boolean createGui) throws Exception {
        HttpClient.setFactory(Http1Client::new);

        josmHome = System.getProperty("josm.home");

        if (josmHome == null) {
            josmHome = DEFAULT_JOSM_HOME;
            logger.info(format("system property ''josm.home'' not set. "
                + "Setting it to the default value ''{0}''",
                    new File(josmHome).getAbsolutePath()));
        }
        final File f = new File(josmHome);
        if (! f.exists() ) {
            logger.warning(format("directory ''{0}'' doesn''t exist. Creating it.",
                f.getAbsolutePath()));
            Files.createDirectories(f.toPath());
        } else if (! f.isDirectory() || ! f.canWrite()) {
            fail(format("''{0}'' is either not a directory or not writable. Aborting.", 
                f.getAbsolutePath()));
        }
        logger.info(format("''josm.home'': using directory ''{0}''",
            f.getAbsolutePath()));

        System.setProperty("josm.home", new File(josmHome).getAbsolutePath());
        TimeZone.setDefault(DateUtils.UTC);
        Preferences pref = Preferences.main();
        Config.setPreferencesInstance(pref);
        Config.setBaseDirectoriesProvider(JosmBaseDirectories.getInstance());
        Config.setUrlsProvider(JosmUrls.getInstance());
        pref.resetToInitialState();
        pref.enableSaveOnPut(false);
        I18n.init();
        // initialize the plaform hook, and
        // call the really early hook before we anything else
        PlatformManager.getPlatform().preStartupHook();

        Logging.setLogLevel(Logging.LEVEL_INFO);
        pref.init(false);
        String url = Config.getPref().get("osm-server.url");
        if (url == null || url.isEmpty() || isProductionApiUrl(url)) {
            Config.getPref().put(
                "osm-server.url",
                "https://api06.dev.openstreetmap.org/api");
        }
        I18n.set(Config.getPref().get("language", "en"));

        try {
            CertificateAmendment.addMissingCertificates();
        } catch (IOException | GeneralSecurityException ex) {
            throw new JosmRuntimeException(ex);
        }

        // init projection
        ProjectionRegistry.setProjection(
            Projections.getProjectionByCode("EPSG:3857"));

        // setup projection grid files
        MainApplication.setupNadGridSources();


        // make sure we don't upload to or test against production
        url = OsmApi.getOsmApi().getBaseUrl().toLowerCase(Locale.ENGLISH).trim();
        if (isProductionApiUrl(url)) {
            fail(MessageFormat.format(
                "configured server url ''{0}'' seems to be a productive url, " +
                "aborting.", url));
        }

        // Setup callbacks
        DeleteCommand.setDeletionCallback(DeleteAction.defaultDeletionCallback);

        if (createGui) {
            GuiHelper.runInEDTAndWaitWithException(this::setupGUI);
        }
    }

    private static boolean isProductionApiUrl(String url) {
        return url.startsWith("http://www.openstreetmap.org")
            || url.startsWith("http://api.openstreetmap.org")
            || url.startsWith("https://www.openstreetmap.org")
            || url.startsWith("https://api.openstreetmap.org");
    }

    private void setupGUI() {
        JOSMTestRules.cleanLayerEnvironment();
        assertTrue(MainApplication.getLayerManager().getLayers().isEmpty());
        assertNull(MainApplication.getLayerManager().getEditLayer());
        assertNull(MainApplication.getLayerManager().getActiveLayer());

        initContentPane();
        initMainPanel();
        initToolbar();
        if (MainApplication.getMenu() == null) {
            Lifecycle.initialize(new MainInitialization(new MainApplication()));
        }
        // Add a test layer to the layer manager to get the MapFrame
        MainApplication.getLayerManager().addLayer(
                new LayerManagerTest.TestLayer());
    }

    /**
     * Make sure {@code MainApplication.contentPanePrivate} is initialized.
     */
    private static void initContentPane() {
        MainApplicationTest.initContentPane();
    }

    /**
     * Make sure {@code MainApplication.mainPanel} is initialized.
     */
    private static void initMainPanel() {
        MainApplicationTest.initMainPanel(false /* don't re-add listeners */);
    }

    /**
     * Make sure {@code MainApplication.toolbar} is initialized.
     */
    private static void initToolbar() {
        MainApplicationTest.initToolbar();
    }
}
