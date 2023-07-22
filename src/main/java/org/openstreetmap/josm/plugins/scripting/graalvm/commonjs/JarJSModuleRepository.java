package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleID;
import org.openstreetmap.josm.plugins.scripting.graalvm.ModuleJarURI;
import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

/**
 * A collection of common JS modules packaged into a jar file.
 */
public class JarJSModuleRepository extends BaseJSModuleRepository {
    @Override
    protected ContextType getContextType(RelativePath context) throws IOException {
        if (context.isEmpty()) {
            return ContextType.DIRECTORY_CONTEXT;
        }
        final JarEntry entry = lookupJarEntry(jarUri.getJarFile(), context);
        if (entry.isDirectory()) {
            return ContextType.DIRECTORY_CONTEXT;
        } else {
            return ContextType.FILE_CONTEXT;
        }
    }

    private void ensureReadableJarFile() throws IOException {
        if (! (jarUri.refersToReadableFile() && jarUri.refersToJarFile())) {
            throw new IOException(format(
                "jar file doesn''t exist, isn''t a jar file, or can''t be read. file=''{0}''", jarUri.getJarFilePath()
            ));
        }
    }

    static private JarEntry lookupJarEntry(final File jar, RelativePath entryKey) throws IOException {
        try(final JarFile jarFile = new JarFile(jar)) {
            final JarEntry entry = jarFile.getJarEntry(entryKey.toString());
            if (entry == null) {
                throw new IOException(format(
                    "jar entry in jar file doesn''t exist. file=''{0}'', entry key=''{1}''", jar, entryKey
                ));
            }
            return entry;
        } catch(SecurityException e) {
            throw new IOException(format(
                "jar entry in jar file doesn''t exist. file=''{0}'', entry key=''{1}''", jar, entryKey
            ), e);
        }
    }

    static private void ensureJarEntryIsDirectory(final File jar,
                                                  final RelativePath entryName)
        throws IOException {
        final JarEntry entry = lookupJarEntry(jar, entryName);
        if (!entry.isDirectory()) {
            throw new IOException(format(
                "jar entry name doesn''t refer to a directory entry in the jar file. file=''{0}'', entry name=''{1}''",
                jar, entryName
            ));
        }
    }

    static private void ensureJarEntryIsFile(final File jar,
                                             final RelativePath entryName)
            throws IOException {
        final JarEntry entry = lookupJarEntry(jar, entryName);
        if (entry.isDirectory()) {
            throw new IOException(format(
                "jar entry name doesn''t refer to a file entry in the jar file. file=''{0}'', entry name=''{1}''",
                jar, entryName
            ));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isRepoFile(@NotNull RelativePath repoPath) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "repoPath=''{0}''", repoPath);
        }
        if (repoPath == null || repoPath.isEmpty()) {
            return false;
        }
        if (repoPath.isEmpty()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "unexpected empty jar entry name ''{0}''", repoPath);
            }
            return false;
        }
        try {
            ensureJarEntryIsFile(jarUri.getJarFile(), repoPath);
        } catch(IOException e) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "jar entry doesn''t exist or isn''t a file entry. jar file=''{0}'', entry name=''{1}''",
                    jarUri.getJarFilePath(), repoPath
               ), e);
            }
            return false;
        }
        return true;
    }

    private final ModuleJarURI jarUri;

    /**
     * Creates the repository for a given jar file.
     *
     * @param jar the jar file
     * @throws IOException thrown, if <code>jar</code> isn't an existing and
     * readable jar file
     */
    public JarJSModuleRepository(@NotNull final File jar) throws IOException {
        Objects.requireNonNull(jar);
        final URI jarUri;
        try {
            jarUri = ModuleJarURI.buildJarUri(jar.toString());
        } catch(MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(format(
                "failed to build jar URI given file. jar file path=''{0}''", jar.toString()
            ),e);
        }
        this.jarUri = new ModuleJarURI(jarUri);
        ensureReadableJarFile();
    }

    /**
     * Creates the repository for a given jar file and a path in the
     * jar file
     *
     * @param jar the jar file
     * @param rootPath the path in the jar file
     * @throws IOException if <code>jar</code> isn't an existing and
     *  readable jar file
     * @throws IOException if there is no directory <code>rootPath</code>
     *  in the jar file <code>jar</code>
     * @throws NullPointerException if one of the parameters is null
     */
    public JarJSModuleRepository(@NotNull final File jar, @NotNull final RelativePath rootPath) throws IOException {
        Objects.requireNonNull(jar);
        Objects.requireNonNull(rootPath);
        final URI uri;
        try {
            uri = ModuleJarURI.buildJarUri(jar.toString(), rootPath);
        } catch(MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(format(
                "failed to build jar URI. jar file path=''{0}'', jar entry path=''{1}''", jar.toString(), rootPath
            ));
        }
        jarUri = new ModuleJarURI(uri);
        ensureReadableJarFile();
        if (!jarUri.getJarEntryName().isEmpty()) {
            ensureJarEntryIsDirectory(
                jarUri.getJarFile(), jarUri.getJarEntryPath());
        }
    }

    /**
     * Creates the repository for a given jar URI.
     *
     * @param uri the jar URI
     * @throws IllegalArgumentException if <code>uri</code> isn't a
     * jar URI
     * @throws IllegalArgumentException if <code>uri</code> isn't a
     *  <em>valid</em> CommonJS module URI
     * @throws IOException if <code>uri</code> doesn't refer to
     *  an existing and readable jar file
     * @throws IOException if <code>uri</code> doesn't refer to an
     *  existing and readable directory entry in the jar file
     * @throws NullPointerException if <code>uri</code> is null
     */
    public JarJSModuleRepository(@NotNull final URI uri) throws IOException {
        Objects.requireNonNull(uri);
        // throws IllegalArgumentException, if uri isn't valid
        jarUri = new ModuleJarURI(uri);
        ensureReadableJarFile();
        if (!jarUri.getJarEntryName().isEmpty()) {
            ensureJarEntryIsDirectory(jarUri.getJarFile(), jarUri.getJarEntryPath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getBaseURI() {
        return jarUri.toURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBaseOf(@NotNull final URI moduleUri) {
        Objects.requireNonNull(moduleUri);
        final ModuleJarURI other;
        try {
            // throws IllegalArgumentException, if moduleUri isn't valid
            other = new ModuleJarURI(moduleUri);
        } catch(IllegalArgumentException e) {
            getLogger().log(Level.WARNING, format(
                "moduleUri isn''t a valid jar URI for a CommonJS module. moduleUri=''{0}''", moduleUri.toString()),
            e);
            return false;
        }
        getLogger().log(Level.FINE, "moduleUri=''{0}'': before jarUri.isBaseOf", moduleUri);
        return this.jarUri.isBaseOf(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull final String moduleId) {
        Objects.requireNonNull(moduleId);
        final RelativePath modulePath;
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().log(Level.FINE, "*** Starting to resolve module id *** moduleId = ''{0}''", moduleId);
        }
        try {
            modulePath = RelativePath.parse(moduleId);
        } catch(IllegalArgumentException e) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "can''t resolve invalid module id. id=''{0}''", moduleId
                ), e);
            }
            return Optional.empty();
        }
        return resolveInternal(
            new ModuleID(modulePath).normalized(),
            jarUri.toURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<URI> resolve(@NotNull final String moduleId,
                                 @NotNull final URI contextUri) {
        Objects.requireNonNull(moduleId);
        Objects.requireNonNull(contextUri);
        final RelativePath modulePath;
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().log(Level.FINE, format(
                "*** Starting to resolve module id *** moduleId = ''{0}'', contextUri = ''{1}''", moduleId, contextUri
            ));
        }

        try {
            modulePath = RelativePath.parse(moduleId);
        } catch (IllegalArgumentException e) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format("can''t resolve invalid module id. id=''{0}''", moduleId), e);
            }
            return Optional.empty();
        }
        ModuleJarURI contextModuleUri;
        try {
            contextModuleUri = new ModuleJarURI(contextUri);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, format(
                "failed to resolve module id, context URI is invalid. id=''{0}'', contextUri=''{1}''",
                moduleId, contextUri
            ));
            return Optional.empty();
        }
        try {
            var normalizedUri = contextModuleUri.normalized();
            if (normalizedUri.isEmpty()) {
                return Optional.empty();
            }
            contextModuleUri = normalizedUri.get().toResolutionContextUri();
        } catch(IOException e) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "failed to derive resolution context URI. contextUri=''{0}''", contextUri
                ),e);
            }
            return Optional.empty();
        }

        if (!jarUri.isBaseOf(contextModuleUri)) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "failed to resolve module id, normalized context URI isn''t  child of base URI. id=''{0}'', " +
                    "contextUri=''{1}'', contextModuleUri=''{2}'', baseUri=''{3}''",
                    moduleId, contextUri, contextModuleUri, getBaseURI()
                ));
            }
            return Optional.empty();
        }
        return resolveInternal(
            new ModuleID(modulePath).normalized(),
            contextModuleUri.toURI());
    }

    private Optional<URI> resolveInternal(final ModuleID id,
                                          final URI contextUri) {
        //pre: id not null and valid
        //pre: contextUri is not null and a valid CommonJS module jar URI
        //pre: this base URI is a base of the context URI

        ModuleJarURI contextJSModuleUri = new ModuleJarURI(contextUri);
        var normalizedUri = contextJSModuleUri.normalized();
        if (normalizedUri.isEmpty()) {
            return Optional.empty();
        }
        contextJSModuleUri = normalizedUri.get();
        final Optional<RelativePath> resolvedModulePath = resolve(id, contextJSModuleUri.getJarEntryPath());
        if (resolvedModulePath.isEmpty()) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, "failed to resolve module. moduleId=''{0}''", id);
            }
            return Optional.empty();
        }

        final URI resolvedModuleUri;
        try {
            resolvedModuleUri = ModuleJarURI.buildJarUri(
                contextJSModuleUri.getJarFilePath(),
                resolvedModulePath.get());
        } catch(final URISyntaxException | IOException e) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                     "failed to build resolved module URI. jar file path=''{0}'', resolved module path=''{1}''",
                    contextJSModuleUri.getJarFilePath(), resolvedModulePath.get()
                ));
            }
            return Optional.empty();
        }

        // make sure the resolved module URI is still a child URI of the
        // base URI of this repo
        if (!isBaseOf(resolvedModuleUri)) {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, format(
                    "failed to resolve module. moduleId=''{0}''. resolved module URI isn''t a child of the base URI. " +
                    "module id=''{0}'', base URI=''{1}'', resolved URI=''{2}''",
                    id, jarUri.toURI(), resolvedModuleUri
                ));
            }
            return Optional.empty();
        }
        return Optional.of(resolvedModuleUri);
    }
}
