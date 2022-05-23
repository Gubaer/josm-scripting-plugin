package org.openstreetmap.josm.plugins.scripting.fixtures;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

public class JOSMFixture extends org.openstreetmap.josm.JOSMFixture {
    static public final String DEFAULT_JOSM_HOME = "build/josm.home";
    static private final Logger logger = Logger.getLogger(JOSMFixture.class.getName());

    public static JOSMFixture createFixture(final boolean withGui) throws Exception {
        String josmHome = System.getProperty("josm.home");
        if (josmHome == null) {
            josmHome = DEFAULT_JOSM_HOME;
            logger.info(format("system property ''josm.home'' not set. "
                + "Setting it to the default value ''{0}''",
                new File(josmHome).getAbsolutePath()));
        }
        Files.createDirectories(new File(josmHome).toPath());
        final JOSMFixture fixture = new JOSMFixture(josmHome);
        fixture.init(withGui);
        return fixture;
    }

    public static JOSMFixture createFixture() throws Exception{
        return createFixture(false /* no gui */);
    }

    private final String josmHome;

    public String getJosmHome() {
        return josmHome;
    }

    public JOSMFixture(final String josmHome) throws Exception {
        super(josmHome);
        this.josmHome = josmHome;
    }
}
