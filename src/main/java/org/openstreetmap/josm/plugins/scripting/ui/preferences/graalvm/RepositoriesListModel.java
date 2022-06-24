package org.openstreetmap.josm.plugins.scripting.ui.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.graalvm.IRepositoriesSource;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleRepositoryBuilder;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.IESModuleRepository;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.IllegalESModuleBaseUri;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RepositoriesListModel extends AbstractListModel<URL> implements PreferenceKeys {

    static final private Logger logger = Logger.getLogger(RepositoriesListModel.class.getName());

    private final List<URL> repositories = new ArrayList<>();
    private final ListSelectionModel selectionModel;

    public RepositoriesListModel(@NotNull final ListSelectionModel selectionModel) {
        //loadCommonJSModuleRepositories();
        this.selectionModel = selectionModel;
    }

    /**
     * Loads the list moddel with repositories provided by <code>source</code>.
     *
     * @param source the source of repositories
     */
    public void loadRepositories(@NotNull final IRepositoriesSource source) {
        Objects.requireNonNull(source);
        repositories.clear();
        source.getRepositories().stream()
            .map(uri -> {
                try {
                    return uri.toURL();
                } catch(MalformedURLException e) {
                    logger.log(Level.WARNING, MessageFormat.format(
                        "failed to convert URI to URL. uri=''{0}''", uri), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .forEach(repositories::add);
    }

    /**
     * Saves the current repository URIs in this list model back to a
     * repository source.
     *
     * @param source the respository source
     */
    public void saveRepositories(@NotNull final IRepositoriesSource source) {
        Objects.requireNonNull(source);
        final var uris = repositories.stream()
            .map(url -> {
                try {
                    return url.toURI();
                } catch(URISyntaxException e) {
                    logger.log(Level.WARNING, MessageFormat.format(
                            "failed to convert URL to URI. url=''{0}''", url), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        source.setRepositories(uris);
    }

    public void loadESModuleRepositories() {
        ESModuleResolver.getInstance().getUserDefinedRepositories()
            .stream()
            .map(repo -> {
                try {
                    return repo.getBaseURI().toURL();
                } catch (MalformedURLException e) {
                    logger.log(Level.WARNING,
                        "failed to convert CommonJS module base URI to URL",
                        e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .forEach(repositories::add);
    }

    public void rememberESModuleRepositories() {
        final var resolver = ESModuleResolver.getInstance();
        final var builder = new ESModuleRepositoryBuilder();
        final List<IESModuleRepository> repos = repositories.stream()
            .map(url -> {
                try {
                    return builder.build(url.toURI());
                } catch(URISyntaxException | IllegalESModuleBaseUri e){
                    final String message = String.format(
                        "failed to create CommonJS module repository for url %s. Ignoring it.",
                        url
                    );
                    logger.log(Level.WARNING, message, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        resolver.setUserDefinedRepositories(repos);
    }

//    public void loadCommonJSModuleRepositories() {
//        CommonJSModuleRepositoryRegistry.getInstance().getUserDefinedRepositories()
//            .stream()
//            .map(repo -> {
//                try {
//                    return repo.getBaseURI().toURL();
//                } catch (MalformedURLException e) {
//                    logger.log(Level.WARNING,
//                        "failed to convert CommonJS module base URI to URL",
//                        e);
//                }
//                return null;
//            })
//            .filter(Objects::nonNull)
//            .forEach(repositories::add);
//    }
//
//    public void rememberCommonJSModuleRepositories() {
//        final List<ICommonJSModuleRepository> repos = repositories.stream()
//            .map(url -> {
//                try {
//                    return CommonJSModuleRepositoryFactory
//                        .getInstance()
//                        .build(url.toURI());
//                } catch(URISyntaxException | IllegalCommonJSModuleBaseURI e){
//                    final String message = String.format(
//                        "failed to create CommonJS module repository for "
//                            + "url %s. Ignoring it.",
//                        url
//                     );
//                    logger.log(Level.WARNING, message, e);
//                    return null;
//                }
//            })
//            .filter(Objects::nonNull)
//            .collect(Collectors.toList());
//
//        CommonJSModuleRepositoryRegistry.getInstance()
//                .setUserDefinedRepositories(repos);
//    }

    public void remove(int i) {
        repositories.remove(i);
        fireIntervalRemoved(this,i,i);
    }

    public void add(final URL url) {
        repositories.add(url);
        fireIntervalAdded(this, 0, getSize());
    }

    public void up(int i) {
        final URL toMove = repositories.remove(i);
        repositories.add(i-1, toMove);
        selectionModel.setSelectionInterval(i-1, i-1);
        fireContentsChanged(this,0,getSize());
    }

    public void down(int i) {
        final URL toMove = repositories.remove(i);
        repositories.add(i+1, toMove);
        selectionModel.setSelectionInterval(i+1, i+1);
        fireContentsChanged(this,0,getSize());
    }

    @Override
    public URL getElementAt(int index) {
        return repositories.get(index);
    }

    @Override
    public int getSize() {
        return repositories.size();
    }
}