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
        if (!relativeModulePath.isAbsolute()) {
            relativeModulePath = Path.of(removeLeadingSlashes(relativeModulePath.toString()));
        }
        final var path = relativeModulePath.normalize();
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
            var repoPath = resolveZipEntryPath(modulePath.normalize());
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

    @Override
    public SeekableByteChannel newByteChannel(Path absolutePath) throws IOException {
        return null;
    }
}
