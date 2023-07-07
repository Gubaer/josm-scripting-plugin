package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

abstract public class BaseJSModuleRepository
        implements ICommonJSModuleRepository {

    static protected final Logger logger =
         Logger.getLogger(BaseJSModuleRepository.class.getName());

    static protected Logger getLogger() {
        return logger;
    }

    /**
     * Implementations should return true, if the following conditions
     * hold:
     * <ul>
     *     <li><code>repoPath</code> refers to an entry in this repository</li>
     *     <li><code>repoPath</code> refer to a file entry, not a directory
     *     entry</li>
     *     <li><code>repoPath</code> refers to a readable file entry</li>
     * </ul>
     *
     * <code>repoPath</code> <bold>must not</bold> be an absolute path.
     * Example: <code>foo/bar/module.js</code>, not
     * <code>/foo/bar/module.js</code>.
     *
     * @param repoPath the path to the entry in a module repository
     * @return true, if <code>repoPath</code> refers to a readable file entry in the module repository
     */
    abstract protected boolean isRepoFile(@NotNull final String repoPath);

    private @NotNull Optional<Path> tryResolve(@NotNull ModuleID moduleId,
                                               @NotNull Path context) {

        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "moduleId=''{0}'', context=''{1}''",
                moduleId,
                context.toString().replace("\\", "/")
            );
            logger.log(Level.FINE, message);
        }
        final Path modulePath = context.resolve(moduleId.toString()).normalize();
        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "moduleId=''{0}'', context=''{1}'', modulePath=''{2}''",
                moduleId,
                context.toString().replace("\\", "/"),
                modulePath.toString().replace("\\", "/")
            );
            logger.log(Level.FINE, message);
        }

        if (isRepoFile(modulePath.toString())) {
            if (logger.isLoggable(Level.FINE)) {
                final String message = MessageFormat.format(
            "MODULE PATH ALTERNATIVE-01: " +
                    "succeeded to resolve module id ''{0}''. " +
                    "Resolved path ''{1}'' refers to a readable file",
                    moduleId, modulePath.toString().replace("\\", "/")
                );
                logger.log(Level.FINE, message);
            }
            return Optional.of(modulePath);
        }
        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
        "MODULE PATH ALTERNATIVE-02: failed to resolve module id ''{0}''. " +
                "resolved path ''{1}'' doesn''t refer to a readable file",
                moduleId, modulePath.toString().replace("\\", "/")
            );
            logger.log(Level.FINE, message);
        }
        return Optional.empty();
    }

    protected @NotNull Optional<Path> resolve(@NotNull final ModuleID moduleId,
                                       @NotNull Path contextPath) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = MessageFormat.format(
                "moduleId=''{0}'', context=''{1}''",
                moduleId,
                contextPath.toString().replace("\\", "/")
            );
            logger.log(Level.FINE, message);
        }
        Objects.requireNonNull(moduleId);
        Objects.requireNonNull(contextPath);
        contextPath = contextPath.normalize();
        if (contextPath.startsWith("/")) {
            final String message = format(
                "resolved: failed to resolve module ID. " +
                "context path must not be absolute. moduleId=''{0}'', " +
                "contextPath=''{1}''",
                moduleId.toString(),
                contextPath.toString().replace("\\", "/")
            );
            logger.log(Level.WARNING, message);
            return Optional.empty();
        }
        final String workingModuleId = moduleId.normalized().toString().replace("\\", "/");
        final String[] alternatives =  new String[]{
            workingModuleId,
            workingModuleId + ".js",
            workingModuleId + "/index.js"
        };

        final Path context = contextPath;
        return Arrays.stream(alternatives)
            .map(ModuleID::new)
            .map(id -> tryResolve(id, context))
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty());
    }
}
