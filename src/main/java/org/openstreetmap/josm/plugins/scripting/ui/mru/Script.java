package org.openstreetmap.josm.plugins.scripting.ui.mru;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.text.MessageFormat.format;

/**
 * Descriptor of a runnable script in the list of most recently run scripts.
 *
 * @param scriptPath the path to the script. Must not be null.
 * @param engineId   the script engine's ID, i.e. <code>graalvm/js</code>
 */
public record Script(@NotNull String scriptPath, @Null String engineId) {

    static public final String ATTR_SCRIPT_PATH = "scriptPath";
    static public final String ATTR_ENGINE_ID = "engineId";

    /**
     * @throws NullPointerException if <code>scriptPath<code></code> is null
     */
    public Script {
        Objects.requireNonNull(scriptPath);
    }

    /**
     * Creates a script file descriptor given a {@link Map}.
     * <p>
     * The map must include a non-null value for the key <code>scriptPath</code>.
     * The map may include a value for the key <code>engineId</code>. This value may be null.
     * All other keys in the map are ignored.
     *
     * @param preferences the preferences. Mut not be null.
     * @return the script descriptor
     * @throws NullPointerException     if <code>preferences</code> is null
     * @throws IllegalArgumentException if the value for key <code>scriptPath</code> is missing or null
     */
    static public Script from(@NotNull final Map<String, String> preferences) {
        Objects.requireNonNull(preferences);
        final var scriptPath = preferences.get(ATTR_SCRIPT_PATH);
        if (scriptPath == null) {
            throw new IllegalArgumentException(format(
                "''preferences'' must include a non-null value for key ''{0}''", ATTR_SCRIPT_PATH));
        }
        final var engineId = preferences.get(ATTR_ENGINE_ID);
        return new Script(scriptPath, engineId);
    }

    /**
     * Creates a map with the attributes of this script.
     * <p>
     * The map contains to key/value pairs:
     * <ul>
     *     <li><code>scriptPath</code>: the script file's path</li>
     *     <li><code>engineId</code>: the engine ID. Missing, if the engine ID is unknown.</li>
     * </ul>
     *
     * @return the map with the scripts attributes
     */
    public @NotNull Map<String, String> toMap() {
        //Note: cannot use an immutable map, i.e. using Map.of(...). Triggers a NullPointerException
        //in https://github.com/JOSM/josm/blob/b6550fcdb34e94676e52d6936446e4cbbe017474/src/org/openstreetmap/josm/spi/preferences/MapListSetting.java#L41
        final var map = new HashMap<String, String>();
        map.put(ATTR_SCRIPT_PATH, scriptPath);
        if (engineId != null) {
            map.put(ATTR_ENGINE_ID, engineId);
        }
        return map;
    }

    @Override
    public String toString() {
        return format("Script: scriptPath=''{0}'', engineId=''{1}''", scriptPath, engineId);
    }

    /**
     * Replies a copy of this script descriptor with the given script path and a new engine ID.
     *
     * @param engineId the engine ID. Must not be null.
     * @return the script
     * @throws NullPointerException if <code>engineId</code> is null
     */
    public @NotNull Script withEngineId(@NotNull final String engineId) {
        Objects.requireNonNull(engineId);
        return new Script(scriptPath, engineId);
    }
}

