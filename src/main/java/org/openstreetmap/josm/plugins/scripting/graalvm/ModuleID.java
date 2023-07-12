package org.openstreetmap.josm.plugins.scripting.graalvm;

import org.openstreetmap.josm.plugins.scripting.model.RelativePath;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * The ID of a CommonJS module.
 * <p>
 * A module ID consists of a sequence of segments, separated by <code>/</code>.
 * It may include and/or start with the segments <code>.</code> and <code>..</code>.
 * It must not start with a leading <code>/</code>.
 */
@SuppressWarnings("unused")
public class ModuleID {
    final private RelativePath value;

    /**
     * Creates a module ID with a given <code>value</code>.
     *
     * @param value the module ID as relative path
     * @throws NullPointerException if <code>value</code> is null
     */
    public ModuleID(@NotNull final RelativePath value) {
        this.value = value;
    }

    /**
     * Returns true, if this module ID  neither starts with <code>./</code>
     * nor with <code>../</code>.
     *
     * @return true, if this module ID is absolute; false, otherwise
     */
    public boolean isAbsolute() {
        return !(value.startsWith(RelativePath.of(".")) || value.startsWith(RelativePath.of("..")));
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
     * Returns the normalized module ID equal to this module ID.
     * <p>
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
        var segments = this.value.getSegments();
        if (segments.size() == 0) {
            return this;
        }
        var segment = segments.remove(segments.size()-1);
        if (segment.endsWith(".js")) {
            segment = segment.substring(0, segment.length() - 3);
            segments.add(segment);
            return new ModuleID(RelativePath.of(segments));
        } else {
            return this;
        }
    }

    /**
     * Replies this module ID as {@link RelativePath}.
     *
     * @return the relative path
     */
    public @NotNull  RelativePath toRelativePath() {
            return value;
    }

    @Override
    public String toString() {
        return value.toString();
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
