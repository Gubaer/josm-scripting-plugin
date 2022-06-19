package org.openstreetmap.josm.plugins.scripting

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture

class JOSMFixtureBasedTest extends BaseTestCase {
    protected JOSMFixture fixture

    @BeforeEach
    void initJOSMFixture() throws Exception {
        fixture = JOSMFixture.createFixture(true /* with gui */)
    }

    @AfterEach
    void tearDownJOSMFixture() {
        fixture = null
    }
}
