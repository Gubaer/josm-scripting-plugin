package org.openstreetmap.josm.plugins.scripting.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
/**
 * <p>Represents a jar file which potentially provides a JSR 223 compatible scripting
 * engine.</p>
 */
public class ScriptEngineJarInfo implements Comparable<ScriptEngineJarInfo>{
	/**
	 * Replied by {@link #getStatusMessage()}, if the jar file actually exists, is
	 * readable, and provides a JSR 223 compatible scripting engine.
	 */
	static public final String OK_MESSAGE = "OK";
	
	private String jarFileName;
	private String statusMessage = null;
	
	/**
	 * <p>Analyses whether the jar file is providing a JSR 223 compatible scripting engine.
	 * Invoke {@link #getStatusMessage()} to retrieve the respective status message.</p>
	 */
	public void analyse(){
		File jar = null;
		jar = new File(jarFileName);		
		if (!jar.exists()) {
			statusMessage = tr("''{0}'' doesn''t exist.", jar);
			return;
		}
		if (! jar.isFile()) {
			System.out.println("jar="+jar);
			statusMessage = tr("''{0}'' is a directory. Expecting a jar file instead.", jar);
			return;
		}
		if (! jar.canRead()) {
			statusMessage = tr("''{0}'' isn't readable. Can''t load a script engine from this file.", jar);
			return;
		}
		JarFile jf = null;
		try {
			jf = new JarFile(jar);
		} catch (IOException e) {
			statusMessage = tr("Failed to open file ''{0}'' as jar file. Can''t load a script engine from this file", jar);
			return;
		}
		ZipEntry ze = jf.getEntry("META-INF/services/javax.script.ScriptEngineFactory");
		if (ze == null){
			statusMessage = tr("The jar file ''{0}'' doesn''t provide a script engine. The entry ''{1}'' is missing.", jar,"/META-INF/services/javax.script.ScriptEngineFactory");
			return;
		}
		statusMessage = OK_MESSAGE;
	}
	
	/**
	 * <p>Creates a new info object for a script engine jar.</p>
	 * 
	 * @param jar the jar file. Empty string assumed, if null.
	 */
	public ScriptEngineJarInfo(String fileName) {
		if (fileName == null) fileName = "";
		this.jarFileName = fileName.trim();
		analyse();
	}
		
	/**
	 * <p>Replies a localized status message describing the error status of this
	 * scripting jar file or {@link #OK_MESSAGE} if this jar file is OK.</p>
	 *   
	 * @return the status message 
	 * @throws IOException 
	 */
	public String getStatusMessage() {
		if (statusMessage == null) analyse();
		return statusMessage;		
	}
	
	/**
	 * <p>Replies the full path of the jar file.</p>
	 * @return the path
	 */
	public String getJarFilePath() {
		return jarFileName;
	}
	
	/**
	 * <p>Sets the path of the jar file.</p>
	 * 
	 * @param path the path. Assumes "" if null.
	 */
	public void setJarFilePath(String path){
		if (path == null) path = "";
		path = path.trim();
		this.jarFileName = path;
		analyse();
	}
	
	public String toString() {
		return MessageFormat.format("<scriptJarInfo for=''{0}'' />", jarFileName);
	}

	/* ----------------------------------------------------------------------------- */
	/* interface Comparable                                                          */                    
	/* ----------------------------------------------------------------------------- */
	@Override
	public int compareTo(ScriptEngineJarInfo o) {
		if (o == null) return -1;
		return jarFileName.compareTo(o.jarFileName);
	}

	/* ----------------------------------------------------------------------------- */
	/* hashCode and equals                                                           */                    
	/* ----------------------------------------------------------------------------- */	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((jarFileName == null) ? 0 : jarFileName.hashCode());
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
		if (jarFileName == null) {
			if (other.jarFileName != null)
				return false;
		} else if (!jarFileName.equals(other.jarFileName))
			return false;
		return true;
	}
}
