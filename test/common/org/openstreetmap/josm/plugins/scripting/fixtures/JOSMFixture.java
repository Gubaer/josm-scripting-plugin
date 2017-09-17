package org.openstreetmap.josm.plugins.scripting.fixtures;

import static org.junit.Assert.fail;
import java.io.File;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.logging.Logger;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.I18n;

import static java.text.MessageFormat.format;

public class JOSMFixture {
    static private final Logger logger = Logger.getLogger(JOSMFixture.class.getName());

    public JOSMFixture() throws Exception {
        String josmHome = System.getProperty("josm.home");

        if (josmHome == null) {
            josmHome = "test/josm.home";
            logger.info(format("system property ''josm.home'' not set. "
                + "Setting it to the default value ''{0}''", new File(josmHome).getAbsolutePath()));
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
        logger.info(format("''josm.home'': using directory ''{0}''", f.getAbsolutePath()));

        I18n.init();
        // initialize the plaform hook, and
        Main.determinePlatformHook();
        // call the really early hook before we anything else
        Main.platform.preStartupHook();

        Main.pref.init(false);
        Config.setPreferencesInstance(Main.pref);
        
        // init projection
        ProjectionPreference.setProjection("core:mercator", null, true /* default */);

        // make sure we don't upload to or test against production
        //
        String url = OsmApi.getOsmApi().getBaseUrl().toLowerCase().trim();
        if (url.startsWith("http://www.openstreetmap.org")
                || url.startsWith("http://api.openstreetmap.org")) {
            fail(MessageFormat.format("configured server url ''{0}'' seems to be a productive url, aborting.", url));
        }
    }
}
