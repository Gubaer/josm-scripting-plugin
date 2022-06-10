package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class JarESModuleRepository extends AbstractESModuleRepository {
    private static final Logger logger = Logger.getLogger(JarESModuleRepository.class.getName());

    static private void logFine(Supplier<String> supplier) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(supplier.get());
        }
    }
    private final JarFile jar;
    private final File jarFile;
    private final Path root;

    private static Pattern LEADING_SLASHES = Pattern.compile("^/+");
    static private String removeLeadingSlashes(String path) {
        return LEADING_SLASHES.matcher(path).replaceFirst("");
    }

    private static final List<String> SUFFIXES = List.of("", ".mjs", ".js");

    private ZipEntry resolveZipEntryPath(@NotNull Path relativeModulePath) {
        if (!relativeModulePath.isAbsolute()) {
            relativeModulePath = Path.of(removeLeadingSlashes(relativeModulePath.toString()));
        }
        final var path = relativeModulePath.normalize();
        return SUFFIXES.stream().map(suffix -> path + suffix)
            .map(p -> jar.getEntry(p))
            .filter(Objects::nonNull)
            .filter(entry -> !entry.isDirectory())
            .findFirst()
            .orElse(null);
    }

    /**
     * Creates a repository of ES Modules stored in a jar file.
     *
     * @param jarFile the jar file
     * @throws IOException if <code>jarFile</code> doesn't exist or isn't a jar file
     * @throws NullPointerException if <code>jarFile</code> is null
     */
    public JarESModuleRepository(@NotNull final File jarFile) throws IOException {
        Objects.requireNonNull(jarFile);
        this.jarFile = jarFile;
        this.jar = new JarFile(jarFile);
        this.root = Path.of("/");
    }

    /**
     * Creates a repository of ES Modules stored in a jar file. ES Modules are stored
     * in the subtree of jar entries given by <code>rootEntry</code>.
     *
     * @param jarFile the jar file
     * @param rootEntry the path to the root entry for the repository
     * @throws IOException if <code>jarFile</code> doesn't exist or isn't a jar file
     * @throws IOException if <code>jarFile</code> or <code>rootEntry</code> is null
     * @throws IllegalArgumentException if there isn't an entry with name <code>rootEntry</code> in the <code>jarFile</code>
     */
    public JarESModuleRepository(@NotNull final File jarFile, @NotNull final String rootEntry) throws IOException {
        Objects.requireNonNull(jarFile);
        Objects.requireNonNull(rootEntry);
        this.jarFile = jarFile;
        this.jar = new JarFile(jarFile);
        var path = Path.of(rootEntry);
        if (path.isAbsolute()) {
            this.root = path.normalize();
        } else {
            this.root = Path.of("/", path.toString()).normalize();
        }
        var entry = this.jar.getEntry(this.root.toString());
        if (entry == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Root entry ''{0}'' not found in jar file ''{1}''",
                this.root.toString(),
                this.jarFile.getAbsolutePath()
            ));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null Path resolveModulePath(@NotNull String modulePath) {
        Objects.requireNonNull(modulePath);
        return resolveModulePath(Path.of(modulePath));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null Path resolveModulePath(@NotNull Path modulePath) {
        Objects.requireNonNull(modulePath);
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path absolutePath) throws IOException {
        return null;
    }
}
