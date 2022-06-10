package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import org.graalvm.polyglot.io.FileSystem;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.*;

@SuppressWarnings({"RedundantThrows", "unused"})
public class ESModuleResolver implements FileSystem {
    private static final ESModuleResolver instance = new ESModuleResolver();

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

    private final java.nio.file.FileSystem fullIO = FileSystems.getDefault();
    private final List<IESModuleRepository> repos = new ArrayList<>();


    private IESModuleRepository lookupRepoForModulePath(Path path) {
        return repos.stream()
            .filter(repo -> repo.resolveModulePath(path) != null)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Path parsePath(URI uri) {
        return fullIO.provider().getPath(uri);
    }

    @Override
    public Path parsePath(String path) {
        var resolvedPath = repos.stream().map(repo -> repo.resolveModulePath(path))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        if (resolvedPath != null) {
            return resolvedPath;
        }
        return fullIO.getPath(path);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        if (lookupRepoForModulePath(path) == null) {
            fullIO.provider().checkAccess(path, modes.toArray(new AccessMode[]{}));
        }
        // since path was successfully resolved against a file in an ES Module repository,
        // checkAccess is OK
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        var repo = lookupRepoForModulePath(path);
        if (repo == null) {
            return fullIO.provider().newByteChannel(path, options, attrs);
        } else {
            return repo.newByteChannel(path);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path toAbsolutePath(Path path) {
        return path.toAbsolutePath();
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        return path.toRealPath(linkOptions);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }
}
