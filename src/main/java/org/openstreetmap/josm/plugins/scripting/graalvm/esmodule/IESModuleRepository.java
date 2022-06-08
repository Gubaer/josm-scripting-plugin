package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

/**
 * A registry for ES Modules.
 */
public interface IESModuleRepository {

    /**
     * Replies the unique path prefix for absolute module paths which refer to a module
     * in this repository.
     *
     * The unique path prefix starts with <code>/es-module-repo</code> followed by a {@link java.util.UUID}.
     * Example: <code>/es-modules/23e4567-e89b-12d3-a456-426614174000</code>.
     *
     * @return the unique path prefix
     */
    @NotNull Path getUniquePathPrefix();

    /**
     * Replies true if <code>modulePath</code> is an absolute module path
     * which refers to a module in this repository.
     *
     * An absolute module path starts with the prefix <code>/es-modules/&lt;uuid&gt;</code>,
     * for example with <code>/es-module-repo/23e4567-e89b-12d3-a456-426614174000</code>. Each
     * ES Modules repository has a unique UUID.
     *
     * @param modulePath the module path
     * @return true, if <code>modulePath</code> is an absolute module path; false, otherwise
     */
    boolean isAbsoluteModulePath(@NotNull final Path modulePath);

    /**
     * Resolves a module path in the repository and replies a repository path
     * if the module exists in the repository, or null, if it doesn't exist.
     *
     * A module path is either an absolute or a relative path, i.e.
     * <ul>
     *     <li>/es-module-repo/23e4567-e89b-12d3-a456-426614174000/baz</li> - an absolute module path
     *     <li>foo/bar</li> - a relative path
     *     <li>./foo/bar</li> - a relative path with leading <code>./</code></li>
     *     <li>./foo/bar/../baz</li> - a relative path with <code>./</code> and <code>../</code> segments</li>
     * </ul>
     *
     * A module path can include <code>./</code> and <code>../</code>.
     *
     * The file suffix is optional. A module path <code>foo/bar/baz</code> is always
     * resolved against <code>foo/bar/baz</code>, <code>foo/bar/baz.mjs</code>, and
     * <code>foo/bar/baz.js</code>, in this order. It is resolved against the first
     * of these alternatives for which a readable file is found in underlying file
     * store.
     *
     * @param modulePath the module path
     * @return the absolute module path or null, if <code>modulePath</code> can't be converted to an
     * absolute module path
     */
    @Null Path resolveModulePath(@NotNull final String modulePath);

    @Null Path resolveModulePath(@NotNull final Path modulePath);

    SeekableByteChannel newByteChannel(@NotNull final Path absolutePath) throws IOException;
}
