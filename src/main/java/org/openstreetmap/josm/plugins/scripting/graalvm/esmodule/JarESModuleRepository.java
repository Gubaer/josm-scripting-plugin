package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleJarURI;
import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final RelativePath root;

    private static final List<String> SUFFIXES = List.of("", ".mjs", ".js");
    private RelativePath resolveZipEntryPath(@NotNull RelativePath relativeModulePath) {


        final var canonicalizedPath = relativeModulePath.canonicalize();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(MessageFormat.format("canonicalized relative module path is ''{0}''", canonicalizedPath));
        }
        // try to locate a suitable zip entry
        return SUFFIXES.stream().map(suffix -> canonicalizedPath.toString() + suffix)
            .filter(p -> {
                var entry = jar.getEntry(p);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format("Tried relative repo path ''{0}'', found entry ''{1}''", p, entry));
                }
                return entry != null && !entry.isDirectory();
            })
            .findFirst()
            .map(RelativePath::parse)
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getBaseURI() {
        try {
            return ModuleJarURI.buildJarUri(jarFile.getAbsolutePath(), root);
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
        this.root = RelativePath.EMPTY;
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
    public JarESModuleRepository(@NotNull final File jarFile, @NotNull final RelativePath rootEntry) throws IOException {
        Objects.requireNonNull(jarFile);
        Objects.requireNonNull(rootEntry);
        this.jarFile = jarFile;
        this.jar = new JarFile(jarFile);

        this.root = rootEntry.canonicalize();
        if (!this.root.isEmpty()) {
            var entry = this.jar.getEntry(this.root.toString());
            if (entry == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Root entry ''{0}'' not found in jar file ''{1}''",
                    rootEntry,
                    this.jarFile
                ));
            }
            if (!entry.isDirectory()) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Root entry ''{0}'' isn''t a directory entry in the jar file ''{1}''",
                    rootEntry,
                    this.jarFile
                ));
            }
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
            this.root = moduleJarUri.getJarEntryPath();
        } catch(IllegalArgumentException e) {
            throw new IllegalESModuleBaseUri(MessageFormat.format(
                "Illegal base URI for jar file based ES Module repository. uri=''{0}''", uri), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Null RelativePath resolveModulePath(@NotNull RelativePath modulePath) {
        Objects.requireNonNull(modulePath);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(MessageFormat.format("**** Starting to resolve module path ''{0}''", modulePath));
        }
        if (startsWithESModuleRepoPathPrefix(modulePath)) {
            // modulePath is a path starting with 'es-module-repo/<uuid>'. First, make sure that <uuid> is
            // the unique id of this repository
            if (!getUniquePathPrefix().getSegment(1).equals(modulePath.getSegment(1))) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                            "{0}: module path doesn''t refer entry in this repository ''{1}''",
                            modulePath,
                            getUniquePathPrefix()
                    ));
                }
                return RelativePath.EMPTY;
            }
            var canonicalizedModulePath= modulePath.canonicalize();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(MessageFormat.format(
                    "{0}: canonicalized module path is ''{1}''",
                    modulePath,
                    canonicalizedModulePath
                ));
            }
            final var relativeRepoPath =  RelativePath.of(
                canonicalizedModulePath.getSegments().subList(2, canonicalizedModulePath.getLength())
            );

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(MessageFormat.format("{0}: relative repo path is ''{1}''",
                    modulePath,
                    relativeRepoPath));
            }
            var resolvedRelativeRepoPath = resolveZipEntryPath(relativeRepoPath);
            if (resolvedRelativeRepoPath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            if (root.isEmpty()) {
                return relativeRepoPath;
            } else {
                return relativeRepoPath.resolveAgainstDirectoryContext(root).canonicalize();
            }
        } else {
            // modulePath isn't a path starting  with 'es-module-repo/<uuid>'. Resolve it against
            // the root
            var repoPath = modulePath.resolveAgainstDirectoryContext(root).canonicalize();
            repoPath = resolveZipEntryPath(repoPath);
            if (repoPath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                        "{0}: can''t resolve relative module path in the jar file based ES Module repository ''{1}''. "
                                +"The path doesn''t refer to a readable zip entry.",
                        modulePath,
                        jarFile
                    ));
                    logger.fine(MessageFormat.format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            return getUniquePathPrefix().append(repoPath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull SeekableByteChannel newByteChannel(@NotNull RelativePath path) throws IOException {
        if (!startsWithESModuleRepoPathPrefix(path)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path doesn''t match unique path prefix ''{1}'' of jar file based ES Module repository",
                path,
                getUniquePathPrefix()
            ));
        }
        if (path.getLength() <= 2) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Path is too short.",
                path,
                getUniquePathPrefix()
            ));
        }
        final var relativeRepoPath = RelativePath.of(
            path.getSegments().subList(2, path.getLength())
        );
        final var zipEntryPath = resolveZipEntryPath(relativeRepoPath);
        if (zipEntryPath == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                path,
                getUniquePathPrefix()
            ));
        }
        final var zipEntry = jar.getEntry(zipEntryPath.toString());
        if (zipEntry == null) {
            // shouldn't happen, but just in case
            throw new IllegalArgumentException(MessageFormat.format(
                "Can''t resolve path ''{0}''. Didn''t find a zip entry under this path in the repo ''{1}''",
                path,
                getUniquePathPrefix()
            ));
        }
        final var bytes = jar.getInputStream(zipEntry).readAllBytes();
        return new SeekableInMemoryByteChannel(bytes);
    }
}
