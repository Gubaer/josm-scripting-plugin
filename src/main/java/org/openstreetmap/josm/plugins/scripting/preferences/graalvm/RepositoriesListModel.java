package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;

import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryFactory;
import org.openstreetmap.josm.plugins.scripting.graalvm.CommonJSModuleRepositoryRegistry;
import org.openstreetmap.josm.plugins.scripting.graalvm.ICommonJSModuleRepository;
import org.openstreetmap.josm.plugins.scripting.graalvm.IllegalCommonJSModuleBaseURI;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RepositoriesListModel extends AbstractListModel<URL>
        implements PreferenceKeys {

    static final private Logger logger = Logger.getLogger(RepositoriesListModel.class.getName());

    private final List<URL> repositories = new ArrayList<>();
    private final DefaultListSelectionModel selectionModel;

    public RepositoriesListModel(DefaultListSelectionModel selectionModel) {
        loadCommonJSModuleRepositories();
        this.selectionModel = selectionModel;
    }

    public void loadCommonJSModuleRepositories() {
        CommonJSModuleRepositoryRegistry.getInstance().getUserDefinedRepositories()
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

    public void rememberCommonJSModuleRepositories() {
        final List<ICommonJSModuleRepository> repos = repositories.stream()
            .map(url -> {
                try {
                    return CommonJSModuleRepositoryFactory
                        .getInstance()
                        .build(url.toURI());
                } catch(URISyntaxException | IllegalCommonJSModuleBaseURI e){
                    final String message = String.format(
                        "failed to create CommonJS module repository for "
                            + "url %s. Ignoring it.",
                        url
                     );
                    logger.log(Level.WARNING, message, e);
                    return null;
                }
            })
            .collect(Collectors.toList());

        CommonJSModuleRepositoryRegistry.getInstance()
                .setUserDefinedRepositories(repos);
    }

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