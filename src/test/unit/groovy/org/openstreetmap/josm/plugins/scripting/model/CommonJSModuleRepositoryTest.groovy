package org.openstreetmap.josm.plugins.scripting.model

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.BaseTestCase

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.assertEquals

class CommonJSModuleRepositoryTest extends BaseTestCase{

    @Test
    void createWithFile() {
        def path = new File(getProjectHome().toString(), "path/to/a/directory")
        def repo = new CommonJSModuleRepository(path)
        assertEquals(path, repo.getFile())

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((File)null)
        }
    }

    @Test
    void createWithUrl() {
        def path = new File(getProjectHome(), "path/to/a/directory")
        def repo = new CommonJSModuleRepository(path.toURI().toURL())
        assertEquals(path, repo.getFile())


        path = new File(getProjectHome(), "path/to/a/directory/jarfile.jar")
        def url = new URL("jar:${path.toURI()}!/")
        repo = new CommonJSModuleRepository(url)
        assertEquals(path, repo.getFile())
        assertEquals("/", repo.getJarEntry())

        url = new URL("jar:${path.toURI()}!/my/module/directory")
        repo = new CommonJSModuleRepository(url)
        assertEquals(path, repo.getFile())
        assertEquals("/my/module/directory", repo.getJarEntry())

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((URL)null)
        }
    }

    @Test
    void createWithString() {
        def path = new File(getProjectHome().toString(), "path/to/a/directory")
        def repo = new CommonJSModuleRepository(path.toURI().toURL().toString())
        assertEquals(path, repo.getFile())

        path = new File(getProjectHome(), "path/to/a/directory/jarfile.jar")
        def url = new URL("jar:${path.toURI()}!/")
        repo = new CommonJSModuleRepository(url.toString())
        assertEquals(path, repo.getFile())
        assertEquals("/", repo.getJarEntry())

        url = new URL("jar:${path.toURI()}!/my/module/directory")
        repo = new CommonJSModuleRepository(url.toString())
        assertEquals(path, repo.getFile())
        assertEquals("/my/module/directory", repo.getJarEntry())

        shouldFail(NullPointerException) {
            repo = new CommonJSModuleRepository((String)null)
        }

        shouldFail(IllegalArgumentException) {
            url = "jar:http://test.test/dir/myjar.jar!/my/dir"
            repo = new CommonJSModuleRepository(url)
        }
    }
}
