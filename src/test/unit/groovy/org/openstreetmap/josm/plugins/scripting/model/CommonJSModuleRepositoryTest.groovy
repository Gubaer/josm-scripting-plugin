package org.openstreetmap.josm.plugins.scripting.model

import org.junit.Test

class CommonJSModuleRepositoryTest {

    def shouldFail = new GroovyTestCase().&shouldFail

    @Test
    void createWithFile() {
        def path = "/path/to/a/directory"
        def repo = new CommonJSModuleRepository(new File(path))
        assert repo.getFile() == new File(path)

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((File)null)
        }
    }

    @Test
    void createWithUrl() {
        def path = "/path/to/a/directory"
        def repo = new CommonJSModuleRepository(new File(path).toURI().toURL())
        assert repo.getFile() == new File(path)


        path = "/path/to/a/directory/jarfile.jar"
        def url = new URL("jar:${new File(path).toURI().toURL()}!/")
        repo = new CommonJSModuleRepository(url)
        assert repo.getFile() == new File(path)
        assert repo.getJarEntry() == "/"

        url = new URL("jar:${new File(path).toURI().toURL()}!/my/module/directory")
        repo = new CommonJSModuleRepository(url)
        assert repo.getFile() == new File(path)
        assert repo.getJarEntry() == "/my/module/directory"

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((URL)null)
        }
    }

    @Test
    void createWithString() {
        def path = "/path/to/a/directory"
        def repo = new CommonJSModuleRepository(new File(path).toURI().toURL().toString())
        assert repo.getFile() == new File(path)

        path = "/path/to/a/directory/jarfile.jar"
        def url = new URL("jar:${new File(path).toURI().toURL()}!/")
        repo = new CommonJSModuleRepository(url.toString())
        assert repo.getFile() == new File(path)
        assert repo.getJarEntry() == "/"

        url = new URL("jar:${new File(path).toURI().toURL()}!/my/module/directory")
        repo = new CommonJSModuleRepository(url.toString())
        assert repo.getFile() == new File(path)
        assert repo.getJarEntry() == "/my/module/directory"

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((String)null)
        }

        shouldFail(IllegalArgumentException) {
            url = "jar:http://test.test/dir/myjar.jar!/my/dir"
            repo = new CommonJSModuleRepository(url)
        }
    }

}
