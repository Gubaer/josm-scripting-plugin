package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import static java.text.MessageFormat.format;

/**
 * ESModuleRepositoryBuilder creates instances of {@link IESModuleRepository} given
 * a file- or jar-URI that refers to the root of the repository.
 */
public class ESModuleRepositoryBuilder {

    /**
     * Builds an {@link IESModuleRepository module repository} given an <code>uri</code> that
     * refers to the root of the repository.
     * <p>
     * <code>uri</code> is either
     * <ul>
     *     <li>a file-URI which refers to a readable local directory</li>
     *     <li>a jar-URI which refers to readable local jar-file with an optional path to
     *     a jar entry of type directory</li>
     * </ul>
     *
     * @param uri the root of the repository
     * @return the module repository
     * @throws IllegalESModuleBaseUri thrown if <code>uri</code> doesn't refer to a valid
     *    root for a ES Module repository
     * @throws NullPointerException thrown if <code>uri</code> is null
     */
    public @NotNull IESModuleRepository build(@NotNull final URI uri) throws IllegalESModuleBaseUri {
        Objects.requireNonNull(uri);
        try {
            final URL url = uri.toURL();
            switch(url.getProtocol().toLowerCase()) {
                case "file":
                    final String path = url.getPath();
                    return new FileSystemESModuleRepository(new File(path));

                case "jar":
                    try {
                        return new JarESModuleRepository(uri);
                    } catch(IOException | IllegalESModuleBaseUri e) {
                        throw new IllegalESModuleBaseUri(format(
                            "invalid base URI for ES Module repository. uri=''{0}''", uri),
                            e);
                }

                default:
                    throw new IllegalESModuleBaseUri(format(
                        "unsupported protocol for ES Module repository URI. uri=''{0}''", url
                    ));
            }
        } catch(MalformedURLException e) {
            throw new IllegalESModuleBaseUri(format("Invalid URI for ES Module repository. uri=''{0}''", uri), e);
    }
    }
}
