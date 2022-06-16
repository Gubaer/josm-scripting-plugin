package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

abstract public class BaseJSModuleRepository
        implements ICommonJSModuleRepository {

    static private final Logger logger =
         Logger.getLogger(BaseJSModuleRepository.class.getName());

    protected Logger getLogger() {
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
     * <code>repoPath</code> should be an absolute path, not a JAR entry key.
     * Example: <code>/foo/bar/module.js</code>, not
     * <code>foo/bar/module.js</code>.
     *
     * @param repoPath the path to the entry in a JAR file
     * @return true, if <code>repoPath</code> refers to a readable file entry
     */
    abstract protected boolean isRepoFile(@NotNull final String repoPath);

    @SuppressWarnings("unused")
    static public @NotNull String moduleIdToModulePath(
            @NotNull final ModuleID moduleId) {
        if (moduleId.isAbsolute()) {
            return "/" + moduleId;
        } else {
            return moduleId.toString();
        }
    }

    @SuppressWarnings("WeakerAccess") // used in subclasses
    protected void logFine(Supplier<String> messageBuilder) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message);
        }
    }

    private @NotNull Optional<Path> tryResolve(@NotNull final ModuleID moduleId,
                                                 @NotNull Path context) {

        final Path modulePath = context.resolve(moduleId.toString())
            .normalize();

        final Object[] params = new Object[]{
            moduleId.toString(),
            modulePath.toString(),
            context.toString()
        };

        if (isRepoFile(modulePath.toString())) {
            logFine(() -> MessageFormat.format(
                "MODULE PATH ALTERNATIVE: " +
                "succeeded to resolve module id ''{0}''. " +
                "Resolved path ''{1}'' refers to a readable file",
                params
            ));
            return Optional.of(modulePath);
        }
        logFine(() -> MessageFormat.format(
            "MODULE PATH ALTERNATIVE: failed to resolve module id ''{0}''. " +
            "resolved path ''{1}'' doesn''t refer to a readable file",
            params
        ));

        return Optional.empty();
    }

    protected @NotNull Optional<String> resolve(@NotNull final ModuleID moduleId,
                                       @NotNull Path contextPath) {

        Objects.requireNonNull(contextPath);
        contextPath = contextPath.normalize();
        if (!contextPath.startsWith("/")) {
            final String message = format(
                "resolved: failed to resolve module ID. " +
                "context path isn''t absolute. moduleId=''{0}'', " +
                "contextPath=''{1}''",
                moduleId.toString(), contextPath.toString());
            logger.log(Level.WARNING, message);
            return Optional.empty();
        }
        Path context = contextPath;
        final String workingModuleId = moduleId.normalized().toString();
        final String[] alternatives =  new String[]{
            workingModuleId,
            workingModuleId + ".js",
            workingModuleId + "/index.js"
        };

        Optional<Path> path = Arrays.stream(alternatives)
            .map(ModuleID::new)
            .map(id -> tryResolve(id, context))
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty());

        return path.map(Path::toString);
    }
}
