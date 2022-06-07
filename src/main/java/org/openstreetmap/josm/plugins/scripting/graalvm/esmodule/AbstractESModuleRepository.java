package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

abstract public class AbstractESModuleRepository implements IESModuleRepository {
    // the unique path prefix for absolute paths that refer to modules in this repo
    private final Path uniquePathPrefix = Path.of("/es-module-repo", UUID.randomUUID().toString());

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
    public boolean isAbsoluteModulePath(@NotNull Path modulePath) {
        Objects.requireNonNull(modulePath);
        return modulePath.startsWith(uniquePathPrefix);
    }
}
