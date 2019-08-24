package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

/**
 * A repository of CommonJS modules. It has a base URI and it is able
 * to resolve CommonJS module ids against it.
 */
public interface ICommonJSModuleRepository {

    /**
     * Replies the base URI of this repository
     *
     * @return the base URI
     */
    @NotNull  URI getBaseURI();

    /**
     * Resolves the CommonJS module id against the base URI and replies
     * its module URI
     *
     * A module id is either a &laquo;toplevel&raquo; or a &laquo;relative&raquo;
     * id.
     * <ul>
     *     <li>a relative module id start with <code>./</code> or
     *     <code>../</code></li>
     *     <li>everything else is a toplevel module id</li>
     * </ul>
     *
     * A module id must not start with <code>/</code>.
     *
     * @param id the module id
     * @return the resolved URI
     * @throws NullPointerException if id is null
     * @throws IllegalArgumentException if id is invalid
     */
    @NotNull Optional<URI> resolve(@NotNull String id);

    /**
     * Resolves the CommonJS module id against the base contextURI and replies
     * its module URI
     *
     * A module id is either a &laquo;toplevel&raquo; or a &laquo;relative&raquo;
     * id.
     * <ul>
     *     <li>a relative module id start with <code>./</code> or
     *     <code>../</code></li>
     *     <li>everything else is a toplevel module id</li>
     * </ul>
     *
     * A module id must not start with <code>/</code>.
     *
     * The context URI must be a child URI of base URI of this repository.
     *
     * The resolution fails (an empty optional is replied), if the resolved
     * module URI isn't a child URI of the base URI.
     *
     * @param id the module id
     * @param contextURI the context URI
     * @return the resolved URI
     * @throws NullPointerException id or contextURI are null
     * @throws IllegalArgumentException id or contextURI are invalid
     */
    @NotNull Optional<URI> resolve(@NotNull String id, @NotNull URI contextURI);

    /**
     * Replies true if the base URI of this repo is the base of the module
     * URI
     *
     * @param moduleURI the module uri
     * @return true, if this repo is the base for the module URI
     */
    boolean isBaseOf(@NotNull URI moduleURI);
}
