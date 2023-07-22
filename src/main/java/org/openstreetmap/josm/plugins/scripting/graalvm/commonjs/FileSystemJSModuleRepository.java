package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;
import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

/**
 * A <strong>FileSystemJSModuleRepository</strong> resolves CommonJS
 * module stored in a directory in the file system.
 */
public class FileSystemJSModuleRepository extends BaseJSModuleRepository {

    private final File baseDir;

    @Override
    protected ContextType getContextType(RelativePath context) throws IOException {
        final var file = new File(baseDir, context.toString());
        if (file.isDirectory()) {
            return ContextType.DIRECTORY_CONTEXT;
        } else if (file.isFile()) {
            return ContextType.FILE_CONTEXT;
        } else {
            throw new IOException(format("file ''{0}}'' is neither a file nor a directory", file));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRepoFile(final RelativePath repoPath) {
        try {
            // make sure that all '.' and '..' segments in the repo path are resolved
            final Path moduleFilePath = Path.of(baseDir.toPath().resolve(repoPath.toPath())
                .toFile().getCanonicalPath());

            if (!moduleFilePath.startsWith(baseDir.toPath())) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, format(
                        "canonical path ''{0}'' for a module isn''t below the repository base dir ''{1}''",
                        moduleFilePath, baseDir.toPath()
                    ));
                }
                return false;
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format("checking path ''{0}''", moduleFilePath));
            }
            return isModuleReadable(moduleFilePath);
        } catch(IOException e) {
            logger.log(Level.SEVERE, format("failed to canonicalize path ''{0}''",
                baseDir.toPath().resolve(repoPath.toPath())
            ));
            return false;
        }
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null. Must not
     *                be empty.
     * @throws IllegalArgumentException thrown if <code>baseDir</code> isn't
     *  an absolute directory
     *
     */
    public FileSystemJSModuleRepository(@NotNull String baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = new File(baseDir);
        if (!this.baseDir.toPath().isAbsolute()) {
            throw new IllegalArgumentException(format(
                "baseDir must be an absolute path. Got ''{0}''", this.baseDir
            ));
        }
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null.
     * @throws IllegalArgumentException thrown if <code>baseDir</code> isn't
     *   an absolute directory
     */
    public FileSystemJSModuleRepository(@NotNull File baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = baseDir;
        if (!this.baseDir.toPath().isAbsolute()) {
            throw new IllegalArgumentException(format(
                "baseDir must be an absolute path. Got ''{0}''", this.baseDir
            ));
        }
    }

    /**
     * Replies the base URI
     *
     * @return the base URI
     */
    @Override
    public @NotNull URI getBaseURI() {
        return baseDir.toURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String id) {
        final var moduleId  = new ModuleID(RelativePath.parse(id));
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format("**** Starting to resolve module with id ''{0}'' ****", id));
        }
        return internalResolve(moduleId, baseDir.toURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String moduleId, @NotNull URI contextUri) {
        Objects.requireNonNull(contextUri);
        Objects.requireNonNull(moduleId);
        final var modulePath = RelativePath.parse(moduleId);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format("*********** Starting to resolve module with id ''{0}''", moduleId));
        }

        if (!isBaseOf(contextUri)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format(
                    "failed to resolve module ''{0}''. Context URI ''{1}'' isn''t a child of the base URI ''{2}''",
                    moduleId, contextUri, baseDir.toURI()
                ));
            }
            return Optional.empty();
        }
        return internalResolve(new ModuleID(modulePath), contextUri);
    }

    private boolean isModuleReadable(Path moduleFilePath) {
        final File moduleFile = moduleFilePath.toFile();
        return moduleFile.exists()
            && moduleFile.isFile()
            && moduleFile.canRead();
    }

    private boolean checkBaseDirExistAndReadable(ModuleID moduleId) {
        if (! (baseDir.exists() && baseDir.canRead() && baseDir.isDirectory())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format(
                    "failed to resolve module moduleId ''{0}''. base dir ''{1}'' " +
                    "doesn''t exist, isn''t readable, or isn''t a directory",
                    moduleId, baseDir
                ));
            }
            return false;
        }
        return true;
    }

    static private Optional<Path> removePrefix(final Path path, final Path prefix) {
        if(!path.startsWith(prefix)) {
            return Optional.empty();
        }
        if(path.equals(prefix)) {
            return Optional.of(Path.of(""));
        }
        return Optional.of(path.subpath(prefix.getNameCount(), path.getNameCount()));
    }

    private Optional<URI> internalResolve(ModuleID moduleId, URI contextURI) {
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().log(Level.FINE, format(
                "moduleId=''{0}'', contextUri=''{1}'', baseDir=''{2}''", moduleId, contextURI, baseDir
            ));
        }
        // make sure baseDir is an existing and readable directory
        if (!checkBaseDirExistAndReadable(moduleId)) {
            return Optional.empty();
        }
        moduleId = moduleId.normalized();

        // we already know, that contextURI is a non-null file:// URI
        // below the baseUri of this repo. Normalize it.
        Path contextFilePath = Paths.get(contextURI).normalize();
        final Optional<Path> contextRepoPath = removePrefix(contextFilePath, baseDir.toPath());
        if (contextRepoPath.isEmpty()) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "failed to resolve module with id ''{0}''. baseDir ''{1}'' isn't a prefix of context file path ''{2}''",
                    moduleId, baseDir.toPath(), contextFilePath
                ));
            }
            return Optional.empty();
        }

        final Optional<RelativePath> moduleRepoPath = resolve(
            moduleId, RelativePath.of(contextRepoPath.get()));
        if (moduleRepoPath.isEmpty()) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "failed to resolve module with id ''{0}''. No matching module repo path found",
                    moduleId
                ));
            }
            return Optional.empty();
        }
        final Path moduleFilePath = baseDir.toPath().resolve(moduleRepoPath.get().toPath());

        if (! isModuleReadable(moduleFilePath)) {
            if (getLogger().isLoggable(Level.FINE)) {
                final String message = format(
                    "failed to resolve module with id ''{0}''. " +
                    "resolved file path ''{1}'' doesn''t refer to a readable file",
                    moduleId, moduleFilePath
                );
                getLogger().log(Level.FINE, message);
            }
            return Optional.empty();
        }
        if (getLogger().isLoggable(Level.FINE)) {
            final String message = format(
                "succeeded to resolve module with id ''{0}''. " +
                "resolved file path ''{1}'' refers to a readable file",
                moduleId, moduleFilePath
            );
            getLogger().log(Level.FINE, message);
        }
        return Optional.of(moduleFilePath.toUri());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBaseOf(@NotNull final URI moduleURI) {
        Objects.requireNonNull(moduleURI);
        if (!moduleURI.getScheme().equalsIgnoreCase("file")) {
            return false;
        }
        final Path moduleFilePath = new File(moduleURI).toPath().normalize();
        return moduleFilePath.startsWith(baseDir.toPath());
    }
}
