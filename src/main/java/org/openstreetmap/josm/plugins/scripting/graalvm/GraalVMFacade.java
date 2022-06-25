package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.*;
import org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.ESModuleResolver;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.plugins.scripting.ui.preferences.graalvm.GraalVMPrivilegesModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GraalVMFacade  implements IGraalVMFacade {
    static private final Logger logger = Logger.getLogger(GraalVMFacade.class.getName());

    static final private Map<String, TypeResolveFunction> pluginObject =
        Collections.singletonMap("type", new TypeResolveFunction());

    private Context context;

    /**
     * Initializes a GraalVM context with the standard bindings.
     *
     * Can be used in test cases to properly initialize a
     * GraalVM context for testing.
     *
     * @param context the context
     */
    static public void populateContext(@NotNull final Context context) {
        Objects.requireNonNull(context);
        final Value bindings = context.getBindings("js");

        // populate the context with the require function
        bindings.putMember("require", new RequireFunction());
        // populate the context with the object 'Plugin'. 'Plugin' offers
        // a member function 'type()' to access java classes from the plugin
        // which can't be accessed with 'Java.type()' because of class
        // loading issues.
        bindings.putMember("Plugin", pluginObject);
    }

    private void grantPrivilegesToContext(final Context.Builder builder) {
        // NOTE: allowAllAccess has to be true. If false, the require()
        // function can't be invoked from JavaScript scripts.
        builder
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(className -> true)
            // required to load ES Modules
            .allowIO(true);

        GraalVMPrivilegesModel.getInstance().prepareContextBuilder(builder);
    }

    private void setOptionsOnContext(final Context.Builder builder) {
        // Enable strict mode for all scripts.
        builder.option("js.strict", "true");

        // TODO(Gubaer): disable later. Currently js.java-package-globals is
        // an experimental option and experimental options should not be
        // used in production environments.
        // ---
        // Don't support the global variable 'Packages'. Don't allow java
        // classes with the backward-compatible notation
        //   const FileClass = java.io.File
        // See
        // https://www.graalvm.org/22.1/reference-manual/js/JavaInteroperability/#class-access
        // builder.option("js.java-package-globals", "false");
    }

    /**
     *
     * @throws IllegalStateException thrown, if no language and polyglot
     *  implementation was found on the classpath
     */
    private void initContext() throws IllegalStateException{
        // currently GraalVM is only used for JavaScript
        final Context.Builder builder = Context.newBuilder("js");
        grantPrivilegesToContext(builder);
        setOptionsOnContext(builder);
        builder.fileSystem(ESModuleResolver.getInstance());
        context = builder.build();
        populateContext(context);
        context.enter();
    }

    public GraalVMFacade() {
        //TODO(gubaer): don't initialize a default context anymore
        //initContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetContext() {
        if (context != null) {
            context.leave();
            context.close(true /* cancelIfExecuting */);
        }
        initContext();
    }

    private ScriptEngineDescriptor buildDescriptorForGraalVMBasedEngine(
            final Engine engine,
            final Language info) {

        final ScriptEngineDescriptor desc = new ScriptEngineDescriptor(
            ScriptEngineDescriptor.ScriptEngineType.GRAALVM,
            info.getId(),                    // engineId
            engine.getImplementationName(),  // engineName
            info.getName(),                  // languageName
            info.getDefaultMimeType(),       // contentType
            engine.getVersion(),             // engineVersion
            info.getVersion()                // languageVersion
        );
        desc.setContentMimeTypes(info.getMimeTypes());
        return desc;
    }

    /**
     * Replies descriptors for languages supported by the GraalVM engine
     *
     * @param engine the engine
     * @return descriptors for GraalVM based engines
     */
    public @NotNull List<ScriptEngineDescriptor> buildDescriptorsForGraalVMBasedEngines(
        @NotNull final Engine engine) {
        return engine.getLanguages().values().stream().map(value ->
            buildDescriptorForGraalVMBasedEngine(engine, value)
        ).collect(Collectors.toList());
    }

    /**
     * @inheritDoc
     */
    public @NotNull List<ScriptEngineDescriptor> getScriptEngineDescriptors() {
        var engine = Engine.newBuilder("js").build();
        return buildDescriptorsForGraalVMBasedEngines(engine);
    }

    private void ensureEngineIdPresent(String engineId) {
        if (engineId.trim().isEmpty()) {
            throw new IllegalArgumentException(tr(
                "script engine descriptor doesn''t provide an engine id "
              + "name, got {0}", engineId));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final String script)
                     throws GraalVMEvalException {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(script);
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);

        try {
            final var source = Source.newBuilder(engineId, script, null)
                .mimeType("application/javascript+module")
                .build();
            return context.eval(source);
        } catch(IOException e) {
            // shouldn't happen because we don't load the script from a file,
            // but just in case
            final var message = tr("Failed to create ECMAScript source object");
            logger.log(Level.SEVERE, message, e);
            throw new GraalVMEvalException(message, e);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("failed to eval script"), script
            );
            logger.log(Level.INFO, e.getMessage(), e);
            throw new GraalVMEvalException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object eval(@NotNull final ScriptEngineDescriptor desc,
                     @NotNull final File script)
                    throws IOException, GraalVMEvalException {
        final String engineId = desc.getEngineId();
        ensureEngineIdPresent(engineId);
        Source source = Source.newBuilder(engineId, script)
            .mimeType("application/javascript+module")
            .build();
        try {
            return context.eval(source);
        } catch(PolyglotException e) {
            final String message = MessageFormat.format(
                tr("failed to eval script in file {0}"), script
            );
            throw new GraalVMEvalException(message, e);
        }
    }

    /* --------------------------------------------------------------------------- */
    /* context management                                                          */
    /* --------------------------------------------------------------------------- */

    // the available default contexts for GraalVM engines
    private final Map<ScriptEngineDescriptor, IGraalVMContext> defaultContexts = new HashMap<>();
    // the available custom contexts for GraalVM engines
    private final Map<ScriptEngineDescriptor, List<IGraalVMContext>> contexts = new HashMap<>();


    private void ensureValidDisplayName(String displayName) {
        Objects.requireNonNull(displayName);
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
    }
    private void ensureValidEngine(final ScriptEngineDescriptor engine) {
        Objects.requireNonNull(engine);
        if (!engine.getEngineType().equals(ScriptEngineDescriptor.ScriptEngineType.GRAALVM)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal script engine type, expected ''{0}'', got ''{1}''",
                ScriptEngineDescriptor.ScriptEngineType.GRAALVM,
                engine.getEngineType()
            ));
        }
        if (!"js".equals(engine.getEngineId())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Illegal engine id. Currently only the engine with id ''js' is supported, "
                + "got id ''{1}''",
                engine.getEngineId()
            ));
        }
    }

    private void rememberContext(ScriptEngineDescriptor engine, IGraalVMContext context) {
        synchronized (contexts) {
            var l = contexts.getOrDefault(engine, new ArrayList<>());
            l.add(context);
            contexts.put(engine, l);
        }
    }

    private Context createAndInitContext() {
        final Context.Builder builder = Context.newBuilder("js");
        grantPrivilegesToContext(builder);
        setOptionsOnContext(builder);
        builder.fileSystem(ESModuleResolver.getInstance());
        final var polyglotContext = builder.build();
        populateContext(polyglotContext);
        return polyglotContext;
    }

    /**
     * Creates the default context for the engine <code>engine</code>. If the default
     * context already exists, replies the existing default context.
     *
     * @param engine the engine
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     */
    public IGraalVMContext getOrCreateDefaultContext(@NotNull final ScriptEngineDescriptor engine) {
        ensureValidEngine(engine);
        var context = defaultContexts.get(engine);
        if (context != null) {
            logger.fine("Returning existing default context ...");
            return context;
        }
        logger.fine("Creating new default context ...");
        context = new GraalVMContext(
            tr("Default"),
            engine,
            createAndInitContext(),
            true
        );
        defaultContexts.put(engine, context);
        return context;
    }

    /**
     * Replies true if the default context for the engine <code>engine</code> exists.
     *
     * @param engine the engine
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     *
     * @return true if the default context exists; false, otherwise
     */
    public boolean existsDefaultContext(@NotNull final ScriptEngineDescriptor engine) {
        ensureValidEngine(engine);
        return defaultContexts.get(engine) != null;
    }

    /**
     * Creates a new context hosted by the engine <code>engine</code>.
     *
     * @param displayName the display name for the context
     * @param engine the engine
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws NullPointerException - if <code>displayName</code> is null
     * @throws IllegalArgumentException  - if <code>displayName</code> is blank
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     * @return the new context
     */
    public @NotNull IGraalVMContext createContext(@NotNull final String displayName, @NotNull final ScriptEngineDescriptor engine) {
        ensureValidDisplayName(displayName);
        ensureValidEngine(engine);
        final var context = new GraalVMContext(
            displayName,
            engine,
            createAndInitContext(),
            true
        );
        rememberContext(engine, context);
        return context;
    }

    /**
     * Replies the context with id <code>id</code> hosted by the engine <code>engine</code>.
     *
     * @param id the id
     * @param engine the engine
     * @return the context or null, if no such context exists
     * @throws NullPointerException - if <code>engine</code> is null
     * @throws NullPointerException - if <code>is</code> is null
     * @throws IllegalArgumentException - if <code>engine</code> doesn't describe a GraalVM
     *   engine or if the described engine is not available
     */
    public @Null IGraalVMContext lookupContext(@NotNull final String id, @NotNull final ScriptEngineDescriptor engine) {
        Objects.requireNonNull(id);
        ensureValidEngine(engine);

        return contexts.getOrDefault(engine, List.of()).stream()
            .filter(context -> context.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Closes a context and removes it.
     *
     * @param context the context
     * @throws NullPointerException - if <code>context</code> is null
     */
    public void closeAndRemoveContext(@NotNull final IGraalVMContext context) {
        Objects.requireNonNull(context);
        try {
            context.getPolyglotContext().close(true);
        } catch(IllegalStateException e) {
            logger.log(Level.WARNING, MessageFormat.format(
                 "Failed to close GraalVM context with id ''{0}''",
                 context.getId()
            ),e);
        }
        // discard the default context if it is the closed context
        var defaultContext = defaultContexts.get(context.getScriptEngine());
        if (defaultContext != null && defaultContext.getId().equals(context.getId())) {
            defaultContexts.remove(context.getScriptEngine());
        }
        // remove the closed context from the list of contexts hosted by this
        // engine
        var contextList = contexts.getOrDefault(context.getScriptEngine(), new ArrayList<>());
        contextList.remove(context);
        contexts.put(context.getScriptEngine(), contextList);
    }
}
