package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

/**
 * A <strong>FileSystemJSModuleRepository</strong> resolves CommonJS
 * module stored in a directory in the file system.
 */
public class FileSystemJSModuleRepository extends BaseJSModuleRepository {

    private File baseDir;

    @Override
    protected boolean isRepoFile(String repoPath) {
        final Path moduleFilePath = Paths.get(
                baseDir.toString(),
                Paths.get("/", repoPath).toString());
        logFine(() -> MessageFormat.format(
            "isRepoFile: checking path ''{0}''", moduleFilePath
        ));
        return isModuleReadable(moduleFilePath);
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null. Must not
     *                be empty.
     */
    FileSystemJSModuleRepository(@NotNull String baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = new File(baseDir).getAbsoluteFile();
    }

    /**
     * Creates a new repository maintained in the local file system
     * in the directory <code>baseDir</code>
     *
     * @param baseDir the base directory. Must not be null.
     */
    FileSystemJSModuleRepository(@NotNull File baseDir) {
        Objects.requireNonNull(baseDir);
        this.baseDir = baseDir.getAbsoluteFile();
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
        return internalResolve(new ModuleID((id)), baseDir.toURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String id, @NotNull URI contextUri) {
        ModuleID.ensureValid(id);
        Objects.requireNonNull(contextUri);

        if (! isBaseOf(contextUri)) {
            logFine(() -> MessageFormat.format(
                "resolve: failed to resolve module ''{0}''. " +
                "Context URI ''{1}'' isn''t a child of the base URI ''{2}''",
                 id, contextUri, baseDir.toURI().toString()
            ));
            return Optional.empty();
        }
        return internalResolve(new ModuleID(id), contextUri);
    }

    private boolean isModuleReadable(Path moduleFilePath) {
        final File moduleFile = moduleFilePath.toFile();
        return moduleFile.exists()
                && moduleFile.isFile()
                && moduleFile.canRead();
    }

    private void ensureBaseDirExistAndReadable(String id) {
        if (! (baseDir.exists() && baseDir.canRead() && baseDir.isDirectory())) {
            logFine(() -> MessageFormat.format(
                "failed to resolve module id ''{0}''. base dir ''{1}'' "
              + "doesn''t exist, isn''t readable, or isn''t a directory",
                id, baseDir.getAbsolutePath()
            ));
            //TODO (karl): throw something else? ResolutionException?
            throw new IllegalStateException(
                MessageFormat.format(
                    "failed to resolve module id ''{0}'' in repo ''{1}''. "
                  + "repo base dir doesn''t exist, isn''t readable, or isn''t "
                  + "a directory",
                    id, baseDir.getAbsolutePath()
                )
            );
        }
    }

    private Optional<URI> internalResolve(ModuleID id,
                                          URI contextURI) {
        // make sure baseDir is an existing and readable directory
        ensureBaseDirExistAndReadable(id.toString());
        id = id.normalized();

        // we already know, that contextURI is a non-null file:// URI
        // below the baseUri of this repo. Normalize it.
        Path contextFilePath = Paths.get(contextURI)
            .toAbsolutePath().normalize();
        if (contextFilePath.toFile().isFile()) {
            contextFilePath = contextFilePath.getParent().normalize();
        }

        // extract the contextRepoPath from the contextFilePath
        Path contextRepoPath = Paths.get("/",
             baseDir.toPath().relativize(contextFilePath).toString());

        final Optional<String> moduleRepoPath = resolve(id, contextRepoPath);
        if (!moduleRepoPath.isPresent()) {
            final Object[] params = {
                id.toString()
            };
            logFine(() -> MessageFormat.format(
                "resolve: failed to resolve module id ''{0}''. " +
                "no matching module repo path found",
                params
            ));
            return Optional.empty();
        }
        final Path moduleFilePath = Paths.get(
            baseDir.toPath().toString(),
            moduleRepoPath.get());

        final Object[] params = {
            id.toString(),
            moduleFilePath.toString()
        };
        if (! isModuleReadable(moduleFilePath)) {
            logFine(() -> MessageFormat.format(
                "resolve: failed to resolve module id ''{0}''. " +
                "resolved file path ''{1}'' doesn''t refer to a readable file",
                 params
            ));
            return Optional.empty();
        }
        logFine(() -> MessageFormat.format(
            "resolve: succeeded to resolve module id ''{0}''. " +
            "resolved file path ''{1}'' refers to a readable file",
            params
        ));
        return Optional.of(moduleFilePath.toUri());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBaseOf(@NotNull final URI moduleURI) {
        Objects.requireNonNull(moduleURI);
        if (!moduleURI.getScheme().toLowerCase().equals("file")) {
            return false;
        }
        final Path moduleFilePath = new File(moduleURI).toPath().normalize();
        return moduleFilePath.startsWith(baseDir.toPath());
    }
}
