package org.openstreetmap.josm.plugins.scripting.preferences.common;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ImportPathModel extends AbstractListModel<File> implements PreferenceKeys {

    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(
        ImportPathModel.class.getName());

    private final List<File> paths = new ArrayList<>();
    private final DefaultListSelectionModel selectionModel;

    public ImportPathModel(DefaultListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    /**
     * Sets the paths this model manages
     *
     * @param paths the paths
     */
    public void setPaths(Collection<String> paths) {
        this.paths.clear();
        paths.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .map(File::new)
            .collect(Collectors.toCollection(() -> this.paths));
        fireContentsChanged(this, 0, this.paths.size());
    }

    /**
     * Replies a list of the paths managed by this model.
     *
     * @return the paths
     */
    public List<String> getPaths() {
        return paths.stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
    }

    /**
     * Loads the paths from the preferences with key <code>key</code>
     *
     * @param prefs the preferences
     * @param key  the preference key
     */
    public void loadFromPreferences(@NotNull Preferences prefs,
                                    String key) {
        Objects.requireNonNull(prefs);
        prefs.getList(key).stream()
            .map(String::trim)
            .filter(path -> !path.isEmpty())
            .map(File::new)
            .collect(Collectors.toCollection(() -> paths));
    }

    public void loadFromPreferences(Preferences prefs) {
        loadFromPreferences(prefs, PREF_KEY_JYTHON_SYS_PATHS);
    }

    /**
     * Saves the paths to the preferences
     *
     * @param prefs the preferences
     * @param key the preference key
     */
    public void persistToPreferences(Preferences prefs, String key) {
        List<String> entries = paths.stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());
        prefs.putList(key, entries);
    }

    /**
     * Saves the paths to the preferences
     *
     * @param prefs the preferences
     */
    public void persistToPreferences(Preferences prefs) {
        persistToPreferences(prefs, PREF_KEY_JYTHON_SYS_PATHS);
    }

    public void remove(int i) {
        paths.remove(i);
        fireIntervalRemoved(this,i,i);
    }

    public void up(int i) {
        File toMove = paths.remove(i);
        paths.add(i-1, toMove);
        selectionModel.setSelectionInterval(i-1, i-1);
        fireContentsChanged(this,0,getSize());
    }

    public void down(int i) {
        File toMove = paths.remove(i);
        paths.add(i+1, toMove);
        selectionModel.setSelectionInterval(i+1, i+1);
        fireContentsChanged(this,0,getSize());
    }

    public void add(File path) {
        if (path == null) return;
        paths.add(path);
        fireContentsChanged(this,0,getSize());
    }

    @Override
    public File getElementAt(int index) {
        return paths.get(index);
    }

    @Override
    public int getSize() {
        return paths.size();
    }
}