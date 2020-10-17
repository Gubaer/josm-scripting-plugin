package org.openstreetmap.josm.plugins.scripting.graalvm;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static java.text.MessageFormat.format;

/**
 * The ID of a CommonJS module.
 * 
 * A module ID consists of a sequence of segments, separated by <code>/</code>.
 * I may include the segments <code>.</code> and <code>..</code>. It may also
 * start with this segments. It must not start with a leading <code>/</code>.
 */
public class ModuleID {
    private String value;

    /**
     * Throws an exception, if <code>moduleId</code> isn't valid.
     *
     * It is valid, if
     * <ul>
     *     <li>it is neither null nor empty</li>
     *     <li>doesn't start with a leading <code>/</code></li>
     * </ul>
     *
     * @param moduleId the module ID
     * @throws NullPointerException if moduleId is null
     * @throws IllegalArgumentException if moduleId is empty
     * @throws IllegalArgumentException if moduleId starts with <code>/</code>
     */
    static public void ensureValid(@NotNull final String moduleId) {
        Objects.requireNonNull(moduleId);
        final String trimmedModuleId = moduleId.trim();
        if (trimmedModuleId.isEmpty()) {
            throw new IllegalArgumentException(format(
                "invalid module id. module id must not be empty. " +
                "moduleId=''{0}''", moduleId
            ));
        }
        if (trimmedModuleId.startsWith("/")) {
            throw new IllegalArgumentException(format(
                "invalid module id. must not start with leading '/'. " +
                "moduleId=''{0}''", moduleId
            ));
        }
    }

    /**
     * Replies true, if <code>moduleId</code> is (syntactically) valid.
     *
     * @param moduleId the module id, i.e. <code>josm/layers</code>
     * @return true, if the <code>moduleId</code> is valid; false, otherwise
     */
    @SuppressWarnings("unused")  // part of the public API
    static public boolean isValid(@NotNull String moduleId) {
        try {
            ensureValid(moduleId);
            return true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Creates a module ID with a given <code>value</code>.
     *
     * @param value the string value of the module ID
     * @throws NullPointerException if <code>value</code> is null
     * @throws IllegalArgumentException if <code>value</code> isn't valid
     * @see #ensureValid(String)
     */
    public ModuleID(@NotNull final String value) {
        Objects.requireNonNull(value);
        ensureValid(value);
        this.value = value.trim();
    }

    /**
     * Returns true, if this module ID  neither starts with <code>./</code>
     * nor with <code>../</code>.
     *
     * @return true, if this module ID is absolute; false, otherwise
     */
    public boolean isAbsolute() {
        return !(value.startsWith("./") || value.startsWith("../"));
    }

    /**
     * Returns true, if this module ID either starts with <code>./</code>
     * or with <code>../</code>.
     *
     * @return true, if this module ID is relative; false, otherwise
     */
    public boolean isRelative() {
        return !isAbsolute();
    }

    /**
     * Returns the a normalized module ID equal to this module ID.
     *
     * Normalized:
     * <ul>
     *     <li>without an trailing <code>.js</code></li>
     *     <li>with  sequences of  <code>/</code> replaced by one
     *     <code>/</code></li>
     * </ul>
     *
     * @return the normalized module ID
     */
    public @NotNull ModuleID normalized() {
        if (value.endsWith(".js")) {
            return new ModuleID(value
                    .substring(0, value.length() - 3)
                    .replaceAll("/+", "/"));
        } else {
            return new ModuleID(value.replaceAll("/+", "/"));
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleID moduleID = (ModuleID) o;

        return Objects.equals(value, moduleID.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
