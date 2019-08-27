package org.openstreetmap.josm.plugins.scripting.graalvm;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the <code>require()</code> function which is added
 * to evaluation context when a CommonJS module is loaded and evaluated.
 */
public class RequireFunction implements Function<String, Value> {

    static private Logger logger
        = Logger.getLogger(RequireFunction.class.getName());

    // the resource name of the mustache template for the wrapper function
    static private final String REQUIRE_WRAPPER_RESSOURCE
        = "/graalvm/require-wrapper.mustache";

    static private Mustache requireWrapperTemplate = null;

    static private void prepareWrapperTemplate() throws IOException {
        try(final InputStream is =
            RequireFunction.class.getResourceAsStream(REQUIRE_WRAPPER_RESSOURCE)) {
            if (is == null) {
                throw new IOException(MessageFormat.format(
                    "mustache template with resource name ''{0}'' not found",
                    REQUIRE_WRAPPER_RESSOURCE
                ));
            }
            requireWrapperTemplate = new DefaultMustacheFactory().compile(
                new BufferedReader(new InputStreamReader(is)),
                "require-wrapper-template"
            );
        }
    }

    static {
        try {
            prepareWrapperTemplate();
        } catch(Throwable e) {
            final String message =  MessageFormat.format(
                "failed to load and compile mustache template resource ''{0}''",
                REQUIRE_WRAPPER_RESSOURCE
            );
            logger.log(Level.SEVERE, message, e);
        }
    }

    // the URI of the module in which this require function is invoked
    private URI contextURI = null;

    /**
     * Creates a <code>require</code> function which will be invoked in no
     * specific context.
     */
    public RequireFunction() {}

    /**
     * Creates a <code>require</code> function which will be invoked in the
     * context of the module given by <code>contextURI</code>. The context URI
     * can be null.
     *
     * @param contextURI
     */
    public RequireFunction(final URI contextURI) {
        this.contextURI = contextURI;
    }

    protected String loadModuleSource(@NotNull  URI uri) throws IOException {
        Objects.requireNonNull(uri);
        if (!uri.getScheme().toLowerCase().equals("file")) {
            throw new IllegalArgumentException(MessageFormat.format(
                "unsupported type of module URI, file URI required. Got ''{0}''",
                uri
            ));
        }
        final File moduleFile = new File(uri);
        final String moduleSource = new String(
            Files.readAllBytes(Paths.get(moduleFile.getAbsolutePath()))
        );
        return moduleSource;
    }

    protected String wrapModuleSource(
            @NotNull String moduleID,
            @NotNull String moduleURI,
            @NotNull String moduleSource) {

        // assemble parameters
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("requireFunctionClassName",
            RequireFunction.class.getName());
        parameters.put("moduleID", moduleID);
        parameters.put("moduleURI", moduleURI);
        parameters.put("moduleSource", moduleSource);
        if (contextURI != null) {
            parameters.put("contextURI", contextURI.toString());
        }

        if (requireWrapperTemplate == null) {
            throw new IllegalStateException(
                "can't evaluate mustache template, template not compiled yet"
            );
        }

        // evaluate template
        final StringWriter buffer = new StringWriter();
        requireWrapperTemplate.execute(buffer, parameters);
        return buffer.toString();
    }

    /**
     * Lookups or loads and evalutes the CommonJS module given by
     * the module ID <code>moduleId</code>-
     *
     * @param moduleID the module ID
     * @return the value the modules exports
     * @throws RequireFunctionException thrown, if something goes wrong, i.e.
     *    if the module with this module ID is not available
     */
    @Override
    public Value apply(String moduleID) {

        final CommonJSModuleCache cache = CommonJSModuleCache.getInstance();
        final Context context = Context.getCurrent();
        if (context == null) {
            final String message = MessageFormat.format(
                "No Polyglot context available. Can''t apply require function. "
              + "Context URI is ''{0}''",
                 this.contextURI == null
                    ? "undefined"
                    : this.contextURI.toString()
            );
            throw new IllegalStateException(message);
        }

        Optional<URI> resolvedModuleUri;
        if (contextURI == null) {
            resolvedModuleUri = ModuleRepositories.getInstance().resolve(moduleID);
        } else {
            resolvedModuleUri = ModuleRepositories.getInstance().resolve(
                moduleID,
                contextURI);
        }

        if (! resolvedModuleUri.isPresent()) {
            final String message = MessageFormat.format(
                "failed to resolve module with module ID ''{0}'' with "
              + "context ''{1}''",
                moduleID,
                contextURI == null ? "undefined" : contextURI.toString()
            );
            logger.log(Level.WARNING, message);
            throw new RequireFunctionException(message);
        }


        // lookup the module in the cache
        final URI moduleUri = resolvedModuleUri.get();
        final Optional<Value> cachedModule = cache.lookup(moduleUri, context);
        if (cachedModule.isPresent()) {
            return cachedModule.get();
        }

        try {
            final String moduleSource = loadModuleSource(moduleUri);
            final String wrapper = wrapModuleSource(
                moduleID,
                resolvedModuleUri.toString(),
                moduleSource);

            final Value module = context.eval(Source.create("js", wrapper));
            cache.remember(moduleUri, module, context);
            return module;
        } catch(IOException | PolyglotException e) {
            final String message = MessageFormat.format(
                "failed to require module ''{0}'''", moduleID);
            logger.log(Level.SEVERE, message, e);
            throw new RequireFunctionException(message, e);
        }
    }
}
