package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A repository for ES Modules stored in the local file system.
 */
public class FileSystemESModuleRepository extends AbstractESModuleRepository {
    static private final Logger logger = Logger.getLogger(FileSystemESModuleRepository.class.getName());
    private final File root;

    static private void logFine(Supplier<String> supplier) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(supplier.get());
        }
    }

    /**
     * Creates a new ES module repository for modules stored in a directory in the file system.
     *
     * @param root the root directory of the repository
     * @throws IllegalArgumentException thrown if <code>root</code> isn't a directory or isn't
     *   readable
     * @throws NullPointerException thrown if <code>root</code> is null
     */
    public FileSystemESModuleRepository(@NotNull final File root) throws IllegalArgumentException {
        Objects.requireNonNull(root);
        this.root = root.getAbsoluteFile();
        if (!this.root.isDirectory() || !this.root.canRead()) {
            throw new IllegalArgumentException(MessageFormat.format(
               "Illegal root directory ''{0}''. Directory doesn''t exist or isn''t readable.",
                root.getAbsolutePath()
            ));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveModulePath(@NotNull String modulePath) {
        Objects.requireNonNull(modulePath);
        return this.resolveModulePath(Path.of(modulePath));
    }

    /**
     * Converts an absolute path in the underlying file system to an absolute module
     * path with the unique prefix for this repository (see {@link #getUniquePathPrefix()}).
     *
     * @param absoluteRepoPath the absolute path in the underlying file system
     * @return the absolute module path
     * @throws IllegalArgumentException thrown if <code>absoluteRepoPath</code> isn't a valid absolute path
     *   into this repository
     * @throws NullPointerException thrown if <code>absoluteRepoPath</code> is null
     */
    protected Path convertAbsoluteRepoPathToAbsoluteModulePath(@NotNull final Path absoluteRepoPath) throws IllegalArgumentException {
        Objects.requireNonNull(absoluteRepoPath);
        if (!absoluteRepoPath.startsWith(root.toPath())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal absolute repository path. Path ''{0}'' doesn't start with the repository root path ''{1}''",
                absoluteRepoPath.toString(),
                root.getAbsolutePath()
            ));
        }
        final var relativePathStart = root.toPath().getNameCount();
        final var relativePathLength = absoluteRepoPath.getNameCount() - root.toPath().getNameCount();
        if (relativePathLength < 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal absolute repository path. Path ''{0}'' must include at least 1 segment after the repository root ''{1}'', "
                + "but only has {2}.",
                absoluteRepoPath,
                root.getAbsoluteFile().toString(),
                relativePathLength
        ));
        }
        final var relativeRepoPath = absoluteRepoPath.subpath(relativePathStart, absoluteRepoPath.getNameCount());
        return Path.of(getUniquePathPrefix().toString(), relativeRepoPath.toString());
    }

    protected Path convertAbsoluteModulePathToAbsoluteRepoPath(@NotNull final Path absoluteModulePath)
        throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(absoluteModulePath);
        if (! absoluteModulePath.startsWith(getUniquePathPrefix())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal absolute module path. Path ''{0}'' doesn't start with the unique path prefix ''{1}''.",
                absoluteModulePath,
                getUniquePathPrefix()
            ));
        }
        if (absoluteModulePath.getNameCount() < 3) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal absolute module path. Path ''{0}'' should have at least 3 segments, but only has {1}.",
                absoluteModulePath,
                absoluteModulePath.getNameCount()
            ));
        }
        final var relativePathStart = 2;
        final var relativeModulePath = absoluteModulePath.subpath(relativePathStart, absoluteModulePath.getNameCount());
        return new File(root, relativeModulePath.toString()).toPath();
    }

    private static final List<String> SUFFIXES = List.of("", ".mjs", ".js");
    protected @Null Path resolveRepoPath(@NotNull final Path repoPath) {
        Objects.requireNonNull(repoPath);
        File absoluteRepoFile;
        if (repoPath.isAbsolute()) {
            absoluteRepoFile = repoPath.normalize().toFile();
        } else {
            absoluteRepoFile = new File(root, repoPath.toString()).toPath().normalize().toAbsolutePath().toFile();
        }
        if (!absoluteRepoFile.toPath().startsWith(root.toPath())) {
            logFine(() -> MessageFormat.format(
                "repo path ''{0}'' resolves to absolute repo path ''{1}'' which is outside of the repo with root ''{2}''",
                repoPath,
                absoluteRepoFile.getAbsolutePath(),
                root.getAbsolutePath()
            ));
            return null;
        }
        return SUFFIXES.stream()
            // build a candidate for the path with one of the candidate suffixes
            .map(suffix -> absoluteRepoFile.getAbsoluteFile().toPath().resolveSibling(absoluteRepoFile.getName() + suffix))
            // reject the path if it doesn't point to a file in the repo
            .filter(path -> path.normalize().startsWith(root.toPath()))
            // reject the path if it doesn't refer to a readable file
            .filter(path -> {
                var f = path.toFile();
                return f.isFile() && f.exists() && f.canRead();
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveModulePath(@NotNull final Path modulePath) {
        Objects.requireNonNull(modulePath);
        logFine(() -> MessageFormat.format("{0}: start resolving", modulePath));
        if (modulePath.isAbsolute()) {
            var normalizedModulePath= modulePath.normalize();
            logFine(() -> MessageFormat.format("{0}: normalized module path is ''{1}''", modulePath, normalizedModulePath));
            if (normalizedModulePath.startsWith(getUniquePathPrefix())) {
                var relativeRepoPath =  normalizedModulePath.subpath(2, normalizedModulePath.getNameCount());
                logFine(() -> MessageFormat.format("{0}: relative repo path is ''{1}''", modulePath, relativeRepoPath));
                var absoluteRepoPath = resolveRepoPath(relativeRepoPath);
                logFine(() -> MessageFormat.format("{0}: absolute repo path is ''{1}''", modulePath, absoluteRepoPath));
                if (absoluteRepoPath == null) {
                    logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                    return null;
                }
                return convertAbsoluteRepoPathToAbsoluteModulePath(absoluteRepoPath);
            } else {
                logFine(() -> MessageFormat.format(
                    "{0}: can''t resolve absolute module path in the file system based ES module repository with unique prefix ''{1}''",
                    modulePath.toString(),
                    getUniquePathPrefix().toString()
                ));
                return null;
            }
        } else {
            var repoPath = Path.of(root.toPath().toString(), modulePath.toString()).normalize();
            logFine(() -> MessageFormat.format("{0}: absolute repo path is ''{1}''", modulePath, repoPath));
            if (!repoPath.startsWith(root.toPath())) {
                logFine(() -> MessageFormat.format(
                    "{0}: can''t resolve relative module path  in the file system based ES module. "
                    + "The module path refers to a file outside of the repo.",
                    modulePath.toString()
                ));
                logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                return null;
            }
            var absoluteRepoPath = resolveRepoPath(repoPath);
            logFine(() -> MessageFormat.format("{0}: resolved absolute repo path is ''{1}''", modulePath, absoluteRepoPath));
            if (absoluteRepoPath == null) {
                logFine(() -> MessageFormat.format(
                    "'{0}: can't resolve relative module path in the file system based ES module with root ''{1}''. "
                    +"The path doesn't refer to a readable file.",
                    modulePath.toString(),
                    root.getAbsolutePath()
                ));
                logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
                return null;
            }
            var resolvedPath = convertAbsoluteRepoPathToAbsoluteModulePath(absoluteRepoPath);
            if (resolvedPath == null) {
                logFine(() -> MessageFormat.format("{0}: failed to lookup a matching file for absolute repo path ''{1}''", modulePath, absoluteRepoPath));
                logFine(() -> MessageFormat.format("{0}: resolution FAILED", modulePath));
            } else {
                logFine(() -> MessageFormat.format("{0}: SUCCESS. Resolved path is ''{1}''", modulePath, resolvedPath));
            }
            return resolvedPath;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(@NotNull final Path absoluteModulePath) throws IOException {
        Objects.requireNonNull(absoluteModulePath);
        final var absoluteRepoPath = convertAbsoluteModulePathToAbsoluteRepoPath(absoluteModulePath);
        return Files.newByteChannel(absoluteRepoPath, StandardOpenOption.READ);
    }
}
