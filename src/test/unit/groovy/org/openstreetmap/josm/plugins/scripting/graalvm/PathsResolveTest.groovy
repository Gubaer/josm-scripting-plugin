package org.openstreetmap.josm.plugins.scripting.graalvm

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.nio.file.Paths

/**
 * test cases to explore the behaviour of
 * {@link java.nio.file.Path#resolve()}
 *
 * Annotated with @Ignore, because doesn't belong to the set of unit
 * tests for the scripting plugin.
 */
@Disabled
class PathsResolveTest extends GroovyTestCase {

    @Test
    void "resolve top level module id"() {
        def path = Paths.get(new File("/foo/bar").toURI())
                .resolve("toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve toplevel module id against path with trailing _slash_"() {
        def path = Paths.get(new File("/foo/bar/").toURI())
                .resolve("toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with _dot__slash_"() {
        def path = Paths.get(new File("/foo/bar").toURI())
                .resolve("./toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with _dot__slash_ against path with trailing _slash_"() {
        def path = Paths.get(new File("/foo/bar/").toURI())
                .resolve("./toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with _dot__dot__slash_"() {
        def path = Paths.get(new File("/foo/bar/sub").toURI())
                .resolve("../toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with _dot__dot__slash_ against path with trailing _slash_"() {
        def path = Paths.get(new File("/foo/bar/sub/").toURI())
                .resolve("../toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }


    @Test
    void "resolve relative module id with too many _dot__dot_ segments"() {
        def path = Paths.get(new File("/foo/bar/sub/").toURI())
                .resolve("../../../../../toplevel")
                .normalize()
        assertEquals("/toplevel", path.toString())
    }

    @Test
    void "resolve module id with leading _slash_"() {
        def path = Paths.get(new File("/foo/bar").toURI())
                .resolve("/toplevel")
                .normalize()
        assertEquals("/toplevel", path.toString())
    }

    @Test
    void "relativize of paths 1"() {
        def basePath = Paths.get(
            new File("/full/path/to/my/repo").toURI())
        def modulePath = Paths.get(
            new File("/full/path/to/my/repo/my/module.index.js").toURI())

        def moduleRepoPath = Paths.get("/",
            basePath.relativize(modulePath).toString())
        println(moduleRepoPath)

    }

    @Test
    void "relativize of paths 2"() {
        def moduleRepoPath = Paths.get(
                new File("/my/module/index.js").toURI())
        def repoBasePath = Paths.get(
                new File("/full/path/to/my/repo").toURI())

        def moduleFilePath = Paths.get(
            repoBasePath.toString(),
            moduleRepoPath.toString())
        println(moduleFilePath)
    }
}
