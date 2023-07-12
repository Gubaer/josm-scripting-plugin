package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;
import java.util.UUID;

abstract public class AbstractESModuleRepository implements IESModuleRepository {
    // the unique path prefix for absolute paths that refer to modules in this repo
    private final RelativePath uniquePathPrefix = RelativePath.of(ES_MODULE_REPO_PATH_PREFIX, UUID.randomUUID().toString());

    /**
     * Replies true if <code>modulePath</code> starts with the prefix for a module path
     * which refers into an ES Module repository. Such a path has to be absolute. It
     * has to match the pattern <code>/es-module-repo/&ltuuid&gt</code>.
     *
     * @param modulePath the module path
     * @return true if <code>modulePath</code> starts with the prefix; false, otherwise
     */
    static public boolean startsWithESModuleRepoPathPrefix(@Null final RelativePath modulePath) {
        if (modulePath == null) {
            return false;
        }
        if (modulePath.getLength() < 2 ) {
            return false;
        }
        if (! ES_MODULE_REPO_PATH_PREFIX.equals(modulePath.getSegment(0))) {
            return false;
        }
        var uuid = modulePath.getSegment(1);
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
    public @NotNull RelativePath getUniquePathPrefix() {
        return uniquePathPrefix;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesWithUniquePathPrefix(@NotNull RelativePath modulePath) {
        Objects.requireNonNull(modulePath);
        return modulePath.startsWith(uniquePathPrefix);
    }
}
