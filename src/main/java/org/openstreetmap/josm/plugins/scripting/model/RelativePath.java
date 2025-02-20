package org.openstreetmap.josm.plugins.scripting.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The scripting plugin has to deal with <strong>relative paths</strong>. A relative path is a
 * sequence of <code>path segments</code>. The <code>empty path</code> consists of zero segments.
 * <p>
 * Relative paths can include two special types of segments:
 * <ol>
 *    <li>the segment <code>'.'</code> which refers to the current position in the path</li>
 *    <li>the segment <code>'..'</code> which refers to the parent position in the path</li>
 * </ol>
 * <p>
 * In the scripting plugin
 * <ul>
 *     <li>module IDs for ECMAscript modules are relative paths</li>
 *     <li>module IDs of CommonJS modules are relative paths</li>
 *     <li>the path to source file in a module repository, be it a
 *     {@link org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.FileSystemESModuleRepository}
 *     or a {@link org.openstreetmap.josm.plugins.scripting.graalvm.esmodule.JarESModuleRepository}</li>, is
 *     a relative path
 * </ul>
 * <p>
 * Earlier versions of the scripting plugin relied on {@link Path} to represent and manipulate relative paths.
 * This turned out to be fragile because {@link Path} behaves differently on the Unix and the Windows platform
 * and because the {@link Path#resolve(Path) resolve method} assumes that the resolution context is a directory.
 * <p>
 * {@link RelativePath} therefore provides a custom implementation of a relative path.
 */
@SuppressWarnings("unused")
public class RelativePath {

    /**
     * The empty relative path
     */
    static public final RelativePath EMPTY = new RelativePath(null);

    final private List<String> segments = new ArrayList<>();

    /**
     * Parses the string representation of a relative path. Examples of <strong>valid</strong> relative
     * paths are:
     * <ul>
     *     <li>"" - the empty path</li>
     *     <li>"foo" - a path consisting of one segment</li>
     *     <li>"foo/bar/baz.js" - a path consisting of three segments</li>
     *     <li>"foo///bar" - sequences of '/' are treated as one '/'</li>
     * </ul>
     *
     * Examples of <strong>invalid</strong> relative paths are:
     * <ul>
     *     <li>"/" - a relative path must not consist of a '/' only</li>
     *     <li>"/foo/bar" - a relative path must not start with a '/'</li>
     *     <li>"foo\baz" - a relative path must not include the character '\'</li>
     * </ul>
     *
     * @param path the path as string
     * @return the parsed relative path
     * @throws NullPointerException if <code>path</code> is null
     * @throws IllegalArgumentException if <code>path</code> is invalid
     */
    static public @NotNull  RelativePath parse(@NotNull String path) {
        Objects.requireNonNull(path);
        if (path.contains("\\")) {
            throw new IllegalArgumentException(
                MessageFormat.format("illegal relative path ''{0}''. Must not contain ''\\''", path)
            );
        }
        if (path.strip().startsWith("/")) {
            throw new IllegalArgumentException(
                    MessageFormat.format("illegal relative path ''{0}''. Must not start with ''/''", path)
            );
        }
        final var segments = path.split("/+");
        if (path.isBlank()) {
            return EMPTY;
        }
        return new RelativePath(Arrays.asList(segments));
    }

    /**
     * Creates a copy of a relative path
     *
     * @param other the other path. Must not be null.
     * @return a copy of the path
     * @throws NullPointerException if <code>other</code> is null
     */
    static public @NotNull RelativePath of(@NotNull RelativePath other) {
        Objects.requireNonNull(other);
        if (other.isEmpty()) {
            return new RelativePath(null);
        } else {
            return new RelativePath(other.segments);
        }
    }

    /**
     * Creates a relative path from the path segments in a {@link Path}. <code>path</code>
     * must not be absolute.
     *
     * @param path the path
     * @return the relative path
     * @throws NullPointerException if <code>path</code> is null
     * @throws IllegalArgumentException if <code>path</code> is absolute
     */
    static public @NotNull RelativePath of(@NotNull Path path) {
        Objects.requireNonNull(path);
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(
                MessageFormat.format("path must not be absolute, got ''{0}''", path)
            );
        }
        if (path.getNameCount() == 1 && path.getName(0).toString().isEmpty()) {
            return EMPTY;
        }
        final List<String> segments = new ArrayList<>();
        path.iterator().forEachRemaining(p -> segments.add(p.toString()));
        return new RelativePath(segments);
    }

    /**
     * Creates a relative path from the path segments of {@link File}. <code>file</code>
     * must not be absolute.
     *
     * @param file the file. Must not be null. Must not be absolute.
     * @return the relative path
     * @throws NullPointerException if <code>file</code> is null
     * @throws IllegalArgumentException if <code>file</code> is absolute
     */
    static public @NotNull RelativePath of(@NotNull File file) {
        Objects.requireNonNull(file);
        if (file.isAbsolute()) {
            throw new IllegalArgumentException(
                MessageFormat.format("file must not be absolute, got ''{0}''", file)
            );
        }
        return RelativePath.of(file.toPath());
    }

    /**
     * Creates a relative path from a sequence of path segments.
     *
     * @param segments a sequence of path segments, all must not be null
     * @return the relative path
     * @throws NullPointerException if one of the segments is null
     * @throws IllegalArgumentException if one of the segments contains / or \
     * @throws IllegalArgumentException if one of the segments is blank
     */
    static public @NotNull RelativePath of(String... segments) {
        if (segments.length == 0) {
            return new RelativePath(null);
        }
        if (Arrays.stream(segments).anyMatch(Objects::isNull)) {
            throw new NullPointerException("segment must not be null");
        }
        if (Arrays.stream(segments).anyMatch(s -> s.contains("\\") || s.contains("/"))) {
            throw new IllegalArgumentException("segment must neither contain '/' nor '\\'");
        }
        if (Arrays.stream(segments).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("segment must not be blank");
        }
        return new RelativePath(Arrays.stream(segments).collect(Collectors.toList()));
    }

    /**
     * Creates a relative path from a list of path segments.
     *
     * @param segments a list of path segments, all must not be null
     * @return the relative path
     * @throws NullPointerException if one of the segments is null
     * @throws IllegalArgumentException if one of the segments contains / or \
     * @throws IllegalArgumentException if one of the segments is blank
     */
    static public @NotNull RelativePath of(@NotNull final List<String> segments) {
        Objects.requireNonNull(segments);
        if (segments.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("segment must not be null");
        }
        if (segments.stream().anyMatch(s -> s.contains("\\") || s.contains("/"))) {
            throw new IllegalArgumentException("segment must neither contain '/' nor '\\'");
        }
        if (segments.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("segment must not be blank");
        }
        return new RelativePath(segments);
    }

    RelativePath(@Null List<String> segments) {
        if (segments != null) {
            this.segments.addAll(segments);
        }
    }

    /**
     * Replies true if this relative path is empty.
     *
     * @return true, if this relative path is empty
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * Replies an unmodifiable List of the segments of this relative path.
     *
     * @return an unmodifiable list of the segments of this path
     */
    public @NotNull List<String> getSegments() {
        return Collections.unmodifiableList(this.segments);
    }

    /**
     * Replies the i-th segment of the relative path
     *
     * @param i the index
     * @return the i-th segment
     */
    public @NotNull String getSegment(final int i) {
        if (i < 0 || i >= segments.size()) {
            throw new IndexOutOfBoundsException(MessageFormat.format("index {0} is out of bounds", i));
        }
        return segments.get(i);
    }

    /**
     * Replies the number of segments of this relative path, counting
     * the segments <code>.</code> and <code>..</code>, if present.
     *
     * @return the number of segments of this relative path
     */
    public int getLength() {
        return this.segments.size();
    }

    /**
     * Replies the parent path of this relative path, if it exists.
     * <p>
     * Returns {@link Optional#empty()} if this is the empty path.
     * <p>
     * The parent path of a relative path with 1 segment is the empty path.
     * <p>
     * Otherwise, the parent path consists of the first <em>n - 1</em> segments of this path, where
     * <em>n</em> is the number of segments of this path.
     *
     * @return the parent path of this relative path, if it exists.
     */
    public Optional<RelativePath> getParent() {
        if (isEmpty()) {
            return Optional.empty();
        }
        if (segments.size() == 1) {
            return Optional.of(RelativePath.EMPTY);
        }
        final var parentSegments = segments.subList(0,segments.size() - 1);
        return Optional.of(new RelativePath(parentSegments));
    }

    /**
     * Append an <code>other</code> relative path to this relative path.
     *
     * @param other the other relative path. Must not be null.
     * @return a new path where <code>other</code> is appended to this path
     * @throws NullPointerException if <code>other</code> is null
     */
    public @NotNull RelativePath append(@NotNull RelativePath other) {
        Objects.requireNonNull(other);
        List<String> segments = new ArrayList<>();
        segments.addAll(this.segments);
        segments.addAll(other.segments);
        return new RelativePath(segments);
    }

    /**
     * Append a sequence of <code>segments</code> to the path.
     *
     * @param segments the variable number of segments. Segments must not be null.
     * @return a copy of this path with the appended segments
     */
    public @NotNull RelativePath append(String... segments) {
        if (segments == null || segments.length == 0) {
            // nothing to append
            return this;
        }
        return append(RelativePath.of(segments));
    }

    /**
     * Resolves this path against another path where the other path is considered to represent a
     * directory.
     * <p>
     * This is equivalent to append this path to context and to canonicalize the result.
     * <p>
     * <strong>Examples</strong><p>
     * <ul>
     *     <li><pre>{@code
     * // a module repository in jar file is stored in the directory entry 'my/module/repo'. We want
     * // to resolve the module with id 'foo/bar/../my-module.mjs' against it.
     * var rootPath = RelativePath.parse("my/module/repo");
     * var modulePath = RelativePath.parse("foo/bar/../my-module.mjs");
     * var resolved = module.resolveAgainstDirectoryContext(sourcePath)
     * System.out.println(resolved.get());  // -> my/module/repo/foo/my-module.mjs
     *     }
     *     </pre>
     *     </li>
     * </ul>
     *
     * @param directoryContext the context
     * @return the resolved path
     * @throws NullPointerException if <code>directoryContext</code> is null
     */
    public @NotNull Optional<RelativePath> resolveAgainstDirectoryContext(@NotNull RelativePath directoryContext) {
        Objects.requireNonNull(directoryContext);
        return directoryContext.append(this).canonical();
    }

    /**
     * Resolves this path against another path where the other path is considered to represent a
     * file.
     * <p>
     * This is equivalent to append this path to <strong>the parent</strong> of the context and to canonicalize the result.
     * If the context path doesn't have a parent, this is equivalent to only canonicalize this path.
     * <p>
     * <strong>Examples</strong><p>
     * <pre>{@code
     * // a source file 'foo/bar/source.mjs' includes an import statement
     * //    import './my-module.mjs'
     * var sourcePath = RelativePath.parse("foo/bar/source.mjs");
     * var modulePath = RelativePath.parse("./my-module.mjs");
     * var resolved = module.resolveAgainstFileContext(sourcePath)
     * System.out.println(resolved.get());  // -> foo/bar/my-module.mjs
     *
     * }</pre>
     * </ul>
     *
     * @param fileContext the context
     * @return the resolved path
     * @throws NullPointerException if <code>directoryContext</code> is null
     */
    public @NotNull Optional<RelativePath> resolveAgainstFileContext(@NotNull RelativePath fileContext) {
        final var parent = fileContext.getParent();
        if (parent.isPresent()) {
            return parent.get().append(this).canonical();
        } else {
            return this.canonical();
        }
    }

    /**
     * Returns the canonical version of this relative path if canonicalization
     * is possible.
     * <p>
     * The canonical version is a relative path where
     * <ul>
     *     <li>all segments <code>.</code> are removed</li>
     *     <li>all segments <code>..</code> are resolved</li>
     * </ul>
     * Two relative paths and their canonical version:
     * <ul>
     *     <code>foo/bar/../../baz</code> -&gt; <code>baz</code>
     *     <code>foo/bar/../baz</code> -&gt; <code>foo/baz</code>
     * </ul>
     *
     * Two relative paths for which this method fails and returns an
     * empty {@link Optional}:
     * <ul>
     *     <li><code>foo/../../bar</code>- the second <code>..</code> segment can't be resolved</li>
     *     <li><code>foo/../bar/../baz/../..</code> - the fourth <code>..</code> segment can't be resolved</li>
     * </ul>
     * @return the canonical relative path
     */
    public @NotNull Optional<RelativePath> canonical() {
        final List<String> canonicalSegs = new ArrayList<>();
        for (final String s : this.segments) {
            switch (s) {
                case ".":
                    continue;
                case "..": {
                    if (canonicalSegs.isEmpty()) {
                        // can't resolve '..' against the empty path, canonicalization
                        // fails. Therefore, return an empty optional.
                        return Optional.empty();
                    }
                    canonicalSegs.remove(canonicalSegs.size() - 1);
                    break;
                }
                default:
                    canonicalSegs.add(s);
            }
        }
        return Optional.of(RelativePath.of(canonicalSegs));
    }

    /**
     * Replies true if this path start with the path <code>prefix</code>.
     *
     * @param prefix the prefix
     * @return true if this path start with the path <code>prefix</code>.
     * @throws NullPointerException if <code>prefix</code> is null
     */
    public boolean startsWith(@NotNull RelativePath prefix) {
        Objects.requireNonNull(prefix);
        if (prefix.isEmpty()) {
            return true;
        }
        if (prefix.getLength() > this.getLength()) {
            return false;
        }
        for (int i=0; i < prefix.segments.size(); i++) {
            if (! segments.get(i).equals(prefix.segments.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this relative path is equal to <code>other</code>.
     * <p>
     * Two relative paths are considered to be equal if they consist of
     * the same sequence of segments, including segments <code>'.'</code>
     * and <code>'..'</code>.
     * <p>
     * To check whether two paths are equal <strong>after canonicalization</strong>,
     * you have to {@link #canonical() canonicalize} them first.
     *
     * @param other the other path
     * @return true, if this relative path is equal to <code>other</code>
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        RelativePath that = (RelativePath) other;
        return Objects.equals(segments, that.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments);
    }

    @Override
    public String toString() {
        return String.join("/", segments);
    }

    public Path toPath() {
        return Path.of(toString());
    }
}
