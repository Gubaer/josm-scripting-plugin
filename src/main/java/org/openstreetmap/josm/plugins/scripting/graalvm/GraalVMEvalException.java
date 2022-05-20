package org.openstreetmap.josm.plugins.scripting.graalvm;

@SuppressWarnings({"unused", "WeakerAccess"})
public class GraalVMEvalException extends Exception {
    public GraalVMEvalException() {
        super();
    }

    public GraalVMEvalException(String message) {
        super(message);
    }

    public GraalVMEvalException(String message, Throwable cause) {
        super(message, cause);
    }
}
