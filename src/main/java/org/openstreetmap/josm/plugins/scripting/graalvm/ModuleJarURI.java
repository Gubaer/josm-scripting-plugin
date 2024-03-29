package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * A ModuleJarURI is a jar-URI with an embedded file-URI and an optional
 * path to a root entry in the jar-file.
 * <p>
 * A ModuleJarURI refers to the root of the repository for CommonJS or ES
 * Modules.
 */
public class ModuleJarURI {
    static private final Logger logger = Logger.getLogger(ModuleJarURI.class.getName());

    /**
     * Builds a jar URI given the path of the jar file and the path
     * of the jar entry.
     *
     * @param jarFilePath the path to the jar file
     * @param jarEntryPath the path to the jar entry in the jar file.
     * @return the jar URI
     * @throws URISyntaxException thrown, if the  URI isn't a
     *  valid jar URI
     * @throws MalformedURLException thrown, if the URI can't be converted
     *  to a valid jar URL
     */
    public static URI buildJarUri(@NotNull final String jarFilePath,
                           @NotNull final RelativePath jarEntryPath)
        throws MalformedURLException, URISyntaxException {
        Objects.requireNonNull(jarFilePath);
        Objects.requireNonNull(jarEntryPath);
        final String fileUri = new File(jarFilePath).toURI().toString();
        final URI uri = new URI(format("jar:{0}!{1}", fileUri, "/" + jarEntryPath));

        // try to convert the uri to a URL. This will make sure, the URI
        // includes a valid jar entry path
        //noinspection ResultOfMethodCallIgnored
        uri.toURL();
        return uri;
    }

    /**
     * Builds a jar URI given the path of the jar file. Assumes the
     * root jar entry path <code>/</code>.
     *
     * @param jarFilePath the path to the jar file
     * @return the jar URI
     * @throws URISyntaxException thrown, if the  URI isn't a
     *  valid jar URI
     * @throws MalformedURLException thrown, if the URI can't be converted
     *  to a valid jar URL
     */
    public static URI buildJarUri(@NotNull final String jarFilePath)
            throws URISyntaxException, MalformedURLException {
        return buildJarUri(jarFilePath, RelativePath.EMPTY);
    }

    /**
     * Replies true, if <code>uri</code> is a valid URI for a Common JS
     * module in a jar file.
     *
     * @param uri the uri
     * @return true, if <code>uri</code> is valid; false otherwise
     */
    @SuppressWarnings("unused") // part of the public API
    static public boolean isValid(@NotNull final String uri) {
        try {
            return isValid(new URI(uri));
        } catch(IllegalArgumentException | URISyntaxException e) {
            return false;
        }
    }

    /**
     * Replies true, if <code>uri</code> is a valid URI for a Common JS
     * module in a jar file.
     *
     * @param uri the uri
     * @return true, if <code>uri</code> is valid; false otherwise
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    static public boolean isValid(@NotNull final URI uri) {
        try {
            new ModuleJarURI(uri);
            return true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }

    private String jarFilePath;

    /**
     * The root path of the module repository <bold>within</bold> the jar file.
     * Always a <bold>relative</bold> path, although in URL itself the path always
     * starts with a leading '/'.
     */
    private RelativePath jarEntryPath = RelativePath.EMPTY;

    private ModuleJarURI() {}

    /**
     * Creates a ModuleJarURI given a URI.
     *
     * @param uri the jar URI
     * @throws IllegalArgumentException thrown, if <code>uri</code> isn't
     *  a valid jar URI
     * @throws IllegalArgumentException thrown, if <code>uri</code> doesn't
     *  embed a file URI
     * @throws IllegalArgumentException thrown, if <code>uri</code> doesn't
     *  end with a jar entry path after <code>!</code>
     */
    public ModuleJarURI(@NotNull final URI uri) {
        Objects.requireNonNull(uri);
        if (!"jar".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(format(
                "illegal URI. Expected scheme ''jar'', got ''{0}''", uri.getScheme()
            ));
        }
        try {
            // this makes sure the URI includes a jar entry path
            //noinspection ResultOfMethodCallIgnored
            uri.toURL();
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(format(
                "illegal URI. Failed to convert to URL. uri=''{0}''", uri
            ), e);
        }
        final String jarSpecificPart = uri.getSchemeSpecificPart();

        final URI fileUri;
        try {
            fileUri = new URI(jarSpecificPart);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(format(
                "illegal URI. Failed to build URI for embedded file URI. uri=''{0}''", uri
            ), e);
        }
        if (! "file".equalsIgnoreCase(fileUri.getScheme())) {
            throw new IllegalArgumentException(format(
                "illegal URI. Expected embedded URI with scheme ''file'', got scheme ''{0}''. uri=''{1}''",
                fileUri.getScheme(), uri
            ));
        }
        final int i = fileUri.toString().lastIndexOf("!");
        if (i < 0) {
            // shouldn't happen, but just in case
            throw new IllegalArgumentException(format(
                "illegal URI. embedded file URI doesn''t include a jar entry path after ''!''. uri=''{0}''", uri
             ));
        }
        try {
            jarFilePath = new File(new URI(fileUri.toString().substring(0, i)))
                    .toString();
        } catch(URISyntaxException | IllegalArgumentException e) {
            throw new IllegalArgumentException(format(
                "failed to rebuild file URI embedded in jar URI. jar URI=''{0}''", uri
            ), e);
        }

        final var path = fileUri.toString().substring(i+1);
        if (path.isBlank() || path.equals("/")) {
            jarEntryPath = RelativePath.EMPTY;
        } else {
            jarEntryPath = RelativePath.parse(path.substring(1));
        }
    }

    /**
     * Replies the path to the jar file
     *
     * @return the path to the jar file
     */
    public @NotNull String getJarFilePath() {
        return jarFilePath;
    }

    /**
     * Replies the file object representing the jar file this URI
     * refers to
     *
     * @return the jar file
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public @NotNull File getJarFile() {
        return new File(jarFilePath);
    }

    /**
     * Replies the path of the jar entry, i.e. <code>foo/bar/mymodule.js</code>
     * this URI refers to
     *
     * @return the path of the jar entry
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public RelativePath getJarEntryPath() {
        return jarEntryPath;
    }

    /**
     * Replies the name of the jar entry to which this URI refers.
     * <p>
     * The name is equal to the jar entry path ({@link #getJarEntryPath()},
     * without a leading '/'. It is used to look up the jar entry, see
     * {@link JarFile#getEntry(String)} or {@link JarFile#getJarEntry(String)}.
     *
     * @return the jar entry name
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public String getJarEntryName() {
        return jarEntryPath.toString();
    }

    /**
     * Replies true, if this URI refers to a readable local file.
     *
     * @return true, if this URI refers to a readable local file;
     *  false otherwise
     */
    public boolean refersToReadableFile() {
        final File f = getJarFile();
        return f.exists() && f.isFile() && f.canRead();
    }

    /**
     * Replies true, if this URI refers to a directory in the jar file,
     * not a file.
     *
     * @return true, if this URI refers to a directory in the jar file; false,
     * otherwise
     */
    public boolean refersToDirectoryJarEntry() {
        try(final JarFile jar = new JarFile(getJarFile())) {
            final JarEntry entry =
                jar.getJarEntry(this.getJarEntryName());
            if (entry == null) {
                return false;
            }
            return entry.isDirectory();
        } catch(IOException e) {
            logger.log(Level.WARNING, format(
                "failed to open and read jar file. jar file=''{0}''", getJarFile()
            ),e);
            return false;
        }
    }

    /**
     * Replies true, if this module URI is the base for the <code>other</code>
     * module URI, i.e.
     * <ul>
     *     <li>if they refer to the same jar file</li>
     *     <li>if this jar entry path is a prefix of the other jar entry path
     *     </li>
     * </ul>
     * @param other the child URI
     * @return true, if <code>other</code> is a child of this URI; false,
     * otherwise
     */
    public boolean isBaseOf(@NotNull final ModuleJarURI other) {
        Objects.requireNonNull(other);
        return jarFilePath.equals(other.jarFilePath)
            && (
                this.getJarEntryPath().isEmpty()
                || other.getJarEntryPath().startsWith(this.getJarEntryPath())
            );
    }

    /**
     * Replies true, if this URI refers to a jar file.
     *
     * @return true, if this URI refers to a jar file;
     *  false otherwise
     */
    public boolean refersToJarFile() {
        try(final JarFile ignored = new JarFile(jarFilePath)) {
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * Replies a normalized version of this URI.
     * <ul>
     *     <li>the file path is converted to a canonical, normalized path</li>
     *     <li>the jar entry path is converted to an absolute,
     *     normalized path</li>
     * </ul>
     * @return the normalized CommonJS module jar URI
     */
    public @NotNull Optional<ModuleJarURI> normalized() {
        try {
            final String normalizedFilePath = Path.of(jarFilePath)
                    .normalize().toFile().getCanonicalPath();
            // don't apply toAbsolutePath(). The jar entry path is already
            // absolute (starts with an /); and it doesn't refer to a file
            // in the local file system, but to a jar entry in the jar file
            final var canonicalPath = jarEntryPath.canonical();
            if (canonicalPath.isEmpty()) {
                return Optional.empty();
            }

            final ModuleJarURI normalizedURI = new ModuleJarURI();
            normalizedURI.jarEntryPath = canonicalPath.get();
            normalizedURI.jarFilePath = normalizedFilePath;
            return Optional.of(normalizedURI);
        } catch(IOException e) {
            logger.log(Level.WARNING, format(
                "Failed to canonicalize path ''{0}''", jarFilePath
            ),e);
            return Optional.empty();
        }
    }

    /**
     * Replies this object as {@link URI}
     *
     * @return the URI
     */
    public @NotNull URI toURI() {
        try {
            return buildJarUri(jarFilePath, jarEntryPath);
        } catch(URISyntaxException | MalformedURLException e) {
            // shouldn't happen, but just in case
            throw new IllegalStateException(format(
                "failed to build URI from jar file path and jar entry path. " +
                "jar file path=''{0}'', jar entry path=''{1}''",
                 jarFilePath, jarEntryPath
            ),e);
        }
    }

    @Override
    public String toString() {
        return toURI().toString();
    }

    /**
     * Replies the CommonJS module jar URI which is a suitable
     * resolution context URI for resolving CommonJS module IDs.
     * <p>
     * If the jar entry path of this URI is empty, this URI is already a
     * suitable resolution context.
     * <p>
     * If the jar entry path of this URI refers to a directory in the jar file, it is
     * already a suitable resolution context.
     * <p>
     * If, however, it refers to a file entry in the jar file, then the
     * parent directory entry is used as resolution context.
     *
     * @return the resolution context URI
     * @throws IOException thrown, if the jar file can't be accessed/
     * opened/read
     */
    public @NotNull ModuleJarURI toResolutionContextUri()
            throws IOException {

        RelativePath normalizedJarEntryPath = jarEntryPath.canonical()
                .orElse(RelativePath.EMPTY);

        if (normalizedJarEntryPath.isEmpty()) {
            final ModuleJarURI resolutionContextUri = new ModuleJarURI();
            resolutionContextUri.jarFilePath = this.jarFilePath;
            resolutionContextUri.jarEntryPath = RelativePath.EMPTY;
            return resolutionContextUri;
        }

        // if this URI refers to a jar entry of type file, then use
        // its parent dir as context path
        try(final JarFile jar = new JarFile(getJarFile())) {
            final JarEntry entry = jar.getJarEntry(normalizedJarEntryPath.toString());
            if (entry != null) {
                if (!entry.isDirectory() && normalizedJarEntryPath.getParent().isPresent()) {
                    normalizedJarEntryPath = normalizedJarEntryPath.getParent().get();
                }
            }
        }
        final ModuleJarURI resolutionContextUri = new ModuleJarURI();
        resolutionContextUri.jarFilePath = this.jarFilePath;
        resolutionContextUri.jarEntryPath = normalizedJarEntryPath;
        return resolutionContextUri;
    }
}
