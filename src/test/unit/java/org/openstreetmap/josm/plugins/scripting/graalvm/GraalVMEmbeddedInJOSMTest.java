package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.plugins.PluginException;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GraalVMEmbeddedInJOSMTest {

    static JOSMFixture fixture;

    private IGraalVMFacade facade;

    @BeforeClass
    public static void init() throws Exception {
        fixture = new JOSMFixture(true);
    }

    @Before
    public void setup() throws PluginException, IOException {
        final String projectDirEnv =
            System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
        final File projectDir =
            new File(projectDirEnv == null ? "." : projectDirEnv);

        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir,  "javascript/").toURI().toURL());
        JOSMModuleScriptProvider.getInstance().addRepository(
            new File(projectDir,  "test/script-api/").toURI().toURL());
        new ScriptingPlugin(new PluginInformation(
            new File(projectDir, "dist/scripting.jar")));

        facade = GraalVMFacadeFactory.getOrCreateGraalVMFacade();
        Logging.getLogger().setFilter(
            record -> record.getLevel().intValue() >= Level.WARNING.intValue());
    }

    protected ScriptEngineDescriptor getDescriptorForJavaScript() {
        assertTrue(GraalVMFacadeFactory.isGraalVMPresent());
        final Optional<ScriptEngineDescriptor> descriptor =
            facade.getScriptEngineDescriptors()
                .stream()
                .filter(desc -> "JavaScript".equals(desc.getLanguageName().orElse(null)))
                .findAny();
        if (descriptor.isEmpty()) {
            fail("no script engine descriptor for language 'JavaScript' found");
        }
        return descriptor.get();
    }

    @Test
    public void shouldAccessJosmVersionClass() throws GraalVMEvalException {
        final ScriptEngineDescriptor descriptor = getDescriptorForJavaScript();
        final String script =
              "const Version = Java.type('org.openstreetmap.josm.data.Version')\n"
            + "const version = Version.getInstance().getVersionString()\n"
            + "console.log(`JOSM Version: ${version}`)";
        facade.eval(descriptor, script);
    }
}
