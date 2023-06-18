package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

abstract public class AbstractESModuleRepository implements IESModuleRepository {
    // the unique path prefix for absolute paths that refer to modules in this repo
    private final Path uniquePathPrefix = Path.of("/es-module-repo", UUID.randomUUID().toString());

    /**
     * Replies true if <code>modulePath</code> starts with the prefix for a module path
     * which refers into an ES Module repository. Such a path has to be absolute. It
     * has to match the pattern <code>/es-module-repo/&ltuuid&gt</code>.
     *
     * @param modulePath the module path
     * @return true if <code>modulePath</code> starts with the prefix; false, otherwise
     */
    static public boolean startsWithESModuleRepoPathPrefix(@NotNull final Path modulePath) {
        if (modulePath == null) {
            return false;
        }
        if ( /* !modulePath.isAbsolute()
                || */ ! (modulePath.getNameCount() >= 2)
                || ! modulePath.getName(0).toString().equalsIgnoreCase(ES_MODULE_REPO_PATH_PREFIX)) {
            return false;
        }
        var uuid = modulePath.getName(1).toString();
        try {
            UUID.fromString(uuid);
            return true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Path getUniquePathPrefix() {
        return uniquePathPrefix;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesWithUniquePathPrefix(@NotNull Path modulePath) {
        Objects.requireNonNull(modulePath);
        return modulePath.startsWith(uniquePathPrefix);
    }
}
