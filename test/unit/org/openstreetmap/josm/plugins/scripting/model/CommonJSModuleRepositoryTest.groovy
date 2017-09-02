package org.openstreetmap.josm.plugins.scripting.model;

import static org.junit.Assert.*;
import org.junit.*;

class CommonJSModuleRepositoryTest {

    def shouldFail = new GroovyTestCase().&shouldFail;

    @Test
    public void createWithFile() {
        def path = "/path/to/a/directory";
        def repo = new CommonJSModuleRepository(new File(path));
        assert repo.getFile() == new File(path);

        shouldFail(IllegalArgumentException) {
            repo = new CommonJSModuleRepository((File)null);
        }
    }

    @Test
    public void createWithUrl() {
        def path = "/path/to/a/directory";
        def repo = new CommonJSModuleRepository(new File(path).toURI().toURL());
        assert repo.getFile() == new File(path);


        path = "/path/to/a/directory/jarfile.jar";
        def url = new URL("jar:${new File(path).toURI().toURL()}!/");
        repo = new CommonJSModuleRepository(url);
        assert repo.getFile() == new File(path);
        assert repo.getJarEntry() == "/";

        url = new URL("jar:${new File(path).toURI().toURL()}!/my/module/directory");
        repo = new CommonJSModuleRepository(url);
        assert repo.getFile() == new File(path);
        assert repo.getJarEntry() == "/my/module/directory";

        shouldFail(IllegalArgumentException) {
            repo = new CommonJSModuleRepository((URL)null);
        }
    }

    @Test
    public void createWithString() {
        def path = "/path/to/a/directory";
        def repo = new CommonJSModuleRepository(new File(path).toURI().toURL().toString());
        assert repo.getFile() == new File(path);

        path = "/path/to/a/directory/jarfile.jar";
        def url = new URL("jar:${new File(path).toURI().toURL()}!/");
        repo = new CommonJSModuleRepository(url.toString());
        assert repo.getFile() == new File(path);
        assert repo.getJarEntry() == "/";

        url = new URL("jar:${new File(path).toURI().toURL()}!/my/module/directory");
        repo = new CommonJSModuleRepository(url.toString());
        assert repo.getFile() == new File(path);
        assert repo.getJarEntry() == "/my/module/directory";

        shouldFail(IllegalArgumentException) {
            repo = new CommonJSModuleRepository((String)null);
        }

        shouldFail(IllegalArgumentException) {
            url = "jar:http://test.test/dir/myjar.jar!/my/dir";
            repo = new CommonJSModuleRepository(url);
        }
    }

}
