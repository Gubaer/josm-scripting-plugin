package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

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

/**
 * <code>JarESModuleRepository</code> is a repository of ES Modules stored in a jar file.
 */
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

    final private static Pattern LEADING_SLASHES = Pattern.compile("^/+");
    static private String removeLeadingSlashes(String path) {
        return LEADING_SLASHES.matcher(path).replaceFirst("");
    }

    private static final List<String> SUFFIXES = List.of("", ".mjs", ".js");

    private Path resolveZipEntryPath(@NotNull Path relativeModulePath) {
        if (relativeModulePath.isAbsolute()) {
            // paths to zip entries in a jar file don't start with
            // a '/'. Remove leading '/'.
            relativeModulePath = Path.of(removeLeadingSlashes(relativeModulePath.toString()));
        }

        // remove/resolve any './' or '..' in the path and prepend the repository
        //  root
        final var path = Path.of(
            root.toString(),
            relativeModulePath.normalize().toString()
        );

        // try to locate a suitable zip entry
        return SUFFIXES.stream().map(suffix -> path + suffix)
            .filter(p -> {
                var entry = jar.getEntry(p);
                return entry != null && !entry.isDirectory();
            })
            .findFirst()
            .map(Path::of)
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
        this.root = Path.of("");
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
        var path = Path.of(rootEntry).normalize();
        if (path.isAbsolute()) {
            this.root = Path.of(removeLeadingSlashes(path.toString()));
        } else {
            this.root = path;
        }
        var entry = this.jar.getEntry(this.root.toString());
        if (entry == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Root entry ''{0}'' not found in jar file ''{1}''",
                rootEntry,
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
        if (modulePath.isAbsolute()) {
            var normalizedModulePath= modulePath.normalize();
            logFine(() -> MessageFormat.format(
                "{0}: normalized module path is ''{1}''",
                modulePath,
                normalizedModulePath
            ));
            if (normalizedModulePath.startsWith(getUniquePathPrefix())) {
                if (normalizedModulePath.getNameCount() < 3) {
                    logFine(() -> MessageFormat.format(
                        "{0}: normalized absolute module path ''{1}'' is too short",
                        modulePath,
                        normalizedModulePath
                    ));
                    logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                    return null;
                }
                final var relativeRepoPath =  normalizedModulePath.subpath(2, normalizedModulePath.getNameCount());
                logFine(() -> MessageFormat.format("{0}: relative repo path is ''{1}''", modulePath, relativeRepoPath));
                var resolvedRelativeRepoPath = resolveZipEntryPath(relativeRepoPath);
                if (resolvedRelativeRepoPath == null) {
                    logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                    return null;
                }
                return Path.of(
                    getUniquePathPrefix().toString(),
                    resolvedRelativeRepoPath.toString()
                );
            } else {
                logFine(() -> MessageFormat.format(
                    "{0}: can''t resolve absolute module path in the file system based ES module repository with unique prefix ''{1}''",
                    modulePath.toString(),
                    getUniquePathPrefix().toString()
                ));
                return null;
            }
        } else {
            var repoPath = resolveZipEntryPath(modulePath);
            if (repoPath == null) {
                logFine(() -> MessageFormat.format(
                    "{0}: can''t resolve relative module path in the jar file based ES Module repository ''{1}''. "
                    +"The path doesn''t refer to a readable zip entry.",
                    modulePath.toString(),
                    jarFile.getAbsolutePath()
                ));
                logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                return null;
            }
            return Path.of(
                getUniquePathPrefix().toString(),
                repoPath.toString()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(Path absolutePath) throws IOException {
        if (!absolutePath.startsWith(getUniquePathPrefix())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path doesn''t match unique path prefix ''{1}'' of jar file based ES Module repository",
                absolutePath.toString(),
                getUniquePathPrefix().toString()
            ));
        }
        if (absolutePath.getNameCount() < 3) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path is too short.",
                absolutePath.toString(),
                getUniquePathPrefix().toString()
            ));
        }
        final var relativeRepoPath = absolutePath.subpath(2, absolutePath.getNameCount());
        final var zipEntryPath = resolveZipEntryPath(relativeRepoPath);
        if (zipEntryPath == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                absolutePath.toString(),
                getUniquePathPrefix().toString()
            ));
        }
        final var zipEntry = jar.getEntry(zipEntryPath.toString());
        if (zipEntry == null) {
            // shouldn't happen, but just in case
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                absolutePath.toString(),
                getUniquePathPrefix().toString()
            ));
        }
        final var bytes = jar.getInputStream(zipEntry).readAllBytes();
        return new SeekableInMemoryByteChannel(bytes);
    }
}
