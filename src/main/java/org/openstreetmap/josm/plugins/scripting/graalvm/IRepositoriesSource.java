package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.net.URI;
import java.util.List;

/**
 * A source of module repositories, either CommonJS modules or ES Modules.
 */
public interface IRepositoriesSource {

    /**
     * Replies the list of repository base URIs for the repositories
     * managed by this source.
     *
     * @return the list of base URIs
     */
    @NotNull List<URI> getRepositories();

    /**
     * Sets a list of repository base URIs for which this source
     * provides repositories.
     *
     * @param repositories the base URIs
     */
    void setRepositories(@Null List<URI> repositories);
}
