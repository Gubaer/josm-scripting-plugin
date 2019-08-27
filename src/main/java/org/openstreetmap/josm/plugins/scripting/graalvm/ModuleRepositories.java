package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of available CommonJS module repositories where we look for
 * CommonJS modules.
 *
 * Provides methods to resolve a module ID against the repositories
 * managed in this registry.
 */
public class ModuleRepositories implements IModuleResolver {

    static private ModuleRepositories instance;

    /**
     * Replies the singleton instance of the registry.
     *
     * @return the singleton instance
     */
    static public @NotNull ModuleRepositories getInstance() {
        if (instance == null) {
            //TODO(karl): init with default repo
            instance = new ModuleRepositories();
        }
        return instance;
    }

    private List<ICommonJSModuleRepository> repos = new ArrayList<>();

    /**
     * Creates a registry with an empty list of module repositories
     */
    private ModuleRepositories(){}

    protected boolean isPresent(final URI baseUri) {
        return repos.stream()
            .anyMatch(repo -> repo.getBaseURI().equals(baseUri));
    }

    /**
     * Adds a CommonJS module repository to the registry.
     *
     * Ignores the repo if it is already present in the registry. Appends
     * <code>repo</code> to the end of the repo list. Modules in this repository
     * are therefore looked up last.
     *
     * @param repo the repository. Must not be null.
     */
    public void add(final @NotNull ICommonJSModuleRepository repo) {
        Objects.requireNonNull(repo);
        if (isPresent(repo.getBaseURI())) {
            return;
        }
        repos.add(repo);
    }

    /**
     * Removes a CommonJS module repository from the registry.
     *
     * @param repo the repository. Must not be null.
     */
    public void remove(final @NotNull ICommonJSModuleRepository repo) {
        Objects.requireNonNull(repo);
        remove(repo.getBaseURI());
    }

    /**
     * Removes the CommonJS module repository with the base URI
     * <code>baseUri</code> from the registry.
     *
     * @param baseUri the base URI. Must not be null.
     */
    public void remove(final @NotNull URI baseUri) {
        Objects.requireNonNull(baseUri);
        repos = repos.stream()
            .filter(repo -> ! repo.getBaseURI().equals(baseUri))
            .collect(Collectors.toList());
    }

    /**
     * Lookup and reply the CommonJS repository providing the module with
     * the module URI <code>moduleURI</code>.
     *
     * Just replies the repository which would provide the module, but
     * doesn't make sure, that the module with this module URI actually
     * exists.
     *
     * @param moduleUri the module URI
     */
    public @NotNull Optional<ICommonJSModuleRepository> getRepositoryForModule(
            final @NotNull URI moduleUri) {
        Objects.requireNonNull(moduleUri);
        return repos.stream()
            .filter(repo -> repo.isBaseOf(moduleUri))
            .findFirst();
    }

    /**
     * Replies an unmodifiable list of the repositories managed by this
     * registry.
     *
     * @return the list of repositories
     */
    public @NotNull List<ICommonJSModuleRepository> getRepositories() {
        return Collections.unmodifiableList(repos);
    }

    /**
     * Remove all repositories from the registry.
     */
    public void clear() {
        repos = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<URI> resolve(final @NotNull  String id) {
        Objects.requireNonNull(id);
        return repos.stream()
            .map(repo -> repo.resolve(id))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<URI> resolve(final @NotNull String id,
                                 final @NotNull  URI contextUri) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(contextUri);
        return repos.stream()
            .map(repo -> repo.resolve(id, contextUri))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
