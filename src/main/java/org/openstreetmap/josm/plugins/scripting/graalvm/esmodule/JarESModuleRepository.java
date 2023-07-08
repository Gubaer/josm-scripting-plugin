package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleJarURI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * <code>JarESModuleRepository</code> is a repository of ES Modules stored in a jar file.
 */
public class JarESModuleRepository extends AbstractESModuleRepository {
    private static final Logger logger = Logger.getLogger(JarESModuleRepository.class.getName());

    /**
     * The jar file in which ES modules of this repository are stored.
     */
    private final JarFile jar;

    /**
     * Same as {@link #jar}, but as the underlying file object
     */
    private final File jarFile;

    /**
     * The root path of the repository with ES modules <bold>within</bold> the jar file.
     * This path is always <bold>relative</bold>.
     */
    private final Path root;
    final private static Pattern LEADING_SLASHES = Pattern.compile("^[/\\\\]+");
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
        // root
        //TODO(Gubaer): normalize isn't enough here. Need to canonicalize?
        final var path = Path.of(
            root.toString(),
            relativeModulePath.normalize().toString()
        );
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(MessageFormat.format("normalized relative module path is ''{0}''", toUnixNotation(path)));
        }
        // try to locate a suitable zip entry
        return SUFFIXES.stream().map(suffix -> toUnixNotation(path) + suffix)
            .filter(p -> {
                var entry = jar.getEntry(p);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format("Tried relative repo path ''{0}'', found entry ''{1}''", p, entry));
                }
                return entry != null && !entry.isDirectory();
            })
            .findFirst()
            .map(Path::of)
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getBaseURI() {
        try {
            return ModuleJarURI.buildJarUri(jarFile.getAbsolutePath(), root.toString());
        } catch (MalformedURLException | URISyntaxException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
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
     * @param rootEntry the path to the root entry for the repository. Must be a relative path, i.e. <code>foo/bar</code>,
     *                  not <code>/foo/bar</code>. Always use <code>/</code> as delimiter, both on windows and linux
     *                  platform.
     * @throws IOException if <code>jarFile</code> doesn't exist or isn't a jar file
     * @throws IOException if <code>jarFile</code> or <code>rootEntry</code> is null
     * @throws IllegalArgumentException if there isn't an entry with name <code>rootEntry</code> in the <code>jarFile</code>
     */
    public JarESModuleRepository(@NotNull final File jarFile, @NotNull final String rootEntry) throws IOException {
        Objects.requireNonNull(jarFile);
        Objects.requireNonNull(rootEntry);
        this.jarFile = jarFile;
        this.jar = new JarFile(jarFile);
        var relativeRootEntry = removeLeadingSlashes(rootEntry);
        if (!relativeRootEntry.equals(rootEntry)) {
            logger.warning(MessageFormat.format(
                    "removed leading slashes from root entry ''{0}''. "
                    + "Root entry now is ''{1}''",
                    rootEntry, relativeRootEntry
            ));
        }
        //TODO(Gubaer): normalize not good enough? Need to canonicalize?
        this.root = Path.of(relativeRootEntry).normalize();
        var entry = this.jar.getEntry(toUnixNotation(this.root));
        if (entry == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Root entry ''{0}'' not found in jar file ''{1}''",
                rootEntry,
                this.jarFile
            ));
        }
    }

    /**
     * Creates a repository of ES Modules stored in a jar file.
     *
     * @param uri a jar-URI
     * @throws IOException thrown if the jar-file doesn't exist or isn't readable
     * @throws IOException thrown if the jar-URI includes a root entry which doesn't exist,
     *    isn't directory entry, or isn't readable
     * @throws IllegalESModuleBaseUri thrown if <code>uri</code> isn't a valid jar-URI
     * @throws NullPointerException thrown if <code>uri</code> is null
     */
    public JarESModuleRepository(@NotNull final URI uri) throws IOException, IllegalESModuleBaseUri {
        Objects.requireNonNull(uri);
        try {
            final var moduleJarUri = new ModuleJarURI(uri);
            if (!moduleJarUri.refersToJarFile()) {
                throw new IllegalESModuleBaseUri(MessageFormat.format(
                    "Jar file doesn''t exist or isn''t readable. uri=''{0}''", uri));
            }
            if (!moduleJarUri.getJarEntryName().isEmpty() && !moduleJarUri.refersToDirectoryJarEntry()) {
                throw new IllegalESModuleBaseUri(MessageFormat.format(
                    "Root entry doesn''t exist or isn''t a directory entry. uri=''{0}''", uri));
            }
            this.jarFile = moduleJarUri.getJarFile();
            this.jar = new JarFile(jarFile);
            this.root = Path.of(moduleJarUri.getJarEntryName());
        } catch(IllegalArgumentException e) {
            throw new IllegalESModuleBaseUri(MessageFormat.format(
                "Illegal base URI for jar-file based ES Module repository. uri=''{0}''", uri), e);
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
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(MessageFormat.format("**** Starting to resolve module path ''{0}''", toUnixNotation(modulePath)));
        }
        if (startsWithESModuleRepoPathPrefix(modulePath)) {
            var normalizedModulePath= modulePath.normalize();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(MessageFormat.format(
                    "{0}: normalized module path is ''{1}''",
                    toUnixNotation(modulePath),
                    toUnixNotation(normalizedModulePath)
                ));
            }
            if (normalizedModulePath.startsWith(getUniquePathPrefix())) {
                if (normalizedModulePath.getNameCount() <= 2) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(MessageFormat.format(
                            "{0}: normalized absolute module path ''{1}'' is too short",
                            toUnixNotation(modulePath),
                            toUnixNotation(normalizedModulePath)
                        ));
                        logger.fine(MessageFormat.format("{0}: resolution FAILED", toUnixNotation(modulePath)));
                    }
                    return null;
                }
                final var relativeRepoPath =  normalizedModulePath.subpath(2, normalizedModulePath.getNameCount());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format("{0}: relative repo path is ''{1}''",
                        toUnixNotation(modulePath),
                        toUnixNotation(relativeRepoPath)));
                }
                var resolvedRelativeRepoPath = resolveZipEntryPath(relativeRepoPath);
                if (resolvedRelativeRepoPath == null) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(MessageFormat.format("{0}: resolution FAILED", toUnixNotation(modulePath)));
                    }
                    return null;
                }
                if (root.toString().isEmpty()) {
                    return Path.of(getUniquePathPrefix().toString(), resolvedRelativeRepoPath.toString());
                } else {
                    return Path.of(
                        getUniquePathPrefix().toString(),
                        resolvedRelativeRepoPath.subpath(
                            root.getNameCount(),
                            resolvedRelativeRepoPath.getNameCount()).toString()
                    );
                }
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                        "{0}: can''t resolve absolute module path in the file system based ES module repository with unique prefix ''{1}''",
                        toUnixNotation(modulePath),
                        toUnixNotation(getUniquePathPrefix())
                    ));
                }
                return null;
            }
        } else {
            var repoPath = resolveZipEntryPath(modulePath);
            if (repoPath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                        "{0}: can''t resolve relative module path in the jar file based ES Module repository ''{1}''. "
                                +"The path doesn''t refer to a readable zip entry.",
                        toUnixNotation(modulePath),
                        jarFile
                    ));
                    logger.fine(MessageFormat.format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            return Path.of(
                getUniquePathPrefix().toString(),
                modulePath.normalize().toString()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull SeekableByteChannel newByteChannel(@NotNull Path absolutePath) throws IOException {
        if (!startsWithESModuleRepoPathPrefix(absolutePath)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path doesn''t match unique path prefix ''{1}'' of jar file based ES Module repository",
                toUnixNotation(absolutePath),
                toUnixNotation(getUniquePathPrefix())
            ));
        }
        if (absolutePath.getNameCount() <= 2) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path is too short.",
                toUnixNotation(absolutePath),
                toUnixNotation(getUniquePathPrefix())
            ));
        }
        final var relativeRepoPath = absolutePath.subpath(2, absolutePath.getNameCount());
        final var zipEntryPath = resolveZipEntryPath(relativeRepoPath);
        if (zipEntryPath == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                toUnixNotation(absolutePath),
                toUnixNotation(getUniquePathPrefix())
            ));
        }
        final var zipEntry = jar.getEntry(toUnixNotation(zipEntryPath));
        if (zipEntry == null) {
            // shouldn't happen, but just in case
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                toUnixNotation(absolutePath),
                toUnixNotation(getUniquePathPrefix())
            ));
        }
        final var bytes = jar.getInputStream(zipEntry).readAllBytes();
        return new SeekableInMemoryByteChannel(bytes);
    }
}
