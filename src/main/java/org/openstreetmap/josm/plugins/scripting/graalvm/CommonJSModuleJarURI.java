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
import java.util.jar.JarFile;

/**
 * A CommonJSModuleJarURI is a jar-URI with an embedded file-URI.
 */
public class CommonJSModuleJarURI {

    /**
     * Replies true, if <code>uri</code> is a valid URI for a Common JS
     * module in a jar file.
     *
     * @param uri the uri
     * @return true, if <code>uri</code> is valid; false otherwise
     */
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
    static public boolean isValid(@NotNull final URI uri) {
        try {
            new CommonJSModuleJarURI(uri);
            return true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }

    private String jarFilePath;
    private String jarEntryPath = "/";

    private CommonJSModuleJarURI() {
    }

    /**
     * Creates a CommonJSModuleJarURI given an URI.
     * @param uri
     * @throws IllegalArgumentException thrown, if <code>uri</code> isn't
     *  a valid jar URI
     * @throws IllegalArgumentException thrown, if <code>uri</code> doesn't
     *  embed a file URI
     * @throws IllegalArgumentException thrown, if <code>uri</code> doesn't
     *  end with a jar entry path after <code>!</code>
     */
    CommonJSModuleJarURI(@NotNull final URI uri) {
        Objects.requireNonNull(uri);
        if (!"jar".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(MessageFormat.format(
                "illegal URI. Expected scheme ''jar'', got ''{0}''",
                uri.getScheme()
            ));
        }
        try {
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
        } catch(URISyntaxException e) {
            // shouldn't happen
            //TODO(karl): log it, rethrow it?
        }
        jarEntryPath = fileUri.toString().substring(i+1);
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
     * Replies the file object representing the the jar file this URI
     * refers to
     *
     * @return the jar file
     */
    public @NotNull File getJarFile() {
        return new File(jarFilePath);
    }

    /**
     * Replies the path of the jar entry, i.e. <code>/foo/bar/mymodule.js</code>
     * this URI refers to
     *
     * @return the path of the jar entry
     */
    public String getJarEntryPathAsString() {
        return jarEntryPath;
    }

    /**
     * Replies the path of the jar entry, i.e. <code>/foo/bar/mymodule.js</code>
     * this URI refers to
     *
     * @return the path of the jar entry
     */
    public Path getJarEntryPath() {
        return new File(jarEntryPath).toPath();
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
     * Replies true, if this URI refers to a jar file.
     *
     * @return true, if this URI refers to a jar file;
     *  false otherwise
     */
    public boolean refersToJarFile() {
        try(final JarFile jf = new JarFile(jarFilePath)) {
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * Replies a normalized version of this URI.
     * <ul>
     *     <li>the file path is converted to an absolute, normalized path</li>
     *     <li>the jar entry path is converted to an absolute,
     *     normalized path</li>
     * </ul>
     * @return
     */
    public @NotNull CommonJSModuleJarURI normalized() {
        final String normalizedFilePath = new File(jarFilePath)
                .toPath().toAbsolutePath().normalize().toString();
        // don't apply toAbsolutePath(). The jar entry path is already
        // absolute (starts with an /); and it doesn't refer to a file
        // in the local file system, but to a jar entry in the jar file
        final String normalizedJarEntryPath = new File(jarEntryPath)
                .toPath().normalize().toString();

        final CommonJSModuleJarURI normalizedURI = new CommonJSModuleJarURI();
        normalizedURI.jarEntryPath = normalizedJarEntryPath;
        normalizedURI.jarFilePath = normalizedFilePath;
        return normalizedURI;
    }

    /**
     * Replies this object as {@link URI}
     *
     * @return
     */
    public @NotNull URI toURI() {
        try {
            return new URI(toString());
        } catch(URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
            "jar:file://{0}!{1}",
            jarFilePath, jarEntryPath
        );
    }
}
