package org.openstreetmap.josm.plugins.scripting.graalvm;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleCache;
import org.openstreetmap.josm.plugins.scripting.graalvm.commonjs.CommonJSModuleRepositoryRegistry;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the <code>require()</code> function which is added
 * to the evaluation context when a CommonJS module is loaded and evaluated.
 */
public class RequireFunction implements Function<String, Value> {

    static private final Logger logger
        = Logger.getLogger(RequireFunction.class.getName());

    // the resource name of the mustache template for the wrapper function
    static private final String REQUIRE_WRAPPER_RESOURCE
        = "/graalvm/require-wrapper.mustache";

    static private Mustache requireWrapperTemplate = null;

    static private void prepareWrapperTemplate() throws IOException {
        try(final InputStream is =
            RequireFunction.class.getResourceAsStream(REQUIRE_WRAPPER_RESOURCE)) {
            if (is == null) {
                throw new IOException(MessageFormat.format(
                    "mustache template with resource name ''{0}'' not found",
                        REQUIRE_WRAPPER_RESOURCE
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
                    REQUIRE_WRAPPER_RESOURCE
            );
            logger.log(Level.SEVERE, message, e);
        }
    }

    // the URI of the module in which this require function is invoked
    private URI contextURI = null;

    private void logFine(Supplier<String> messageBuilder) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message);
        }
    }

    /**
     * Creates a <code>require</code> function which will be invoked in no
     * specific context.
     */
    public RequireFunction() {}

    /**
     * Replies the context URI for this <code>require()</code> instance.
     *
     * @return the context URI. null, if missing.
     */
    public URI getContextURI() {
        return contextURI;
    }

    /**
     * Creates a <code>require()</code> function which will be invoked in the
     * context of the module given by <code>contextURI</code>. The context URI
     * can be null.
     *
     * @param contextURI the context URI
     */
    public RequireFunction(final URI contextURI) {
        this.contextURI = contextURI;
    }

    private String loadModuleSourceFromFile(@NotNull URI uri)
        throws IOException {
        // pre: uri is a file URI - don't check again
        final File moduleFile = new File(uri);
        return new String(
            Files.readAllBytes(Paths.get(moduleFile.getAbsolutePath()))
        );
    }

    private String loadModuleSourceFromJarEntry(@NotNull URI uri)
        throws IOException {
        // pre: uri is a jar file URI - don't check again
        final ModuleJarURI moduleUri = new ModuleJarURI(uri);

        try (final JarFile jarFile = new JarFile(moduleUri.getJarFile())) {
            final JarEntry entry = jarFile.getJarEntry(
                moduleUri.getJarEntryName());
            if (entry == null || entry.isDirectory()) {
                // should not happen here, because we already checked before,
                // that this entry exists and is a file. Just in case, throw
                // an exception.
                throw new IllegalStateException(MessageFormat.format(
                    "unexpected CommonJS module jar URI doesn''t refer to " +
                    "a file entry in a jar file. uri=''{0}''",
                    uri.toString()
                ));
            }
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try(InputStream is = jarFile.getInputStream(entry)) {
                final byte [] data = new byte[1024];
                int numRead;
                while ((numRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, numRead);
                }
                buffer.flush();
                return buffer.toString(StandardCharsets.UTF_8);
            }
        }
    }

    private String loadModuleSource(@NotNull URI uri) throws IOException {
        Objects.requireNonNull(uri);
        final String scheme = uri.getScheme().toLowerCase();
        switch(scheme) {
            case "file":
                return loadModuleSourceFromFile(uri);
            case "jar":
                return loadModuleSourceFromJarEntry(uri);
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported type of module URI, file URI required. " +
                    "Got ''{0}''", uri
                ));
        }
    }

    private String wrapModuleSource(@NotNull String moduleID,
        @NotNull String moduleURI, @NotNull String moduleSource) {

        // assemble parameters
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("moduleID", moduleID);
        parameters.put("moduleURI", moduleURI);
        parameters.put("moduleSource", moduleSource);

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
     * Lookup, load and evaluate the CommonJS module given by
     * the module ID <code>moduleId</code>
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
              + "Context URI =''{0}''",
                 this.contextURI == null
                    ? "undefined"
                    : this.contextURI.toString()
            );
            throw new IllegalStateException(message);
        }

        Optional<URI> resolvedModuleURI;
        if (contextURI == null) {
            logFine(() -> MessageFormat.format(
                "Resolving module ID without context. module ID=''{0}''",
                moduleID
            ));
            resolvedModuleURI = CommonJSModuleRepositoryRegistry.getInstance()
                .resolve(moduleID);
        } else {
            logFine(() -> MessageFormat.format(
                "Resolving module ID with context. module ID=''{0}'', " +
                "context URI=''{1}''",
                moduleID, contextURI.toString()
            ));
            resolvedModuleURI = CommonJSModuleRepositoryRegistry.getInstance().resolve(
                moduleID,
                contextURI);
        }

        if (resolvedModuleURI.isEmpty()) {
            final String message = MessageFormat.format(
                "failed to resolve module with module ID ''{0}'' from "
              + "context ''{1}''",
                moduleID,
                contextURI == null ? "undefined" : contextURI.toString()
            );
            logger.log(Level.WARNING, message);
            throw new RequireFunctionException(message);
        }


        // lookup the module in the cache
        final URI moduleURI = resolvedModuleURI.get();
        final Optional<Value> cachedModule = cache.lookup(moduleURI, context);
        if (cachedModule.isPresent()) {
            return cachedModule.get();
        }

        try {
            final String moduleSource = loadModuleSource(moduleURI);
            final String wrapperSource = wrapModuleSource(
                moduleID,
                moduleURI.toString(),
                moduleSource);

            final Source source = Source.newBuilder(
                    "js",                                  // language
                    new StringReader(wrapperSource),       // source
                    moduleURI + "(wrapped)"                // source name
                ).build();
            final Value module = context.eval(source);
            cache.remember(moduleURI, module, context);
            return module;
        } catch(IOException | PolyglotException e) {
            final String message = MessageFormat.format(
                "failed to require module ''{0}''", moduleID);
            logger.log(Level.SEVERE, message, e);
            throw new RequireFunctionException(message, e);
        }
    }
}
