package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

@SuppressWarnings("unused")
public class CommonJSModuleRepositoryFactory {

    private final static Logger logger =
        Logger.getLogger(CommonJSModuleRepositoryFactory.class.getName());

    private final static CommonJSModuleRepositoryFactory instance =
        new CommonJSModuleRepositoryFactory();

    public static CommonJSModuleRepositoryFactory getInstance() {
        return instance;
    }

    /**
     * Builds a CommonJS module repository given the URI of its base.
     * <p>
     * Expects either a valid file- or a valid jar-URI.
     *
     * @param uri the URI
     * @return the CommonJS module repository
     * @throws IllegalCommonJSModuleBaseURI thrown, if <code>uri</code>
     * isn't a valid URI for a CommonJS module repository
     * @throws NullPointerException if uri is null
     */
    public ICommonJSModuleRepository build(@NotNull final URI uri)
        throws IllegalCommonJSModuleBaseURI {
        Objects.requireNonNull(uri);
        try {
            final URL url = uri.toURL();
            switch(url.getProtocol().toLowerCase()) {
                case "file":
                    final String path = url.getPath();
                    return new FileSystemJSModuleRepository(path);

                case "jar":
                    try {
                        return new JarJSModuleRepository(uri);
                    } catch(IOException e) {
                        throw new IllegalCommonJSModuleBaseURI(e);
                    }

                default:
                    throw new IllegalCommonJSModuleBaseURI(format(
                        "unsupported protocol for CommonJS module base. url=''{0}''", url
                    ));
            }
        } catch(MalformedURLException e) {
            throw new IllegalCommonJSModuleBaseURI(e);
        }
    }

    /**
     * Builds a CommonJS module repository given the URI of its base.
     * <p>
     * Expects either a valid file- or a valid jar-URI.
     *
     * @param uri the URI
     * @return the CommonJS module repository
     * @throws IllegalCommonJSModuleBaseURI thrown, if <code>uri</code>
     * isn't a valid URI for a CommonJS module repository
     * @throws NullPointerException if uri is null
     */
    public ICommonJSModuleRepository build(@NotNull final String uri)
            throws IllegalCommonJSModuleBaseURI {
        Objects.requireNonNull(uri);
        try {
            return build(new URI(uri.trim()));
        } catch(Exception e) {
            throw new IllegalCommonJSModuleBaseURI(e);
        }
    }
}
