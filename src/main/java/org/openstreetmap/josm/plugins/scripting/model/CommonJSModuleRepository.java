package org.openstreetmap.josm.plugins.scripting.model;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * A location from where CommonJS modules are loaded.
 * <p>
 * The scripting plugin loads CommonJS modules either from a directory in
 * the file system or from a jar file in the local file system. It doesn't
 * load modules from remote locations, i.e. from an HTTP server.
 */
public class CommonJSModuleRepository {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(
            CommonJSModuleRepository.class.getName());

    final private URL url;
    final private URI uri;

    /**
     * Creates a repository.
     * <p>
     * Doesn't enforce that <code>dir</code> exists or that it is a
     * directory.
     *
     * @param dir a directory. Must not be null.
     * @throws IllegalArgumentException thrown if dir is null
     */
    public CommonJSModuleRepository(@NotNull File dir)  throws IllegalArgumentException {
        Objects.requireNonNull(dir);
        uri = dir.toURI();
        try {
            url = uri.toURL();
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException(
                MessageFormat.format("failed to convert file ''{0}'' to URL", dir), e);
        }
    }

    protected void ensureValidUrl(URL url) {
        switch(url.getProtocol()) {
            case "file":
                return;
            case "jar":
                String s = url.getFile();
                try {
                    URL jarFileUrl = new URI(s).toURL();
                    if (jarFileUrl.getProtocol().equals("file")) return;
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Type of URL not supported for CommonJS module repository, "
                       + "got {0}", url));
                } catch(URISyntaxException | MalformedURLException e){
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Failed to create URL for jar file <{0}>.", s));
                }
            default:
                throw new IllegalArgumentException(MessageFormat.format(
                    "Type of URL not supported for CommonJS module repository, "
                  + "got {0}", url));
        }
    }

    /**
     * Creates a repository
     * <p>
     * <code>url</code> must be a valid file or jar URL.
     *
     * @param url an acceptable URL for a module repository as string.
     *              Must not be null.
     * @throws NullPointerException thrown if url is null
     * @throws IllegalArgumentException thrown if url isn't a valid URL
     */
    public CommonJSModuleRepository(@NotNull String url) {
        Objects.requireNonNull(url);
        try {
            final var repo = new URI(url).toURL();
            ensureValidUrl(repo);
            this.url = repo;
            this.uri = repo.toURI();
        } catch(MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(
                MessageFormat.format("failed to convert string ''{0}'' to URL", url), e);
        }
    }

    /**
     * Creates a repository.
     * <p>
     * <code>url</code> must be a valid file or jar URL.
     *
     * @param url an acceptable URL for a module repository. Must not be null.
     * @throws IllegalArgumentException thrown if url isn't a valid URL
     */
    public CommonJSModuleRepository(@NotNull URL url) throws IllegalArgumentException {
        Objects.requireNonNull(url);
        ensureValidUrl(url);
        this.url = url;
        try {
            this.uri = url.toURI();
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(
                MessageFormat.format("failed to convert URL ''{0}'' to URI", url), e);
        }
    }

    /**
     * Creates a repository.
     * <p>
     * <code>jar</code> must be an existing local jar file.
     *
     * @param jar an existing and readable local jar file. Must not be null.
     * @throws IllegalArgumentException thrown if jar is null or if the jar
     *      URL can't be created
     */
    public CommonJSModuleRepository(JarFile jar)
           throws IllegalArgumentException {
        this(jar, "/");
    }

    /**
     * Creates a repository.
     * <p>
     * <code>jar</code> must be an existing local jar file.
     *
     * @param jar an existing and readable local jar file. Must not be null.
     * @param jarPath the jar path. May be null.
     * @throws IllegalArgumentException thrown if jar is null or if the jar URL
     *   can't be created
     */
    public CommonJSModuleRepository(@NotNull JarFile jar, String jarPath) throws IllegalArgumentException {
        Objects.requireNonNull(jar);
        if (jarPath == null) jarPath = "/";
        jarPath = "/" + jarPath.trim().replace("\\", "/")
                .replaceAll("/+", "/")
                .replaceAll("^/","");
        try {
            url = new URI("jar:" + new File(jar.getName()).toURI().toURL() + "!" + jarPath).toURL();
            uri = url.toURI();
        } catch(MalformedURLException  | URISyntaxException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Failed to create jar URL for jar file ''{0}'' and jar path "
              + "''{1}''. Exception is: {2}", jar, jarPath, e));
        }
    }


    /**
     * Replies the local file for this module repository, either a directory,
     * or the local jar file.
     *
     * @return the file, or null, if something unexpected happens
     */
    public File getFile() {
        if (url.getProtocol().equals("file")) {
            return new File(url.getFile());
        } else {
            try {
                return new File(new URI(url.getFile().split("!")[0]).toURL().getFile());
            } catch(URISyntaxException | MalformedURLException e) {
                return null;
            }
        }
    }

    /**
     * Replies jar file entry where to look for CommonJS modules in the jar
     * file.
     * <p>
     * Replies null, if this module repository is a local directory, not a jar
     * file.
     *
     * @return the jar file entry or null
     */
    public String getJarEntry() {
        if (url.getProtocol().equals("file")) {
            return null;
        }
        String[] segments = url.toString().split("!");
        if (segments.length != 2) return null;
        return segments[1];
    }

    /**
     * Replies the URL of this module repository.
     *
     * @return the url
     */
    public URL getURL() {
        return url;
    }

    /* --------------------------------------------------------------------- */
    /* hash code and equals                                                  */
    /* ----------------------------------------------------------------------*/
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommonJSModuleRepository other = (CommonJSModuleRepository) obj;
        if (uri == null) {
            return other.uri == null;
        } else {
            return uri.equals(other.uri);
        }
    }
}
