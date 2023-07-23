package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
/**
 * Represents a jar file which potentially provides a JSR 223 compatible scripting
 * engine.
 */
public class ScriptEngineJarInfo implements Comparable<ScriptEngineJarInfo>{
    /**
     * Replied by {@link #getStatusMessage()}, if the jar file actually exists, is
     * readable, and provides a JSR 223 compatible scripting engine.
     */
    static public final String OK_MESSAGE = "OK";

    private String jarFilePath;
    private String statusMessage = null;

    /**
     * Analyses whether the jar file is providing a JSR 223 compatible scripting engine.
     * Invoke {@link #getStatusMessage()} to retrieve the respective status message.
     */
    public void analyse(){
        File jar;
        jar = new File(jarFilePath);
        if (!jar.exists()) {
            statusMessage = tr("''{0}'' doesn''t exist.", jar);
            return;
        }
        if (! jar.isFile()) {
            statusMessage = tr("''{0}'' is a directory. Expecting a jar file instead.", jar);
            return;
        }
        if (! jar.canRead()) {
            statusMessage = tr("''{0}'' isn''t readable. Can''t load a script engine from this file.", jar);
            return;
        }
        try (JarFile jf = new JarFile(jar)){
            ZipEntry ze = jf.getEntry("META-INF/services/javax.script.ScriptEngineFactory");
            if (ze == null){
                statusMessage = tr("The jar file ''{0}'' doesn''t provide a script engine. The entry ''{1}'' is missing.", jar,"/META-INF/services/javax.script.ScriptEngineFactory");
                return;
            }
        } catch (IOException e) {
            statusMessage = tr("Failed to open file ''{0}'' as jar file. Can''t load a script engine from this file.", jar);
            return;
        }
        statusMessage = OK_MESSAGE;
    }

    /**
     * Creates a new info object for a script engine jar.
     *
     * @param fileName the jar file. Empty string assumed, if null.
     */
    public ScriptEngineJarInfo(String fileName) {
        if (fileName == null) fileName = "";
        this.jarFilePath = fileName.trim();
        analyse();
    }

    /**
     * Replies a localized status message describing the error status of this
     * scripting jar file or {@link #OK_MESSAGE} if this jar file is OK.
     *
     * @return the status message
     */
    public String getStatusMessage() {
        if (statusMessage == null) analyse();
        return statusMessage;
    }

    /**
     * Replies the full path of the jar file.
     *
     * @return the path
     */
    public String getJarFilePath() {
        return jarFilePath;
    }

    /**
     * Sets the path of the jar file.
     *
     * @param path the path. Assumes "" if null.
     */
    public void setJarFilePath(String path){
        if (path == null) path = "";
        path = path.trim();
        this.jarFilePath = path;
        analyse();
    }

    public String toString() {
        return MessageFormat.format("<scriptJarInfo for=''{0}'' />", jarFilePath);
    }

    /* ----------------------------------------------------------------------------- */
    /* interface Comparable                                                          */
    /* ----------------------------------------------------------------------------- */
    @Override
    public int compareTo(ScriptEngineJarInfo o) {
        if (o == null) return -1;
        return jarFilePath.compareTo(o.jarFilePath);
    }

    /* ----------------------------------------------------------------------------- */
    /* hashCode and equals                                                           */
    /* ----------------------------------------------------------------------------- */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((jarFilePath == null) ? 0 : jarFilePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptEngineJarInfo other = (ScriptEngineJarInfo) obj;
        if (jarFilePath == null) {
            return other.jarFilePath == null;
        } else return jarFilePath.equals(other.jarFilePath);
    }
}
