package org.openstreetmap.josm.plugins.scripting.graalvm.commonjs;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * A repository of CommonJS modules. It has a base URI and it is able
 * to resolve CommonJS module ids against it.
 */
public interface ICommonJSModuleRepository extends IModuleResolver {


    /**
     * Replies the base URI of this repository
     *
     * @return the base URI
     */
    @NotNull  URI getBaseURI();

    /**
     * Replies true if the base URI of this repo is the base of the module
     * URI
     *
     * @param moduleURI the module uri
     * @return true, if this repo is the base for the module URI
     */
    boolean isBaseOf(@NotNull URI moduleURI);
}
