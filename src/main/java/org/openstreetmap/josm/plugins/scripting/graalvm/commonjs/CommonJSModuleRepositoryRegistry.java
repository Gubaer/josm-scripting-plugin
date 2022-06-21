package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.scripting.graalvm.*;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registry of available CommonJS module repositories where we look for
 * CommonJS modules.
 *
 * Provides methods to resolve a module ID against the repositories
 * managed in this registry.
 */
@SuppressWarnings("unused")
public class CommonJSModuleRepositoryRegistry implements IModuleResolver, IRepositoriesSource {
    static private final Logger logger =
        Logger.getLogger(CommonJSModuleRepositoryRegistry.class.getName());

    static private CommonJSModuleRepositoryRegistry instance;

    /**
     * Replies the jar URI referring to the built-in CommonJS modules
     * shipped in the plugin jar.
     *
     * @param info plugin information
     * @return the URI
     */
    static public @Null URI buildRepositoryUrlForBuiltinModules(@NotNull PluginInformation info) {
        Objects.requireNonNull(info);
        try {
            return ModuleJarURI.buildJarUri(info.file.getAbsolutePath(), "/js/v2");
        } catch(MalformedURLException | URISyntaxException e) {
            logger.log(Level.WARNING, "Failed to create URI referring to the "
                  + "CommonJS modules in the plugin jar. Cannot load CommonJS "
                  + "modules for v2 API from the plugin jar.",e);
            return null;
        }
    }

    /**
     * Replies the singleton instance of the registry.
     *
     * @return the singleton instance
     */
    static public @NotNull
    CommonJSModuleRepositoryRegistry getInstance() {
        if (instance == null) {
            instance = new CommonJSModuleRepositoryRegistry();
        }
        return instance;
    }

    private ICommonJSModuleRepository builtInRepo = null;
    private List<ICommonJSModuleRepository> userDefinedRepos = new ArrayList<>();

    /**
     * Sets the built-in repository for CommonJS modules.
     *
     * This repo can't be deleted or overriden in the preferences settings.
     * It refers to the CommonJS modules shipped with the scripting plugin
     * jar.
     *
     * @param repo the repository
     */
    public void setBuiltInRepository(@Null ICommonJSModuleRepository repo) {
        this.builtInRepo = repo;
    }

    /**
     * Replies the built-in repository for CommonJS modules.
     * @return the built-in repository
     */
    public @Null ICommonJSModuleRepository getBuiltInRepository() {
        return builtInRepo;
    }

    /**
     * Creates a registry with an empty list of module repositories
     */
    private CommonJSModuleRepositoryRegistry(){}

    private Stream<ICommonJSModuleRepository> getRepositoriesAsStream() {
        return Stream.concat(
            Stream.of(builtInRepo),
            userDefinedRepos.stream()
        ).filter(Objects::nonNull);
    }

    protected boolean isPresent(final URI baseUri) {
        return getRepositoriesAsStream()
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
    public void addUserDefinedRepository(
        final @NotNull ICommonJSModuleRepository repo) {
        Objects.requireNonNull(repo);
        if (isPresent(repo.getBaseURI())) {
            return;
        }
        userDefinedRepos.add(repo);
        saveToPreferences(Preferences.main());
    }

    /**
     * Removes a CommonJS module repository from the registry.
     *
     * @param repo the repository. Must not be null.
     */
    public void removeUserDefinedRepository(
        final @NotNull ICommonJSModuleRepository repo) {
        Objects.requireNonNull(repo);
        removeUserDefinedRepository(repo.getBaseURI());
    }

    /**
     * Removes the CommonJS module repository with the base URI
     * <code>baseUri</code> from the registry.
     *
     * @param baseUri the base URI. Must not be null.
     */
    public void removeUserDefinedRepository(final @NotNull URI baseUri) {
        Objects.requireNonNull(baseUri);
        userDefinedRepos = userDefinedRepos.stream()
            .filter(repo -> ! repo.getBaseURI().equals(baseUri))
            .collect(Collectors.toList());
        saveToPreferences(Preferences.main());
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
        return userDefinedRepos.stream()
            .filter(repo -> repo.isBaseOf(moduleUri))
            .findFirst();
    }

    /**
     * Replies an unmodifiable list of the user defined repositories managed
     * by this registry.
     *
     * @return the list of repositories
     */
    public @NotNull List<ICommonJSModuleRepository> getUserDefinedRepositories() {
        return Collections.unmodifiableList(userDefinedRepos);
    }

    /**
     * Sets the user defined repositories managed by this registry.
     *
     * @param repos the list of user defined CommonJS module repositories
     */
    public void setUserDefinedRepositories(@NotNull final List<ICommonJSModuleRepository> repos) {
        Objects.requireNonNull(repos);
        userDefinedRepos.clear();
        userDefinedRepos.addAll(repos);
        saveToPreferences(Preferences.main());
    }



    /**
     * Remove all repositories from the registry.
     */
    public void clear() {
        userDefinedRepos = new ArrayList<>();
    }

    private void logFine(Supplier<String> messageBuilder) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Optional<URI> resolve(final @NotNull  String id) {
        Objects.requireNonNull(id);
        return getRepositoriesAsStream()
            .map(repo -> {
                logFine(() -> MessageFormat.format(
                    "Resolve module in repository. module ID=''{0}'', " +
                    "repository URI=''{1}''",
                    id, repo.getBaseURI().toString()
                ));
                return repo.resolve(id);
            })
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
        final ModuleID moduleID = new ModuleID(id);
        if (moduleID.isRelative()) {
            // try to resolve a relative module against each available module
            // repo, but only accept resolved modules from the same repo, to
            // which the contextUri refers
            return getRepositoriesAsStream()
                .map(repo -> {
                    logFine(() -> MessageFormat.format(
                        "Resolve module in repository with context. " +
                        "module ID=''{0}'', repository URI=''{1}'', " +
                        "context URI=''{2}''",
                        id, repo.getBaseURI().toString(),contextUri.toString()
                    ));
                    return repo.resolve(id, contextUri);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        } else {
            // resolve an absolute module ID without context against all
            // available module repository
            return resolve(id);
        }
    }

    /**
     * Save the base URIs for the user defined CommonJS module
     * repositories to preferences.
     *
     * @param pref the preferences
     */
    public void saveToPreferences(@NotNull final Preferences pref) {
        Objects.requireNonNull(pref);
        List<String> entries = userDefinedRepos.stream()
            .map(repo -> {
                try {
                    return repo.getBaseURI().toURL().toString();
                } catch (MalformedURLException e) {
                    // should not happen, just in case
                    logger.log(Level.WARNING,
                        "failed to convert CommonJS module base URI to URL",
                        e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        pref.putList(PreferenceKeys.PREF_KEY_GRAALVM_COMMONJS_MODULE_REPOSITORIES,
            entries);
    }

    /**
     * Load the base URIs for the user defined CommonJS module
     * repositories from preferences.
     *
     * @param pref the preferences
     */
    public void loadFromPreferences(@NotNull final Preferences pref) {
        Objects.requireNonNull(pref);
        final CommonJSModuleRepositoryFactory factory =
            CommonJSModuleRepositoryFactory.getInstance();
        final CommonJSModuleRepositoryRegistry repos = CommonJSModuleRepositoryRegistry.getInstance();
        repos.clear();
        pref.getList(PreferenceKeys.PREF_KEY_GRAALVM_COMMONJS_MODULE_REPOSITORIES)
            .stream()
            .map(value -> {
                try {
                    return factory.build(value);
                } catch(IllegalCommonJSModuleBaseURI e) {
                    final String message = String.format(
                          "illegal preference value for CommonJS module base URI. "
                        + "Ignoring preference value. preference: key=%s, value=%s ",
                        PreferenceKeys.PREF_KEY_GRAALVM_COMMONJS_MODULE_REPOSITORIES,
                        value);
                    logger.log(Level.WARNING,message, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(repos::addUserDefinedRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<URI> getRepositories() {
        return userDefinedRepos.stream()
            .map(ICommonJSModuleRepository::getBaseURI)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepositories(@Null List<URI> repositories) {
        this.userDefinedRepos.clear();
        if (repositories == null) {
            return;
        }
        final var repos = repositories.stream()
            .map(uri -> {
                try {
                    return CommonJSModuleRepositoryFactory.getInstance().build(uri);
                } catch (IllegalCommonJSModuleBaseURI e) {
                    logger.log(Level.WARNING, MessageFormat.format(
                            "Illegal base URI for CommonJS module. uri=''{0}''", uri), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        this.userDefinedRepos.addAll(repos);
    }
}
