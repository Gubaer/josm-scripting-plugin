package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;

/**
 * A repository for ES Modules.
 */
public interface IESModuleRepository {

    String ES_MODULE_REPO_PATH_PREFIX = "es-module-repo";

    /**
     * Replies the base URI of this repository
     *
     * @return the base URI
     */
    @NotNull URI getBaseURI();

    /**
     * Replies the unique path prefix for absolute module paths which refer to a module
     * in this repository.
     * <p>
     * The unique path prefix starts with <code>es-module-repo</code> followed by a {@link java.util.UUID}.
     * Example: <code>es-modules/23e4567-e89b-12d3-a456-426614174000</code>.
     *
     * @return the unique path prefix
     */
    @NotNull RelativePath getUniquePathPrefix();

    /**
     * Replies true if <code>modulePath</code> is a module path which refers to a module in this repository.
     * <p>
     * An  module path starts with the prefix <code>es-modules/&lt;uuid&gt;</code>,
     * for example with <code>es-module-repo/23e4567-e89b-12d3-a456-426614174000</code>. Each
     * ES Modules repository has a unique UUID.
     *
     * @param modulePath the module path
     * @return true, if <code>modulePath</code> is an absolute module path; false, otherwise
     */
    boolean matchesWithUniquePathPrefix(@NotNull final RelativePath modulePath);

    /**
     * Resolves a module path in the repository and replies a repository path
     * if the module exists in the repository, or null, if it doesn't exist.
     * <p>
     * A module path is either an absolute or a relative path, i.e.
     * <ul>
     *     <li>es-module-repo/23e4567-e89b-12d3-a456-426614174000/baz</li> - an absolute module path
     *     <li>foo/bar</li> - a relative module path
     *     <li>./foo/bar</li> - a relative module path with leading <code>./</code></li>
     *     <li>./foo/bar/../baz</li> - a relative module path with <code>./</code> and <code>../</code> segments</li>
     * </ul>
     *
     * A module path can include <code>./</code> and <code>../</code> segments.
     * <p>
     * The file suffix is optional. A module path <code>foo/bar/baz</code> is always
     * resolved against <code>foo/bar/baz</code>, <code>foo/bar/baz.mjs</code>, and
     * <code>foo/bar/baz.js</code>, in this order. It is resolved against the first
     * of these alternatives for which a readable file is found in the underlying file
     * store.
     *
     * @param modulePath the module path
     * @return the absolute module path or null, if <code>modulePath</code> can't be converted to an
     * absolute module path
     */
    @Null RelativePath resolveModulePath(@NotNull final RelativePath modulePath);


    /**
     * Replies a {@link SeekableByteChannel channel} to read the module content given
     * by the module path <code>path</code>.
     *
     * @param path an absolute module path
     * @return the channel
     * @throws IOException if accessing the content in the underlying file store files
     * @throws NullPointerException if <code>path</code> is null
     * @throws IllegalArgumentException if <code>path</code> can't be resolved in this
     *  repository
     */
    @NotNull SeekableByteChannel newByteChannel(@NotNull final RelativePath path) throws IOException;
}
