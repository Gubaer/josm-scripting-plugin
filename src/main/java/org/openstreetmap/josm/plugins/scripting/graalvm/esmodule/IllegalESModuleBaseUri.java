package org.openstreetmap.josm.plugins.scripting.graalvm.esmodule;

public class IllegalESModuleBaseUri extends Exception {
    public IllegalESModuleBaseUri(String message) {
        super(message);
    }
    public IllegalESModuleBaseUri(String message, Throwable cause) {
        super(message, cause);
    }
}
