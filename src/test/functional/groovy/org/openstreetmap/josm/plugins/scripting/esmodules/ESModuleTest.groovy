package org.openstreetmap.josm.plugins.scripting.esmodules

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.FileSystem
import org.junit.jupiter.api.Test
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

import javax.naming.OperationNotSupportedException
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import java.util.logging.Level
import java.util.logging.Logger

import static org.junit.Assert.assertNotNull

class ESModuleTest extends JOSMFixtureBasedTest {
    static class CustomFileSystem implements FileSystem {
        static private final Logger logger = Logger.getLogger(CustomFileSystem.class.name)
        private java.nio.file.FileSystem fs = FileSystems.default
        private File root

        CustomFileSystem(File root) {
            final rootDirectories = fs.rootDirectories.join("\n")
            logger.info("CustomFileSystem: rootDirectories: $rootDirectories")
            this.root = root
        }

        @Override
        Path parsePath(URI uri) {
            logger.info("parsePath: uri=${uri?.toString()}")
            return null
        }

        @Override
        Path parsePath(String pathAsString) {
            logger.info("parsePath: path=${pathAsString?.toString()}")
            final path = new File(pathAsString).toPath()
            if (path.isAbsolute()) {
                if (path.startsWith(root.toPath())) {
                    return path
                } else {
                    logger.info("parsePath: path '$path' not below root")
                    return fs.getPath(pathAsString)
                }
            } else {
                final sourcePath = new File(root, pathAsString).toPath()
                logger.info("parsePath: path expanded: ${sourcePath.toString()}")
                return sourcePath
            }
        }

        @Override
        void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
            logger.info("checkAccess: path=$path, modes=${modes.join(",")}")
        }

        @Override
        void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
            throw new OperationNotSupportedException()
        }

        @Override
        void delete(Path path) throws IOException {
            throw new OperationNotSupportedException()
        }

        @Override
        SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            logger.info("newByteChannel: path=$path")

            return Files.newByteChannel(path)
        }

        @Override
        DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
            logger.info("newDirectoryStream: path=$dir")
            return null
        }

        @Override
        Path toAbsolutePath(final Path path) {
            logger.info("toAbsolutePath: result=$path")
            return path
        }

        @Override
        Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
            final normalizedPathAsString = path.toString().toLowerCase()
            if (normalizedPathAsString.endsWith(".js") || normalizedPathAsString.endsWith(".mjs")) {
                return path
            }
            final suffix = [".mjs", ".js"].find {suffix ->
                final pathWithSuffix = path.resolveSibling("${path.fileName}${suffix}")
                logger.info("toRealPath: checking path with suffix: $pathWithSuffix")
                try {
                    checkAccess(pathWithSuffix,[] as Set<AccessMode>)
                    return pathWithSuffix.toFile().isFile()
                } catch(IOException e) {
                    // file doesn't exist, isn't a file, or can't be read
                    logger.log(Level.INFO, "checkAccessFailed", e)
                    return false
                }
            }
            if (suffix == null) {
                logger.info("toRealPath: result=null")
                return null
            }
            final result = path.resolveSibling("${path.fileName}${suffix}")
            logger.info("toRealPath: result=$result")
            return result
        }

        @Override
        Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
            return null
        }
    }

    @Test
    void "should import module"() {
        final customFS = new CustomFileSystem(new File(getProjectHome(), "src/test/resources/es-modules"))
        final Context cx = Context.newBuilder("js")
           .fileSystem(customFS)
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
