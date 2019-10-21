package org.openstreetmap.josm.plugins.scripting.graalvm

import org.junit.Ignore
import org.junit.Test

import java.nio.file.Paths
import static org.junit.Assert.assertEquals

/**
 * test cases to explore the behaviour of
 * {@link java.nio.file.Path#resolve()}
 *
 * Annotated with @Ignore, because doesn't belong to the set of unit
 * tests for the scripting plugin.
 */
@Ignore
class PathsResolveTest {

    @Test
    void "resolve toplevel module id"() {
        def path = Paths.get(new File("/foo/bar").toURI())
                .resolve("toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve toplevel module id against path with trailing /"() {
        def path = Paths.get(new File("/foo/bar/").toURI())
                .resolve("toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with ./"() {
        def path = Paths.get(new File("/foo/bar").toURI())
                .resolve("./toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with ./ against path with trailing /"() {
        def path = Paths.get(new File("/foo/bar/").toURI())
                .resolve("./toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with ../"() {
        def path = Paths.get(new File("/foo/bar/sub").toURI())
                .resolve("../toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }

    @Test
    void "resolve relative module id with ../ against path with trailing /"() {
        def path = Paths.get(new File("/foo/bar/sub/").toURI())
                .resolve("../toplevel")
                .normalize()
        assertEquals("/foo/bar/toplevel", path.toString())
    }


    @Test
    void "resolve relative module id with too many .. segments"() {
        def path = Paths.get(new File("/foo/bar/sub/").toURI())
                .resolve("../../../../../toplevel")
                .normalize()
        assertEquals("/toplevel", path.toString())
    }

    @Test
    void "resolve module id with leading /"() {
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

        def moduleRepoPath = Paths.get("/", basePath.relativize(modulePath).toString());
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
