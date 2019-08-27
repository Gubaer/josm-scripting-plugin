package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A <strong>FileSystemJSModuleRepository</strong> resolves CommonJS
 * module stored in a directory in the file system.
 */
public class FileSystemJSModuleRepository implements ICommonJSModuleRepository {
    private static Logger logger =
        Logger.getLogger(FileSystemJSModuleRepository.class.getName());

    private File baseDir;

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

    private void ensureValidId(String id) {
        Objects.requireNonNull(id);
        id = id.trim();
        // id must not be empty
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id must not be empty");
        }

        // id must not start with '/'
        if (id.startsWith("/")) {
            throw new IllegalArgumentException(MessageFormat.format(
                "a module id must not start with '/', got ''{0}''",
                id
            ));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String id) {
        ensureValidId(id);
        return internalResolve(id, baseDir.toURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull String id, @NotNull URI contextUri) {
        ensureValidId(id);
        Objects.requireNonNull(contextUri);

        if (! isBaseOf(contextUri)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "context URI ''{0}'' isn't a child of the base URI ''{1}''",
                    contextUri, baseDir.toURI().toString()
            ));
        }
        return internalResolve(id, contextUri);
    }

    private void logFine(Supplier<String> messageBuilder) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message);
        }
    }

    private boolean isModuleReadable(Path modulePath) {
        final File moduleFile = modulePath.toFile();
        return moduleFile.exists()
                && moduleFile.isFile()
                && moduleFile.canRead();
    }

    private boolean tryModulePathAlternative(String id, Path modulePath) {
        final Path basePath = baseDir.toPath();
        final boolean moduleIsReadable = isModuleReadable(modulePath);
        final Object[] params = new Object[]{
            id,
            modulePath.toAbsolutePath().toString(),
            basePath.toAbsolutePath().toString()
        };

        if (! moduleIsReadable) {
            logFine(() -> MessageFormat.format(
                "MODULE PATH ALTERNATIVE: failed to resolve module id ''{0}''. "
              + "resolved path ''{1}'' doesn''t refer to a readable file",
                params
            ));
        } else {
            logFine(() -> MessageFormat.format(
                "MODULE PATH ALTERNATIVE: succeeded to resolve module id ''{0}''. "
              + "resolved path ''{1}'' refers to a readable file",
                params
            ));
        }
        return moduleIsReadable;
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

    private String normalizeModuleId(String moduleId) {
        if (moduleId.endsWith(".js")) {
            return moduleId.substring(0, moduleId.length() - 3);
        } else {
            return moduleId;
        }
    }

    private boolean isAbsolute(@NotNull String moduleId) {
        return !(moduleId.startsWith("./") || moduleId.startsWith("../"));
    }

    private Optional<URI> internalResolve(String id,
                                          URI contextURI) {
        // make sure baseDir is an existing and readable directory
        ensureBaseDirExistAndReadable(id);

        // remove trailing ".js" in module id, if any. For convenience only,
        // a file suffix ".js" should't be part of a module id.
        id = normalizeModuleId(id);

        final Path basePath = Paths.get(baseDir.toURI())
            .toAbsolutePath().normalize();
        Path resolvedPath = Paths.get(contextURI).resolve(id)
            .toAbsolutePath().normalize();

        if (! resolvedPath.startsWith(basePath)) {
            final String _id = id;
            final String _path = resolvedPath.toString();
            logFine(() -> MessageFormat.format(
                "failed to resolve module id ''{0}''. resolved path ''{1}'' "
              + "isn''t a child path relative to the base path ''{2}''",
                _id, _path, basePath.toString()
            ));
            return Optional.empty();
        }

        // normalize context URI. Always resolve against a directory, even
        // if the context URI refers to a file
        final Path contextPath = Paths.get(contextURI)
            .toAbsolutePath().normalize();
        if (contextPath.toFile().isFile()) {
            contextURI = contextPath.getParent().toUri();
            resolvedPath = Paths.get(contextURI).resolve(id)
                .toAbsolutePath().normalize();
        }

        // always resolve absolute module ids against the base dir
        if (isAbsolute(id)) {
            contextURI = baseDir.toURI();
            resolvedPath = Paths.get(contextURI).resolve(id)
                .toAbsolutePath().normalize();
        }

        if (tryModulePathAlternative(id, resolvedPath)) {
            return Optional.of(resolvedPath.toUri());
        }

        final Path alternativePath = Paths.get(resolvedPath.toString() + ".js")
            .toAbsolutePath().normalize();
        if (tryModulePathAlternative(id, alternativePath)) {
            return Optional.of(alternativePath.toUri());
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBaseOf(@NotNull final URI moduleURI) {
        Objects.requireNonNull(moduleURI);
        return moduleURI.toString().startsWith(baseDir.toURI().toString());
    }
}
