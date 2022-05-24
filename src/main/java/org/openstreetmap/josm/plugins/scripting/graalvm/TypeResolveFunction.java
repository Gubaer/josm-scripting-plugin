package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.graalvm.polyglot.Value;
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;

import java.text.MessageFormat;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Supports access from JavaScript to Java classes in the plugin. The standard
 * <code>Java.type(...)</code> function in GraalJS can't access them because
 * of class loading issues (probably because JOSM loads the scripting.jar
 * dynamically).
 */
@SuppressWarnings("unused")
public class TypeResolveFunction implements Function<String, Value>  {
    static private final Logger logger = Logger.getLogger(TypeResolveFunction.class.getName());

    private void logFine(Supplier<String> messageBuilder) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message);
        }
    }

    private void logFine(Supplier<String> messageBuilder, Throwable t) {
        if (logger.isLoggable(Level.FINE)) {
            final String message = messageBuilder.get();
            logger.log(Level.FINE, message, t);
        }
    }
    @Override
    public Value apply(String className) {
        final String basePackageName = ScriptingPlugin.class.getPackageName();
        if (className == null || !className.startsWith(basePackageName)) {
            logFine(() -> MessageFormat.format(
                "Couldn't lookup a java class in the plugin jar. Class name is null or doesn't belong "
                + "to the plugin package. className='{0}'",
                className
            ));
            return null;
        }
        try {
            final Class<?> clazz = Class.forName(className);
            return Value.asValue(clazz);
        } catch (ClassNotFoundException e) {
            logFine(() -> MessageFormat.format(
                "Couldn't lookup a java class in the plugin jar. className='{0}'",
                className
            ), e);
            return null;
        }
    }
}
