package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.graalvm.polyglot.io.FileSystem;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.AbstractESModuleRepository.startsWithESModuleRepoPathPrefix;

/**
 * ESModuleResolver resolves ES Module names and creates a {@link SeekableByteChannel channel} from which
 * GraalJS reads the content of the module with the resolved name.
 *
 * It can resolve ES Module names in three kind of repositories:
 * <ul>
 *     <li>in repositories of type {@link FileSystemESModuleRepository}</li>
 *     <li>in repositories of type {@link JarESModuleRepository}</li>
 *     <li>in the {@link FileSystems#getDefault() default file system}</li>
 * </ul>
 *
 * ESModuleResolver is a {@link FileSystem}. If it is set for {@link org.graalvm.polyglot.Context} then
 * <code>import</code> statements for ES Modules in this context are resolved against the ESModuleResolver.
 * <pre>
 *    var context = Context.newBuilder("js")
 *        .allowIO(true)
 *        .fileSystem(ESModuleResolver.getInstance())
 *        .build();
 * </pre>
 */
@SuppressWarnings({"RedundantThrows", "unused"})
public class ESModuleResolver implements FileSystem {
    static private final Logger logger = Logger.getLogger(ESModuleResolver.class.getName());

    private static final ESModuleResolver instance = new ESModuleResolver();

    static private void logFine(Supplier<String> supplier) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(supplier.get());
        }
    }

    /**
     * The singleton instance of the resolver
     *
     * @return the singleton instance of the resolver
     */
    static public @NotNull ESModuleResolver getInstance() {
        return instance;
    }

    /**
     * Adds a repo where the resolver looks for ES Modules.
     *
     * @param repo the repo
     * @throws NullPointerException thrown if <code>repo</code> is null
     */
    public void addRepository(@NotNull final IESModuleRepository repo) {
        Objects.requireNonNull(repo);
        repos.add(repo);
    }

    /**
     * Remove a repo from the list of repos where the resolver looks for ES Modules.
     *
     * @param repo the repo. Ignore if null.
     */
    public void removeRepository(final IESModuleRepository repo) {
        if (repo == null) {
            return;
        }
        for (int i=0; i < repos.size(); i++) {
            if (repo.getUniquePathPrefix().toString().equals(repos.get(i).getUniquePathPrefix().toString())) {
                repos.remove(i);
                return;
            }
        }
    }

    public void setRepositories(final List<IESModuleRepository> repos) {
        if (repos == null || repos.isEmpty()) {
            this.repos.clear();
            return;
        }
        this.repos.addAll(repos.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    private final java.nio.file.FileSystem fullIO = FileSystems.getDefault();
    private final List<IESModuleRepository> repos = new ArrayList<>();

    private IESModuleRepository lookupRepoForModulePath(Path path) {
        return repos.stream()
            .filter(repo -> repo.matchesWithUniquePathPrefix(path))
            .findFirst()
            .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path parsePath(URI uri) {
        logFine(() -> MessageFormat.format("parsePath: uri=''{0}''", uri));
        return fullIO.provider().getPath(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path parsePath(String path) {
        logFine(() -> MessageFormat.format("parsePath: path=''{0}'", path ));
        var p = Path.of(path);
        if (p.isAbsolute() && ! startsWithESModuleRepoPathPrefix(p)) {
            return fullIO.getPath(path);
        }
        var resolvedPath = repos.stream().map(repo -> repo.resolveModulePath(path))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        return Objects.requireNonNullElseGet(resolvedPath, () -> fullIO.getPath(path));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        logFine(() -> MessageFormat.format("checkAccess: path=''{0}'", path ));
        if (!startsWithESModuleRepoPathPrefix(path)) {
            fullIO.provider().checkAccess(path, modes.toArray(new AccessMode[]{}));
        }
        // since path was successfully resolved against a file in an ES Module repository,
        // checkAccess is OK
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        if (startsWithESModuleRepoPathPrefix(dir)) {
            throw new UnsupportedOperationException();
        }
        fullIO.provider().createDirectory(dir, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Path path) throws IOException {
        if (startsWithESModuleRepoPathPrefix(path)) {
            throw new UnsupportedOperationException();
        }
        fullIO.provider().delete(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        logFine(() -> MessageFormat.format("newByteChannel: path=''{0}'", path ));
        var repo = lookupRepoForModulePath(path);
        if (repo == null) {
            return fullIO.provider().newByteChannel(path, options, attrs);
        } else {
            logFine(() -> MessageFormat.format("newByteChannel: creating byteChannel, path:=''{0}''", path ));
            return repo.newByteChannel(path);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        if (startsWithESModuleRepoPathPrefix(dir)) {
            throw new UnsupportedOperationException();
        }
        return fullIO.provider().newDirectoryStream(dir, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toAbsolutePath(Path path) {
        logFine(() -> MessageFormat.format("toAbsolutePath: path=''{0}'", path ));
        return path.toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        logFine(() -> MessageFormat.format("toRealPath: path=''{0}'", path ));
        if (startsWithESModuleRepoPathPrefix(path)) {
            return path;
        }
        return path.toRealPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        logFine(() -> MessageFormat.format("readAttributes: path=''{0}'", path ));
        if (startsWithESModuleRepoPathPrefix(path)) {
            throw new UnsupportedOperationException();
        }
        return fullIO.provider().readAttributes(path, attributes, options);
    }
}
