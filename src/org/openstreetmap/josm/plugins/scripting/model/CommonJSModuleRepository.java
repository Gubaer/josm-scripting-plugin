package org.openstreetmap.josm.plugins.scripting.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * <p>Represents a location where CommonJS modules are loaded from.</p>
 *
 * <p>The scripting plugin loads CommonJS modules either from a directory in
 * the file system or from a jar file in the local file system. It doesn't
 * load modules from remove location, i.e. from a HTTP server.</p>
 */
public class CommonJSModuleRepository {
    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(
            CommonJSModuleRepository.class.getName());

    private URL url;

    /**
     * <p>Creates a repository.</p>
     *
     * <p>Doesn't enforce that <code>dir</code> exists or that it is a
     * directory.</p>
     *
     * @param dir a directory. Must not be null.
     * @throws IllegalArgumentException thrown if dir is null
     */
    public CommonJSModuleRepository(@NotNull File dir)
            throws IllegalArgumentException {
        Objects.requireNonNull(dir);
        try {
            url = dir.toURI().toURL();
        } catch(MalformedURLException e) {
            Assert.assertArg(false, "Failed to convert file {0} to URL. "
               + "Exception is: {1}", dir, e);
        }
    }

    protected void ensureValidUrl(URL url) {
        if (url.getProtocol().equals("file")) {
            return;
        } else  if (url.getProtocol().equals("jar")) {
            String s = url.getFile();
            try {
                URL jarFileUrl = new URL(s);
                if (jarFileUrl.getProtocol().equals("file")) return;
                Assert.assertArg(false,
                    "Type of URL not supported for CommonJS module repository, "
                   + "got {0}", url);
            } catch(MalformedURLException e){
                Assert.assertArg(false,
                    "Failed to create URL for jar file <{0}>.", s);
            }
        } else {
            Assert.assertArg(false,
                "Type of URL not supported for CommonJS module repository, "
              + "got {0}", url);
        }
    }

    /**
     * <p>Creates a repository.</p>
     *
     * <p><code>url</code> must be a valid file or jar URL.</p>
     *
     * @param url an acceptable URL for a module repository as string.
     *              Must not be null.
     * @throws NullPointerException thrown if url is null
     * @throws MalformedURLException thrown if url isn't a valid URL
     */
    public CommonJSModuleRepository(@NotNull String url)
            throws MalformedURLException {
        Objects.requireNonNull(url);
        URL repo = new URL(url);
        ensureValidUrl(repo);
        this.url = repo;
    }

    /**
     * <p>Creates a repository.</p>
     *
     * <p><code>url</code> must be a valid file or jar URL.</p>
     *
     * @param url an acceptable URL for a module repository. Must not be null.
     */
    public CommonJSModuleRepository(@NotNull URL url) {
        Objects.requireNonNull(url);
        ensureValidUrl(url);
        this.url = url;
    }

    /**
     * <p>Creates a repository.</p>
     *
     * <p><code>jar</code> must be an existing local jar file.</p>
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
     * <p>Creates a repository.</p>
     *
     * <p><code>jar</code> must be an existing local jar file.</p>
     *
     * @param jar an existing and readable local jar file. Must not be null.
     * @param jarPath the jar path. May be null.
     * @throws IllegalArgumentException thrown if jar is null or if the jar URL
     *   can't be created
     */
    public CommonJSModuleRepository(@NotNull JarFile jar, String jarPath)
            throws IllegalArgumentException {
        Objects.requireNonNull(jar);
        if (jarPath == null) jarPath = "/";
        jarPath = "/" + jarPath.trim().replace("\\", "/")
                .replaceAll("/+", "/")
                .replaceAll("^/","");

        try {
            this.url = new URL("jar:" + new File(jar.getName()).toURI()
                    .toURL() + "!" + jarPath);
        } catch(MalformedURLException e) {
            Assert.assertArg(false,
                "Failed to create jar URL for jar file <{0}> and jar path "
              + "<{1}>. Exception is: {2}", jar, jarPath, e);
        }
    }


    /**
     * <p>Replies the local file for this module repository, either a directory,
     * or the local jar file.</p>
     *
     * @return the file, or null, in something unexpected happens
     */
    public File getFile() {
        if (url.getProtocol().equals("file")) {
            return new File(url.getFile());
        } else {
            try {
                return new File(new URL(url.getFile().split("!")[0]).getFile());
            } catch (MalformedURLException e) {
                // should not happen
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * <p>Replies jar file entry where to look for CommonJS modules in the
     *      jar file.</p>
     *
     * <p>Replies null, if this module repository is a local directory, not
     *      a jar file.</p>
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
     * <p>Replies the URL of this module repository.</p>
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
        result = prime * result + ((url == null) ? 0 : url.hashCode());
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
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }
}
