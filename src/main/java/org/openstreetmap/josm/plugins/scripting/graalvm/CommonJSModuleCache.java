package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;


import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a singleton cache for CommonJS modules.
 */
public class CommonJSModuleCache {

    static private CommonJSModuleCache instance = null;

    // the cache of required CommonJS modules
    private Map<Context, Map<URI, Value>> cache = new HashMap<>();

    /**
     * Replies the singleton instance of the cache
     *
     * @return the singleton instance
     */
    static public CommonJSModuleCache getInstance() {
        if (instance == null) {
            instance = new CommonJSModuleCache();
        }
        return instance;
    }

    /**
     * Remembers a module in the cache.
     *
     * The <code>module</code> is the value returned by the <tt>require()</tt>
     * function. It is remembered in the cache for a given <code>moduleURI</code>
     * and a given {@link Context}.
     *
     * @param moduleURI the module URI. Must not be null.
     * @param module the module. Must not be null.
     * @param forContext the polyglot context where the module was required
     */
    public void remember(@NotNull final URI moduleURI,
                         @NotNull final Value module,
                         @NotNull final Context forContext) {
        Objects.requireNonNull(moduleURI);
        Objects.requireNonNull(module);
        Objects.requireNonNull(forContext);

        cache.computeIfAbsent(forContext, context -> new HashMap<>())
            .put(moduleURI, module);
    }

    /**
     * Clear the module in <code>inContext</code> for <code>moduleURI</code>
     * from the cache.
     *
     * @param moduleURI the module URI
     * @param inContext the polyglot context where the module was required
     */
    public void clear(@NotNull  final URI moduleURI,
                      @NotNull  final Context inContext) {
        Objects.requireNonNull(moduleURI);
        Objects.requireNonNull(inContext);
        Map<URI, Value> modulesCache = cache.get(inContext);
        if (modulesCache == null) return;
        modulesCache.remove(moduleURI);
    }

    /**
     * Clears all modules required in the polyglot <code>context</code> from
     * the cache.
     *
     * @param context the polyglot context. Must not be null.
     */
    public void clear(@NotNull  final Context context) {
        Objects.requireNonNull(context);
        cache.remove(context);
    }

    /**
     * Lookup a module given the <code>moduleURI</code> and the
     * <code>context</code> where the module is imported.
     *
     * @param moduleURI the module URI. Must not be null.
     * @param context the polyglot context. Must not be null.
     * @return the exported value if it is cached. Empty, if no module value
     * is in the cache
     */
    public Optional<Value> lookup(
            @NotNull final URI moduleURI,
            @NotNull final Context context) {
        Objects.requireNonNull(moduleURI);
        Objects.requireNonNull(context);

        final Map<URI, Value> modulesCache = cache.get(context);
        if (modulesCache == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modulesCache.get(moduleURI));
    }
}
