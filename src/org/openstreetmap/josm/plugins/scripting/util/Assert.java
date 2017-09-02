package org.openstreetmap.josm.plugins.scripting.util;

import java.text.MessageFormat;

public class Assert {

    public static void assertArg(boolean condition, String message,
            Object...objs) {
        if (!condition){
            throw new IllegalArgumentException(
                    MessageFormat.format(message, objs)
            );
        }
    }
    public static void assertState(boolean condition, String message,
            Object... objs) {
        if (!condition) {
            throw new IllegalStateException(
                    MessageFormat.format(message, objs)
            );
        }
    }
}
