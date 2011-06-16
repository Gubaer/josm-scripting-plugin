package org.openstreetmap.josm.plugins.scripting.preferences;

import java.io.File;
import java.text.MessageFormat;
import java.util.UUID;

import javax.swing.KeyStroke;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * <strong>ScriptDescriptor</strong> represents a <em>managed</em> script in JOSM, i.e.
 * a script which we can assign a keyboard shortcuts, which we can list in the scripts menu and 
 * for which we can place an icon on the toolbar. 
 * 
 */
@XmlType(name = "scriptType")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptDescriptor {
	
	private static class FileAdapter extends XmlAdapter<String, File> {
		@Override
		public File unmarshal(String v) throws Exception {
			return new File(v);
		}

		@Override
		public String marshal(File v) throws Exception {
			if (v == null) return null;
			return v.getAbsolutePath();
		}		
	}	
	
	private static class KeyStrokeAdapter extends XmlAdapter<String, KeyStroke> {
		@Override
		public KeyStroke unmarshal(String v) throws Exception {
			KeyStroke ks = KeyStroke.getKeyStroke(v);
			if (ks == null){
				throw new Exception(
						MessageFormat.format("Failed to unmarshal key stroke from ''{0}''", v)
				);
			}
			return ks;
		}

		@Override
		public String marshal(KeyStroke v) throws Exception {
			if (v == null) return null;
			return v.toString();
		}		
	}	
	
	private static class IdAdapter extends XmlAdapter<String, String> {
		@Override
		public String unmarshal(String v) throws Exception {
			v = v.trim();
			if (v.isEmpty()){
				throw new Exception(
						MessageFormat.format("Attribute ''{0}'' must not be empty or consist of white space only", "id")
				);
			}
			return v;
		}

		@Override
		public String marshal(String v) throws Exception {
			return v.trim();
		}		
	}

	@XmlAttribute(name="id",required=true)
	@XmlJavaTypeAdapter(IdAdapter.class)
	@XmlID
	/**
	 * Every script descriptor has a unique ID, per default an UUID. This uniquely
	 * identifies a scripts independet of file or directory names. A unique ID is 
	 * very helpful to match updates (new script versions, new meta data) against 
	 * already installed scripts. 
	 */
	private String id = UUID.randomUUID().toString();
		
	@XmlAttribute(name="displayName", required=false)
	private String displayName;
	
	@XmlAttribute(name="file", required=true)
	@XmlJavaTypeAdapter(FileAdapter.class)
	private File scriptFile;
	
	@XmlAttribute(name="engine", required=false)
	private String scriptEngineName = null;

	@XmlAttribute(name="shortcut", required=false)	
	@XmlJavaTypeAdapter(KeyStrokeAdapter.class)
	private KeyStroke shortCut;

	@XmlAttribute(name="overrideShortcut", required=false)	
	private boolean shortCutOverride = false;

	/**
	 * Replies the script id. 
	 * 
	 * @return the script id 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id of the script. Leading and trailing white space is removed.
	 * 
	 * @param id the id. Must not be null. Must not consist of white space only.
	 * 
	 * @throws IllegalArgumentException thrown if id is null or if it consists of white space only
	 */
	public void setId(String id) throws IllegalArgumentException {
		Assert.assertArgNotNull(id, "id");
		Assert.assertArg(!id.trim().isEmpty(), "id must not consist of white space only. Got ''{0}''", id);
		this.id = id.trim();
	}

	/**
	 * Replies the display name. Can be null, if no display name is defined.
	 * 
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name. null, to set no display name.
	 * 
	 * @param displayName 
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Replies the script file. 
	 * @return
	 */
	public File getScriptFile() {
		return scriptFile;
	}

	/**
	 * Sets the script file.
	 * 
	 * @param scriptFile the script file. Must not be null. Must be file, not a directory.
	 * @throws IllegalArgumentException thrown if <code>scriptFile</code> is null
	 * @throws IllegalArgumentException thrown if {@code scriptFile} is a directory 
	 */
	public void setScriptFile(File scriptFile) throws IllegalArgumentException {
		Assert.assertArgNotNull(scriptFile, "scriptFile");
		Assert.assertArg(scriptFile.isFile(), "Expected a file, got a directory. Got ''{0}''", scriptFile.toString());		
		this.scriptFile = scriptFile;
	}

	/**
	 * Replies the name of the script engine to use. May be null, if the script engine isn't
	 * explicitly set.
	 * 
	 * @return the name of the script engine 
	 */
	public String getScriptEngineName() {
		return scriptEngineName;
	}

	/**
	 * Sets the name of the script engine to use. 
	 * 
	 * @param scriptEngineName the name of the script engine. Can be null.
	 */
	public void setScriptEngineName(String scriptEngineName) {
		this.scriptEngineName = scriptEngineName;
	}

	/**
	 * Replies the keyboard shortcut for this script. Can be null, if no
	 * keyboard shortcut is defined.
	 * 
	 * @return the keyboard shortcut 
	 */
	public KeyStroke getShortCut() {
		return shortCut;
	}

	/**
	 * Sets the keyboard shortcut for this script. 
	 * 
	 * @param shortCut the shortcut. Can be null.
	 */
	public void setShortCut(KeyStroke shortCut) {
		this.shortCut = shortCut;
	}

	/**
	 * Replies true, if the keyboard shortcut shall override an already installed shortcut
	 * in JOSM. False, otherwise.
	 * 
	 * @return true, if the keyboard shortcut shall override an already installed shortcut
	 * in JOSM. False, otherwise.
	 */
	public boolean isShortCutOverride() {
		return shortCutOverride;
	}

	/**
	 * Sets whether the keyboard shortcut shall override an already installed shortcut
	 * in JOSM.
	 * 
	 * @param shortCutOverride true, if the keyboard shortcut shall override an already installed shortcut
	 * in JOSM. False, otherwise.
	 */
	public void setShortCutOverride(boolean shortCutOverride) {
		this.shortCutOverride = shortCutOverride;
	}
}
