package org.openstreetmap.josm.plugins.scripting.js;

import java.text.MessageFormat;

@SuppressWarnings("unused")
public class JSMixinException extends Exception {

    static public JSMixinException make(String msg, Object...args) {
        String m = MessageFormat.format(msg, args);
        return new JSMixinException(m);
    }

    static public JSMixinException make(Throwable cause, String msg, Object...args) {
        String m = MessageFormat.format(msg, args);
        return new JSMixinException(m, cause);
    }

    public JSMixinException() {
        super();
    }

    public JSMixinException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSMixinException(String message) {
        super(message);
    }

    public JSMixinException(Throwable cause) {
        super(cause);
    }
}
