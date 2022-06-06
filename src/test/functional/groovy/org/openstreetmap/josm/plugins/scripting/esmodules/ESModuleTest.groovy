package org.openstreetmap.josm.plugins.scripting.esmodules


import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.FileSystem
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import java.util.logging.Logger

class ESModuleTest extends JOSMFixtureBasedTest {
    static class CustomFileSystem implements FileSystem {
        static private final Logger logger = Logger.getLogger(CustomFileSystem.class.name)

        @Override
        Path parsePath(URI uri) {
            logger.info("uri=${uri?.toString()}")
            return null
        }

        @Override
        Path parsePath(String path) {
            logger.info("path=${path?.toString()}")

            final sourcePath = new File(getProjectHome(), "src/test/resources/es-modules/${path}.mjs").toPath()
            logger.info("path expanded: ${sourcePath.toString()}")
            return sourcePath
        }

        @Override
        void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {

        }

        @Override
        void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {

        }

        @Override
        void delete(Path path) throws IOException {

        }

        @Override
        SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            return Files.newByteChannel(path)
        }

        @Override
        DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
            return null
        }

        @Override
        Path toAbsolutePath(Path path) {
            logger.info("path=$path")
            return path
        }

        @Override
        Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
            logger.info("path=$path")
            return path
        }

        @Override
        Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
            return null
        }
    }

    @Test
    void "should import module"() {
        final Context cx = Context.newBuilder("js")
           .fileSystem(new CustomFileSystem())
           .allowIO(true)
           .allowHostClassLookup(s -> true)
           .allowHostAccess(HostAccess.ALL)
           .build()
        cx.enter()

        final src= """
        import {version} from 'josm'
        version
        """

        // have to supply a file name with extension .msj or
        // mime-type application/javascript+module. Otherwise,
        // import of ES modules isn't supported
        final source = Source.newBuilder("js", src, null)
            .mimeType("application/javascript+module")
            .build()
        final version = cx.eval(source)
        assertNotNull(version)
        println("Version: ${version?.asString()}")
    }
}
