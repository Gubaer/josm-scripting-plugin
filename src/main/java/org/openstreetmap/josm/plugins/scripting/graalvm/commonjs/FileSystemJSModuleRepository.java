package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

/**
 * A <strong>FileSystemJSModuleRepository</strong> resolves CommonJS
 * module stored in a directory in the file system.
 */
public class FileSystemJSModuleRepository extends BaseJSModuleRepository {

    private final File baseDir;

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRepoFile(final String repoPath) {
        try {
            // make sure that all '.' and '..' segments in the repo path are resolved
            final Path moduleFilePath = Path.of(baseDir.toPath().resolve(repoPath)
                .toFile().getCanonicalPath());

            if (!moduleFilePath.startsWith(baseDir.toPath())) {
                if (logger.isLoggable(Level.FINE)) {
                    final String message = MessageFormat.format(
                    "canonical path ''{0}'' for a module isn''t below the repository base dir ''{1}''",
                    moduleFilePath, baseDir.toPath()
                    );
                    logger.log(Level.FINE, message);
                }
                return false;
            }

            if (logger.isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
                        "checking path ''{0}''", moduleFilePath
                );
                logger.log(Level.FINE, message);
            }
            return isModuleReadable(moduleFilePath);
        } catch(IOException e) {
            final String message = MessageFormat.format(
        "failed to canonicalize path ''{0}''",
                baseDir.toPath().resolve(repoPath)
            );
            logger.log(Level.SEVERE, message);
            return false;
        }
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null. Must not
     *                be empty.
     * @throws IllegalArgumentException thrown if <code>baseDir</code> isn't an
     *  an absolute directory
     *
     */
    public FileSystemJSModuleRepository(@NotNull String baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = new File(baseDir);
        if (!this.baseDir.toPath().isAbsolute()) {
            throw new IllegalArgumentException(
                MessageFormat.format(
                "baseDir must be an absolute path. Got ''{0}''",
                    this.baseDir
                )
            );
        }
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null.
     * @throws IllegalArgumentException thrown if <code>baseDir</code> isn't an
     *   an absolute directory
     */
    public FileSystemJSModuleRepository(@NotNull File baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = baseDir;
        if (!this.baseDir.toPath().isAbsolute()) {
            throw new IllegalArgumentException(
                MessageFormat.format(
                    "baseDir must be an absolute path. Got ''{0}''",
                    this.baseDir
                )
            );
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
        ModuleID.ensureValid(id);
        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "**** Starting to resolve module with id ''{0}'' ****",
                id
            );
            logger.log(Level.FINE, message);
        }
        return internalResolve(new ModuleID(id), baseDir.toURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String moduleId, @NotNull URI contextUri) {
        ModuleID.ensureValid(moduleId);
        Objects.requireNonNull(contextUri);
        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "*********** Starting to resolve module with id ''{0}''",
                moduleId
            );
            logger.log(Level.FINE, message);
        }

        if (! isBaseOf(contextUri)) {
            if (logger.isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
            "failed to resolve module ''{0}''. " +
                    "Context URI ''{1}'' isn''t a child of the base URI ''{2}''",
                    moduleId, contextUri, baseDir.toURI().toString()
                );
                logger.log(Level.FINE, message);
            }
            return Optional.empty();
        }
        return internalResolve(new ModuleID(moduleId), contextUri);
    }

    private boolean isModuleReadable(Path moduleFilePath) {
        final File moduleFile = moduleFilePath.toFile();
        return moduleFile.exists()
            && moduleFile.isFile()
            && moduleFile.canRead();
    }

    private boolean checkBaseDirExistAndReadable(String moduleId) {
        if (! (baseDir.exists() && baseDir.canRead() && baseDir.isDirectory())) {
            if (logger.isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
            "failed to resolve module moduleId ''{0}''. base dir ''{1}'' "
                    + "doesn''t exist, isn''t readable, or isn''t a directory",
                    moduleId, baseDir.getAbsolutePath()
                );
                logger.log(Level.FINE, message);
            }
            return false;
        }
        return true;
    }

    private Optional<URI> internalResolve(ModuleID moduleId, URI contextURI) {
        if (getLogger().isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "moduleId=''{0}'', contextUri=''{1}''",
                moduleId,
                contextURI
            );
            getLogger().log(Level.FINE, message);
        }
        // make sure baseDir is an existing and readable directory
        if (!checkBaseDirExistAndReadable(moduleId.toString())) {
            return Optional.empty();
        }
        moduleId = moduleId.normalized();

        // we already know, that contextURI is a non-null file:// URI
        // below the baseUri of this repo. Normalize it.
        Path contextFilePath = Paths.get(contextURI).normalize();
        if (contextFilePath.toFile().isFile()) {
            contextFilePath = contextFilePath.getParent().normalize();
        }
        final Path contextRepoPath =  baseDir.toPath().relativize(contextFilePath);

        final Optional<Path> moduleRepoPath = resolve(moduleId, contextRepoPath);
        if (moduleRepoPath.isEmpty()) {
            final Object[] params = {
                moduleId.toString()
            };
            if (getLogger().isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
            "resolve: failed to resolve module with id ''{0}''. " +
                    "no matching module repo path found",
                    params
                );
                getLogger().log(Level.FINE, message);
            }
            return Optional.empty();
        }
        final Path moduleFilePath = baseDir.toPath().resolve(moduleRepoPath.get());

        final Object[] params = {
            moduleId.toString(),
            moduleFilePath.toString()
        };
        if (! isModuleReadable(moduleFilePath)) {
            if (getLogger().isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
             "failed to resolve module with id ''{0}''. " +
                    "resolved file path ''{1}'' doesn''t refer to a readable file",
                    params
                );
                getLogger().log(Level.FINE, message);
            }
            return Optional.empty();
        }
        if (getLogger().isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
         "succeeded to resolve module with id ''{0}''. " +
                "resolved file path ''{1}'' refers to a readable file",
                params
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
