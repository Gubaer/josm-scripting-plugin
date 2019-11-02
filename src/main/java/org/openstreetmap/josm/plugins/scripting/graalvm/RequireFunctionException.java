package org.openstreetmap.josm.plugins.scripting.graalvm;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RequireFunctionException extends RuntimeException {
    public RequireFunctionException() {
        super();
    }

    public RequireFunctionException(String message) {
        super(message);
    }

    public RequireFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}
