package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.net.URI;
import java.util.List;

public interface IRepositoriesSource {

    @NotNull List<URI> getRepositories();

    void setRepositories(@Null List<URI> repositories);
}
