package org.openstreetmap.josm.plugins.scripting


import org.junit.jupiter.api.BeforeAll
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

class JOSMFixtureBasedTest extends BaseTestCase {
    protected static JOSMFixture fixture

    @BeforeAll
    static void initJOSMFixture() throws Exception {
        fixture = JOSMFixture.createFixture(true /* with gui */)
    }
}
