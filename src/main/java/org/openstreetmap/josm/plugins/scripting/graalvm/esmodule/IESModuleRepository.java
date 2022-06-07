package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

/**
 * A registry for ES Modules
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
     *     <li>/foo/bar/baz</li> - an absolute path
     *     <li>foo/bar</li> - a relative path
     *     <li>./foo/bar</li> - a relative path with leading <code>./</code></li>
     *     <li>./foo/bar/../baz</li> - a relative path with <code>./</code> and <code>../</code> segments</li>
     * </ul>
     *
     * A module path can include <code>./</code> and <code>../</code>.
     *
     * A module path can have a suffix. If it has a suffix, the path is resolved
     * against a file with a file with the same name, including the suffix. If it
     * doesn't have a suffix, whether a matching files with the suffix <code>.mjs</code>
     * or <code>.js</code> exists and is readable.
     *
     * @param modulePath the module path
     * @return the absolute
     */
    @Null
    Path resolveModulePath(@NotNull final String modulePath);

    @Null Path resolveModulePath(@NotNull final Path modulePath);

    SeekableByteChannel newByteChannel(@NotNull final Path absolutePath) throws IOException;
}
