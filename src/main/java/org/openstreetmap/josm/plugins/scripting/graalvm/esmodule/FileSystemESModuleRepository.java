package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * A repository for ES Modules stored in the local file system.
 * <p>
 * The repository represents a subtree in the local file system, below its {@link #getRoot() root directory}.
 * <p>
 * Its main purpose is to <strong>resolve</strong> a {@link org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID module id}.
 * Successful resolution means that the repository can map a module ID to a source file somewhere in the file system
 * below the repository root.
 * <p>
 * Terminology:
 * <ul>
 *     <li>module ID path - the module ID represented as {@link RelativePath}</li>
 *     <li>full module repo path - a full module repo path always starts with two segments 'es-module-repo/uuid'.
 *     It uniquely identifies the ES module repository and the path to the file within the repository. It is
 *     represented as {@link RelativePath}</li>
 *     <li>relative module repo path - the relative path to the source file within a repository. It is represented as {@link RelativePath}</li>
 *     <li>module file path - the absolute path to a source file in the file system. It is represented as {@link Path}.</li>
 * </ul>
 *
 */
@SuppressWarnings("unused")
public class FileSystemESModuleRepository extends AbstractESModuleRepository {
    static private final Logger logger = Logger.getLogger(FileSystemESModuleRepository.class.getName());
    private final File root;

    /**
     * Creates a new ES module repository for modules stored in a directory in the file system.
     *
     * @param root the root directory of the repository
     * @throws IllegalArgumentException if <code>root</code> isn't a directory or isn't
     *   readable
     * @throws IllegalArgumentException if <code>root</code> isn't absolute file
     * @throws NullPointerException  if <code>root</code> is null
     */
    public FileSystemESModuleRepository(@NotNull final File root) throws IllegalArgumentException {
        Objects.requireNonNull(root);
        this.root = root;
        if (!root.isAbsolute()) {
            throw new IllegalArgumentException(format(
                "Illegal root directory ''{0}''. The root directory must be an absolute file.", root
            ));
        }
        if (!this.root.isDirectory() || !this.root.canRead()) {
            throw new IllegalArgumentException(format(
               "Illegal root directory ''{0}''. Directory doesn''t exist or isn''t readable.", root.getAbsolutePath()
            ));
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public URI getBaseURI() {
        return root.toURI();
    }

    /**
     * Replies the root directory of this repository.
     *
     * @return the root directory
     */
    public @NotNull File getRoot() {
        return root;
    }

    /**
     * Converts a module file path in the underlying file system to a full module rep
     * path with the unique prefix for this repository (see {@link #getUniquePathPrefix()}) (for
     * terminology see the class comment above).
     * <p>
     * <strong>Example:</strong> Convert the Windows path <code>c:\path\to\root\sub\module-a.mjs</code> to the full
     * module path <code>es-module-repo/8122b2b4-776f-4812-82d7-3b961e531793/sub/module-a.mjs</code>
     *
     * @param moduleFilePath the module file path in the underlying file system
     * @return the full module path
     * @throws IllegalArgumentException thrown if <code>moduleFilePath</code> isn't a valid absolute path
     *   into this repository
     * @throws NullPointerException thrown if <code>moduleFilePath</code> is null
     */
    protected RelativePath convertModuleFilePathToFullModuleRepoPath(@NotNull final Path moduleFilePath) throws IllegalArgumentException {
        Objects.requireNonNull(moduleFilePath);
        if (!moduleFilePath.startsWith(root.toPath())) {
            throw new IllegalArgumentException(format(
                "Illegal absolute repository path. Path ''{0}'' doesn''t start with the repository root path ''{1}''",
                moduleFilePath.toString(),
                root.getAbsolutePath()
            ));
        }
        final var relativePathStart = root.toPath().getNameCount();
        final var relativePathLength = moduleFilePath.getNameCount() - root.toPath().getNameCount();
        if (relativePathLength < 1) {
            throw new IllegalArgumentException(format(
                "Illegal module file path. Path ''{0}'' must include at least 1 segment after the repository root ''{1}'', "
                + "but only has {2}.",
                moduleFilePath, root, relativePathLength
            ));
        }
        final var relativeModuleRepoPath = moduleFilePath.subpath(relativePathStart, moduleFilePath.getNameCount());
        return getUniquePathPrefix()
            .append(RelativePath.of(relativeModuleRepoPath));
    }

    protected Path convertFullModuleRepoPathToModuleFilePath(@NotNull final RelativePath fullModuleRepoPath)
        throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(fullModuleRepoPath);
        if (! fullModuleRepoPath.startsWith(getUniquePathPrefix())) {
            throw new IllegalArgumentException(format(
                "Illegal full module repo path. Path ''{0}'' doesn''t start with the unique path prefix ''{1}''.",
                fullModuleRepoPath, getUniquePathPrefix()
            ));
        }
        if (fullModuleRepoPath.getLength() < 3) {
            throw new IllegalArgumentException(format(
                "Illegal full module repo path. Path ''{0}'' should have at least 3 segments, but only has {1}.",
                fullModuleRepoPath, fullModuleRepoPath.getLength()
            ));
        }
        final var relativeModuleRepoPath = RelativePath.of(
            fullModuleRepoPath.getSegments().subList(2, fullModuleRepoPath.getLength()));
        return new File(root, relativeModuleRepoPath.toString()).toPath();
    }

    private static final List<String> SUFFIXES = List.of("", ".mjs", ".js");

    protected @Null Path resolveModuleRepoPathToModuleFilePath(@NotNull final RelativePath moduleRepoPath) {
        Objects.requireNonNull(moduleRepoPath);
        File absoluteModuleFile = new File(root, moduleRepoPath.toString());
        try {
            absoluteModuleFile = absoluteModuleFile.getCanonicalFile();
        } catch(IOException e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format("failed to build canonical file for file ''{0}''", absoluteModuleFile), e);
            }
            return null;
        }
        if (!absoluteModuleFile.toPath().startsWith(root.toPath())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format(
                    "repo path ''{0}'' resolves to absolute repo path ''{1}'' which is outside of the repo with root ''{2}''",
                    moduleRepoPath, absoluteModuleFile.getAbsolutePath(), root.getAbsolutePath()
                ));
            }
            return null;
        }
        final File repoFile = absoluteModuleFile;
        return SUFFIXES.stream()
            // build a candidate for the path with one of the candidate suffixes
            .map(suffix -> repoFile.toPath().resolveSibling(repoFile.getName() + suffix))
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
    public RelativePath resolveModulePath(@NotNull final RelativePath modulePath) {
        Objects.requireNonNull(modulePath);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("{0}: start resolving", modulePath));
        }
        if (modulePath.startsWith(getUniquePathPrefix())) {
            var relativeModuleRepoPath = RelativePath.of(
                modulePath.getSegments().subList(2, modulePath.getLength())
            );
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("{0}: relative module repo path is ''{1}''", modulePath, relativeModuleRepoPath));
            }
            var moduleFilePath = resolveModuleRepoPathToModuleFilePath(relativeModuleRepoPath);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("{0}: full module file path is ''{1}''", modulePath, moduleFilePath));
            }
            if (moduleFilePath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            return convertModuleFilePathToFullModuleRepoPath(moduleFilePath);
        } else {
            var moduleFilePath = Path.of(root.toPath().toString(), modulePath.toString()).normalize();
            try {
                moduleFilePath = moduleFilePath.toFile().getCanonicalFile().toPath();
            } catch(IOException e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, format("failed to canonicalize moduleFilePath ''{0}''", moduleFilePath), e);
                }
                return null;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("{0}: module file path is ''{1}''", modulePath, moduleFilePath));
            }
            if (!moduleFilePath.startsWith(root.toPath())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format(
                        "{0}: can''t resolve relative module path in the file system based ES module. " +
                        "The module file path ''{1}'' refers to a file outside of the repo.",
                        modulePath.toString(), moduleFilePath
                    ));
                    logger.fine(format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            moduleFilePath = resolveModuleRepoPathToModuleFilePath(modulePath);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(format("{0}: resolved module file path is ''{1}''", modulePath, moduleFilePath));
            }
            if (moduleFilePath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format(
                        "{0}: can''t resolve relative module path in the file system based ES module with root ''{1}''. " +
                        "The path doesn''t refer to a readable file.",
                        modulePath, root
                    ));
                    logger.fine(format("{0}: resolution FAILED", modulePath));
                }
                return null;
            }
            var fullModuleRepoPath = convertModuleFilePathToFullModuleRepoPath(moduleFilePath);
            if (fullModuleRepoPath == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format(
                        "{0}: failed to convert module file path ''{1}'' into a full module repo path",
                        modulePath, moduleFilePath
                    ));
                    logger.fine(format("{0}: resolution FAILED", modulePath));
                }
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(format("{0}: SUCCESS. Resolved path is ''{1}''", modulePath, fullModuleRepoPath));
                }
            }
            return fullModuleRepoPath;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull SeekableByteChannel newByteChannel(@NotNull final RelativePath path) throws IOException {
        Objects.requireNonNull(path);
        final var absoluteRepoPath = convertFullModuleRepoPathToModuleFilePath(path);
        return Files.newByteChannel(absoluteRepoPath, StandardOpenOption.READ);
    }
}
