package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;
import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

abstract public class BaseJSModuleRepository implements ICommonJSModuleRepository {

    static protected final Logger logger =
         Logger.getLogger(BaseJSModuleRepository.class.getName());

    static protected Logger getLogger() {
        return logger;
    }

    public enum ContextType {
        FILE_CONTEXT,
        DIRECTORY_CONTEXT
    }

    protected abstract @NotNull ContextType getContextType(@NotNull final RelativePath context) throws IOException;

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
    abstract protected boolean isRepoFile(@NotNull final RelativePath repoPath);

    private @NotNull Optional<RelativePath> tryResolve(@NotNull ModuleID moduleId,
                                               @NotNull RelativePath context) {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format("moduleId=''{0}'', context=''{1}''", moduleId, context));
        }
        final ContextType contextType;
        try {
            contextType = getContextType(context);
        } catch(IOException e) {
            //TODO(gubaer): log it
           return Optional.empty();
        }
        final Optional<RelativePath> modulePath;
        switch(contextType) {
            case FILE_CONTEXT:
                modulePath = moduleId.toRelativePath().resolveAgainstFileContext(context);
                break;
            case DIRECTORY_CONTEXT:
                modulePath = moduleId.toRelativePath().resolveAgainstDirectoryContext(context);
                break;
            default:
                // should not happen
                throw new IllegalStateException();
        }
        if (modulePath.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format(
                    "moduleId=''{0}'', context=''{1}'': failed to resolve against file context", moduleId, context
                ));
            }
            return Optional.empty();
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format(
                "moduleId=''{0}'', context=''{1}'', modulePath=''{2}''", moduleId, context, modulePath.get()
            ));
        }

        if (isRepoFile(modulePath.get())) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, format(
                    "succeeded to resolve module id ''{0}''. Resolved path ''{1}'' refers to a readable file",
                    moduleId, modulePath.get()
                ));
            }
            return modulePath;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format(
                "failed to resolve module id ''{0}''. resolved path ''{1}'' doesn''t refer to a readable file",
                moduleId, modulePath.get()
            ));
        }
        return Optional.empty();
    }

    protected @NotNull Optional<RelativePath> resolve(@NotNull final ModuleID moduleId,
                                       @NotNull RelativePath contextPath) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format(
                "moduleId=''{0}'', context=''{1}''", moduleId, contextPath
            ));
        }
        Objects.requireNonNull(moduleId);
        Objects.requireNonNull(contextPath);

        final String workingModuleId = moduleId.toRelativePath().toString();
        final String[] alternatives =  new String[]{
            workingModuleId,
            workingModuleId + ".js",
            workingModuleId + "/index.js"
        };

        final RelativePath context = contextPath;
        return Arrays.stream(alternatives)
            .map(id -> new ModuleID(RelativePath.parse(id)))
            .map(id -> tryResolve(id, context))
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty());
    }
}
