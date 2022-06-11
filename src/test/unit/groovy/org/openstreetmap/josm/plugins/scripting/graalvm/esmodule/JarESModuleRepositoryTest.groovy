package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

class JarESModuleRepositoryTest extends BaseTestCase {

    @Test
    void "can create a repo"() {
        final file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        final repo = new JarESModuleRepository(file)
        assertNotNull(repo)
    }

    @Test
    void "can create a repo with an existing zip entry as root"() {
        def file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        def root = "josm"
        def repo = new JarESModuleRepository(file, root)
        assertNotNull(repo)

        // should work with an absolute path too
        root = "/josm"
        repo = new JarESModuleRepository(file, root)
        assertNotNull(repo)
    }

    @Test
    void "reject creating a repo when jar file doesn't exist"()  {
        final file = new File("no-such-jar.jar")
        shouldFail(IOException) {
            final repo = new JarESModuleRepository(file)
        }
    }

    @Test
    void "reject creating a repo with nulls as parameters"() {
        shouldFail(NullPointerException) {
            final repo = new JarESModuleRepository(null, "foo")
        }
        shouldFail(NullPointerException) {
            final repo = new JarESModuleRepository(new File("foo"), null)
        }
        shouldFail(NullPointerException) {
            final repo = new JarESModuleRepository(null, null)
        }
    }

    @Test
    void "reject creating a repo with non existing zip entry for the root"() {
        final file = new File(getProjectHome(), "src/test/resources/es-modules/es-modules.jar")
        final root = "no/such/root"
        shouldFail(IllegalArgumentException) {
            final repo = new JarESModuleRepository(file, root)
        }
    }
}
