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
import java.util.logging.Logger;

public class FileSystemESModuleRepository extends AbstractESModuleRepository {
    static private final Logger logger = Logger.getLogger(FileSystemESModuleRepository.class.getName());

    private final File root;

    /**
     * Creates a new ES module repository for modules stored in a directory in the file system.
     *
     * @param root the root directory of the repository
     * @throws IllegalArgumentException thrown if <code>root</code> isn't a directory or isn't
     *   readable
     * @throws NullPointerException thrown if <code>root</code> is null
     */
    FileSystemESModuleRepository(@NotNull final File root) throws IllegalArgumentException {
        Objects.requireNonNull(root);
        this.root = root.getAbsoluteFile();
        if (!root.isDirectory() || !root.canRead()) {
            throw new IllegalArgumentException(MessageFormat.format(
               "Illegal root directory '{0}'. Directory doesn't exist or isn't readable.",
                root.getAbsolutePath()
            ));
        }
    }

    @Override
    public Path resolveModulePath(@NotNull String modulePath) {
        Objects.requireNonNull(modulePath);
        return this.resolveModulePath(Path.of(modulePath));
    }

    protected Path buildAbsoluteModulePath(@NotNull final Path absoluteRepoPath) {
        Objects.requireNonNull(absoluteRepoPath);
        if (!absoluteRepoPath.startsWith(root.toPath())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Unexpected absolute repository path. Path '{0}' doesn't start with the repository root path '{1}'",
                absoluteRepoPath.toString(),
                root.getAbsolutePath()
            ));
        }
        final var relativePathStart = root.toPath().getNameCount() - 1;
        final var relativePathLength = absoluteRepoPath.getNameCount() - root.toPath().getNameCount();
        final var relativeRepoPath = absoluteRepoPath.subpath(relativePathStart, relativePathLength);
        return Path.of(getUniquePathPrefix().toString(), relativeRepoPath.toString());
    }

    protected boolean isReadableFile(final File file) {
        return file.isFile() && file.exists() && file.canRead();
    }

    static final List<String> SUFFIXES = List.of("", ".mjs", ".js");
    protected @Null Path buildAbsoluteRepositoryPath(@NotNull final Path relativeRepositoryPath) {
        Objects.requireNonNull(relativeRepositoryPath);
        final var file = new File(root, relativeRepositoryPath.toString());
        return SUFFIXES.stream()
            .map(suffix -> file.toPath().resolveSibling(file.getName() + suffix))
            .filter(path -> isReadableFile(path.toFile().getAbsoluteFile()))
            .findFirst()
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveModulePath(@NotNull Path modulePath) {
        Objects.requireNonNull(modulePath);
        if (modulePath.isAbsolute()) {
            modulePath = modulePath.normalize();
            if (modulePath.startsWith(getUniquePathPrefix())) {
                var relativeRepoPath = new File(root, modulePath.subpath(2, modulePath.getNameCount()).toString()).toPath();
                var absoluteRepoPath = buildAbsoluteRepositoryPath(relativeRepoPath);
                if (absoluteRepoPath == null) {
                    return null;
                }
                return buildAbsoluteModulePath(absoluteRepoPath);
            } else {
                logger.fine(MessageFormat.format(
                    "Can't resolve absolute path '{0}' in file system based ES module repository with unique prefix '{1}'",
                    modulePath.toString(),
                    getUniquePathPrefix().toString()
                ));
                return null;
            }
        } else {
            var repoPath = Path.of(root.toPath().toString(), modulePath.toString()).normalize();
            if (!repoPath.startsWith(root.toPath())) {
                // error, navigation outside of repo
                return null;
            }
            var absoluteRepoPath = buildAbsoluteRepositoryPath(repoPath);
            if (absoluteRepoPath == null) {
                return null;
            }
            return buildAbsoluteModulePath(absoluteRepoPath);
        }
    }

    @Override
    public SeekableByteChannel newByteChannel(@NotNull final Path absolutePath) throws IOException {
        return null;
    }
}
