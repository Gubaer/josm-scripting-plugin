package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ModuleJarURI is a jar-URI with an embedded file-URI and an optional
 * path to a root entry in the jar-file.
 * <p>
 * A ModuleJarURI refers to the root of the repository for CommonJS or ES
 * Modules.
 */
public class ModuleJarURI {
    static private final Logger logger =
        Logger.getLogger(ModuleJarURI.class.getName());

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
                           @NotNull final String jarEntryPath)
        throws MalformedURLException, URISyntaxException {
        Objects.requireNonNull(jarFilePath);
        Objects.requireNonNull(jarEntryPath);
        final String fileUri = new File(jarFilePath).toURI().toString();
        final URI uri = new URI(MessageFormat.format(
            "jar:{0}!{1}", fileUri, jarEntryPath.startsWith("/") ? jarEntryPath : "/" + jarEntryPath
        ));

        // try to convert the uri to an URL. This will make sure, the URI
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
        return buildJarUri(jarFilePath, "/");
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
    private String jarEntryPath = "";

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
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. Expected scheme ''jar'', got ''{0}''",
                uri.getScheme()
            ));
        }
        try {
            // this makes sure the URI includes a jar entry path
            //noinspection ResultOfMethodCallIgnored
            uri.toURL();
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. Failed to convert to URL. uri=''{0}''",
                uri.toString()
            ), e);
        }
        final String jarSpecificPart = uri.getSchemeSpecificPart();

        final URI fileUri;
        try {
            fileUri = new URI(jarSpecificPart);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. Failed to build URI for embedded file URI. " +
                "uri=''{0}''",
                uri.toString()
            ), e);
        }
        if (! "file".equalsIgnoreCase(fileUri.getScheme())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. Expected embedded URI with scheme ''file'', " +
                "got scheme ''{0}''. uri=''{1}''",
                fileUri.getScheme(), uri.toString()
            ));
        }
        final int i = fileUri.toString().lastIndexOf("!");
        if (i < 0) {
            // shouldn't happen, but just in case
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. embedded file URI doesn''t include a jar entry " +
                "path after ''!''. uri=''{0}''",
                uri.toString()
             ));
        }
        try {
            jarFilePath = new File(new URI(fileUri.toString().substring(0, i)))
                    .toString();
        } catch(URISyntaxException | IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                "failed to rebuild file URI embedded in jar URI. " +
                "jar URI=''{0}''", uri.toString()
            ), e);
        }
        jarEntryPath = fileUri.toString().substring(i+1)
            // always use / as delimiter, also on windows platform
            .replace("\\", "/");
        if (jarEntryPath.startsWith("/")) {
            jarEntryPath = jarEntryPath.substring(1);
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
    public Path getJarEntryPath() {
        return Path.of(jarEntryPath);
    }

    /**
     * Replies the name of the jar entry to which this URI refers.
     * <p>
     * The name is equal to the jar entry path ({@link #getJarEntryPath()},
     * without a leading '/'. It is used to lookup the jar entry, see
     * {@link JarFile#getEntry(String)} or {@link JarFile#getJarEntry(String)}.
     *
     * @return the jar entry name
     */
    @SuppressWarnings("WeakerAccess") // part of the public API
    public String getJarEntryName() {
        final String path = getJarEntryPath().toString().replace("\\", "/");
        if (path.startsWith("/")) {
            throw new IllegalStateException(MessageFormat.format(
                "jar entry path must not start with ''/''. " +
                "jar entry path=''{0}''",
                path
            ));
        }
        return path;
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
            logger.log(Level.WARNING, MessageFormat.format(
                "failed to open and read jar file. " +
                "jar file=''{0}''",
                getJarFile().toString()
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
        logger.log(Level.FINE, MessageFormat.format("this=''{0}'', other=''{1}''", this, other)); //TODO(Gubaer): remove after debugging
        return jarFilePath.equals(other.jarFilePath)
            && (
                this.getJarEntryPath().toString().equals("") // this is the empty (or root) path
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
            final String normalizedJarEntryPath = Path.of(jarEntryPath).normalize()
                    .toString();

            final ModuleJarURI normalizedURI = new ModuleJarURI();
            normalizedURI.jarEntryPath = normalizedJarEntryPath
                    // always use '/' as delimiter, also on windows platform
                    .replace("\\", "/");
            normalizedURI.jarFilePath = normalizedFilePath;
            return Optional.of(normalizedURI);
        } catch(IOException e) {
            logger.log(Level.WARNING, MessageFormat.format(
            "Failed to canonicalize path ''{0}''", Path.of(jarFilePath).normalize()
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
            throw new IllegalStateException(MessageFormat.format(
                "failed to build URI from jar file path and jar entry path. " +
                "jar file path=''{0}'', jar entry path=''{1}''",
                 jarFilePath,jarEntryPath
            ),e);
        }
    }

    @Override
    public String toString() {
        return toURI().toString();
    }

    /**
     * Replies the CommonJS module jar URI which which is a suitable
     * resolution context URI for resolving CommonJS module IDs.
     * <p>
     * If this jar entry path of this URI is '/', this URI is already a
     * suitable resolution context.
     * <p>
     * If this jar entry path refers to a directory in the jar file, it is
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

        String normalizedJarEntryPath =
            new File(jarEntryPath).toPath().normalize().toString()
                .replace("\\", "/");

        // jar entry name without leading '/'
        final String normalizedJarEntryName =
            normalizedJarEntryPath.startsWith("/")
                ? normalizedJarEntryPath.substring(1)
                : normalizedJarEntryPath;

        if (normalizedJarEntryName.isEmpty()) {
            final ModuleJarURI resolutionContextUri =
                    new ModuleJarURI();
            resolutionContextUri.jarFilePath = this.jarFilePath;
            resolutionContextUri.jarEntryPath = "/";
            return resolutionContextUri;
        }

        // if this URI refers to a jar entry of type file, then use
        // its parent dir as context path
        try(final JarFile jar = new JarFile(getJarFile())) {
            final JarEntry entry = jar.getJarEntry(normalizedJarEntryName);
            if (entry != null) {
                if (!entry.isDirectory()) {
                    normalizedJarEntryPath = new File(normalizedJarEntryPath)
                        .toPath().getParent().toString()
                        .replace("\\", "/");
                }
            }
        }
        final ModuleJarURI resolutionContextUri =
            new ModuleJarURI();
        resolutionContextUri.jarFilePath = this.jarFilePath;
        resolutionContextUri.jarEntryPath = normalizedJarEntryPath;
        return resolutionContextUri;
    }
}
