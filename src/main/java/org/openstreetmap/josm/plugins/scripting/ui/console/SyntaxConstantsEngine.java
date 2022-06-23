package org.openstreetmap.josm.plugins.scripting.ui.console;

import org.openstreetmap.josm.plugins.Plugin;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.*;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class SyntaxConstantsEngine {

    /**
     * A regular-expression based rule that derives a suitable
     * {@link org.fife.ui.rsyntaxtextarea.SyntaxConstants syntax style} for
     * a media-type.
     */
    public static class Rule {
        static private final Logger logger = Logger.getLogger(Rule.class.getName());

        /**
         * Build the rule from two property values.
         *
         * @param syntaxStyleProperty the value of the syntax style property
         * @param patternProperty the value of the pattern property
         * @return a rule or null, if there is a problem with the property values
         */
        public static @Null Rule fromProperties(@NotNull final String syntaxStyleProperty,
                                                @NotNull final String patternProperty) {
            Objects.requireNonNull(syntaxStyleProperty);
            Objects.requireNonNull(patternProperty);

            if (syntaxStyleProperty.isBlank() || patternProperty.isBlank()) {
                return null;
            }
            try {
                var pattern = Pattern.compile(patternProperty);
                return new Rule(syntaxStyleProperty, pattern);
            } catch(PatternSyntaxException e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Illegal regular expression in syntax style rule", e);
                }
                return null;
            }
        }

        private final String syntaxStyle;
        private final Pattern pattern;

        /**
         * Creates a new rule.
         *
         * @param syntaxStyle the name of the syntax style
         * @param pattern the matching pattern
         */
        public Rule(@NotNull final String syntaxStyle, @NotNull  final Pattern pattern) {
            Objects.requireNonNull(syntaxStyle);
            Objects.requireNonNull(pattern);
            this.syntaxStyle = syntaxStyle;
            this.pattern = pattern;
        }

        /**
         * Replies the syntax style name
         *
         * @return syntax style name
         */
        public @NotNull  String getSyntaxStyle() {
            return syntaxStyle;
        }

        /**
         * Replies true if the rule matches for the mime-type <code>mime-type</code>.
         *
         * @param mimeType the mime-type
         * @return true if the rule matches for the mime-type <code>mime-type</code>.
         */
        public boolean matches(final String mimeType) {
            if (mimeType == null) {
                return false;
            }
            return pattern.matcher(mimeType).matches();
        }
    }

    /**
     * A collection of {@link Rule syntax style rules}.
     */
    public static class Rules {

        /**
         * Loads the rules from properties.
         *
         * Properties example:
         * <pre>
         *  rule.1.syntax-style=text/javascript
         *  rule.1.regexp=(?i).*javascript.*
         *
         *  rule.2.syntax-style=text/groovy
         *  rule.2.regexp=(?i).*groovy.*
         * </pre>
         *
         * @param properties the properties
         * @return the rules
         */
        static public @NotNull Rules loadFromProperties(final Properties properties) {
            if (properties == null) {
                return new Rules(null);
            }
            final var rulePattern = Pattern.compile("^rule\\.(\\d+).*");
            final var ruleIds = properties.keySet().stream().map(key -> {
                var matcher = rulePattern.matcher((String) key);
                    if (matcher.matches()) {
                        try {
                            return Integer.parseInt(matcher.group(1));
                        } catch(NumberFormatException e) {
                            logger.log(Level.FINE, MessageFormat.format(
                                    "Illegal property key ''{0}''", key
                            ), e);
                            // shouldn't happen because of matching regexp
                            return null;
                        }
                    } else {
                        logger.log(Level.FINE, MessageFormat.format(
                            "Illegal property key ''{0}''", key
                        ));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

            var syntaxStyleFormat = new MessageFormat("rule.{0}.syntax-style");
            var patternFormat = new MessageFormat("rule.{0}.regexp");

            var rules = ruleIds.stream().sorted()
                .map(id -> {
                    var formatArgs = new Integer[]{id};
                    var syntaxStyleValue = properties.getProperty(
                        syntaxStyleFormat.format(formatArgs));
                    var patternValue = properties.getProperty(
                        patternFormat.format(formatArgs));
                    if (syntaxStyleValue == null || patternValue == null) {
                        logger.fine(MessageFormat.format(
                            "Property for property key ''{0}'' or property key ''{1}'' not found in syntax rules",
                            syntaxStyleFormat.format(formatArgs),
                            patternFormat.format(formatArgs)
                        ));
                        return null;
                    }
                    return Rule.fromProperties(syntaxStyleValue, patternValue);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            return new Rules(rules);
        }

        private final List<Rule> rules;

        /**
         * Creates a collection of rules.
         *
         * @param rules the rules. If <code>null</code>, assumes empty list of rules.
         */
        Rules(final List<Rule> rules) {
            this.rules = Objects.requireNonNullElse(rules, Collections.emptyList());
        }

        /**
         * Creates an empty collection of rules.
         *
         */
        Rules() {
            this(null);
        }

        /**
         * Apply the rules to find a suitable syntax style for the mime-type
         * <code>mime-type</code>.
         *
         * @param mimeType the mime-type
         * @return the syntax style. <code>null</code>, if no suitable syntax
         * style is found
         */
        public @Null String deriveSuitableSyntaxStyle(@Null final String mimeType) {
            if (mimeType == null) {
                return null;
            }
            return rules.stream()
                .filter(rule -> rule.matches(mimeType))
                .map(Rule::getSyntaxStyle)
                .findFirst()
                .orElse(null);
        }

    }

    private static final Logger logger = Logger.getLogger(SyntaxConstantsEngine.class.getName());

    private static List<String> loadAvailableSyntaxConstants() {
        return Arrays.stream(org.fife.ui.rsyntaxtextarea.SyntaxConstants.class.getDeclaredFields())
            .filter(field ->
                Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("SYNTAX_STYLE_")
            )
            .map(field -> {
                final String value = "";
                field.setAccessible(true);
                try {
                    return (String) field.get(value);
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, MessageFormat.format(
                        "Failed to read syntax style constant ''{0}''",
                        field.getName()
                    ), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static final List<String> standardSyntaxConstants = loadAvailableSyntaxConstants();

    /**
     * Replies an unmodifiable list of syntax constants defined as static
     * final fields in {@link SyntaxConstantsEngine}
     *
     * @return the list of syntax constants
     */
    public static @NotNull List<String> getAvailableSyntaxConstants() {
        return Collections.unmodifiableList(standardSyntaxConstants);
    }

    private static SyntaxConstantsEngine instance;
    public static SyntaxConstantsEngine getInstance() {
        if (instance == null) {
            instance = new SyntaxConstantsEngine();
        }
        return instance;
    }

    private Rules rules = new Rules();

    private SyntaxConstantsEngine() {
    }

    public static final String SYNTAX_STYLE_RULES_FILE = "syntax-style-rules.properties";

    /**
     * Loads the rules for deriving syntax styles from mime-types.
     *
     * First, tries to load them from a configuration file in the plugin data
     * directory. If no such file exists, tries to load them from a resource
     * file included in the plugin jar.
     *
     * @param context the plugin context
     */
    public void loadRules(@NotNull final Plugin context) {
        Objects.requireNonNull(context);
        rules = new Rules();
        InputStream input = null;
        var file = new File(
            context.getPluginDirs().getUserDataDirectory(false),
            SYNTAX_STYLE_RULES_FILE
        );
        try {
            // try to load rules from a configuration file
            if (file.isFile() && file.canRead()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                        "Loading syntax style rules from file ''{0}''",
                        file.getAbsolutePath()
                    ));
                }
                input = new FileInputStream(file);
            } else {
                // if no configuration file is available try to load default
                // rules from a resource
                var resourceName = "/resources/" + SYNTAX_STYLE_RULES_FILE;
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(MessageFormat.format(
                        "Loading syntax style rules from resource ''{0}''",
                        resourceName
                    ));
                }
                input = SyntaxConstantsEngine.class.getResourceAsStream(resourceName);
                if (input == null) {
                    logger.warning(MessageFormat.format(
                        "Failed to read syntax style rules. Resource ''{0}'' not found.",
                        resourceName
                    ));
                }
            }
            if (input != null) {
                var properties = new Properties();
                properties.load(new InputStreamReader(input));
                rules = Rules.loadFromProperties(properties);
            }
        } catch(IOException e) {
            logger.log(Level.WARNING, "Failed to read syntax style rules", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch(IOException e){
                    // ignore
                }
            }
        }
    }

    /**
     * Determines a suitable syntax style for content with mime-type <code>mimeType</code>.
     *
     * @param mimeType the mime-type
     * @return a suitable syntax style from {@link org.fife.ui.rsyntaxtextarea.SyntaxConstants},
     *  or <code>null</code> if no syntax style is suitable
     */
    public @Null String deriveSyntaxStyle(@NotNull final String mimeType) {
        Objects.requireNonNull(mimeType);

        // rule 1: exact match of mime-type and syntax style?
        if (standardSyntaxConstants.contains(mimeType)) {
            return mimeType;
        }
        // rule 2: use rules from a configuration file
        return rules.deriveSuitableSyntaxStyle(mimeType);
    }
}
