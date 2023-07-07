package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

public interface IModuleResolver {
    /**
     * Resolves the CommonJS module id against the base URI and replies
     * its module URI
     * <p>
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
    @NotNull
    Optional<URI> resolve(@NotNull String id);

    /**
     * Resolves the CommonJS module id against the base contextUri and replies
     * its module URI
     * <p>
     * A module id is either a &laquo;toplevel&raquo; or a &laquo;relative&raquo;
     * id.
     * <ul>
     *     <li>a relative module id start with <code>./</code> or
     *     <code>../</code></li>
     *     <li>everything else is a toplevel module id</li>
     * </ul>
     *
     * A module id must not start with <code>/</code>.
     * <p>
     * The context URI must be a child URI of base URI of this repository.
     * <p>
     * The resolution fails (an empty optional is replied), if the resolved
     * module URI isn't a child URI of the base URI.
     *
     * @param id the module id
     * @param contextUri the context URI
     * @return the resolved URI
     * @throws NullPointerException id or contextUri are null
     * @throws IllegalArgumentException id or contextUri are invalid
     */
    @NotNull Optional<URI> resolve(@NotNull String id, @NotNull URI contextUri);
}
