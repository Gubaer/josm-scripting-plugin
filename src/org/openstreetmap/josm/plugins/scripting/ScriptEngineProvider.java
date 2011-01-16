package org.openstreetmap.josm.plugins.scripting;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractListModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.scripting.preferences.PreferenceKeys;
import org.openstreetmap.josm.plugins.scripting.preferences.ScriptEngineJarInfo;
/**
 * <p>Provides a list model for the list of available script engines.</p>
 */
public class ScriptEngineProvider extends AbstractListModel implements PreferenceKeys{
	static private final Logger logger = Logger.getLogger(ScriptEngineProvider.class.getName());
	
	static private ScriptEngineProvider instance;
	static public ScriptEngineProvider getInstance() {
		if (instance == null) {
			instance = new ScriptEngineProvider();
		}
		return instance;
	}
	
	
	private final List<ScriptEngineFactory> factories = new ArrayList<ScriptEngineFactory>();
	private final List<File> scriptEngineJars = new ArrayList<File>();
	private MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		
	protected void loadMimeTypesMap() {
		File f = new File(ScriptingPlugin.getInstance().getPluginDir(), "mime.types");
		if (f.isFile() && f.canRead()){
			try {
				mimeTypesMap = new MimetypesFileTypeMap(new FileInputStream(f));
				return;
			} catch(IOException e) {
				System.err.println(tr("Warning: failed to load mime types from file ''0''.", f));
				e.printStackTrace();
			}
		}
		
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("/resources/mime.types.default");
			if (is == null){
				System.err.println(tr("Warning: failed to load default mime types from  resource ''0''.", "/resources/mime.types.default"));
				return;
			}
			mimeTypesMap = new MimetypesFileTypeMap(is);
		} finally {
			if (is != null) {try {is.close();}catch(IOException e){}}
		}
	}
	
	protected void restoreScriptEngineUrlsFromPreferences() {
		scriptEngineJars.clear();
		Collection<String> jars = Main.pref.getCollection(PREF_KEY_SCRIPTING_ENGINE_JARS,null);
		if (jars == null) return;
		for (String jar: jars){
			jar = jar.trim();
			if (jar.isEmpty()) continue;			
			ScriptEngineJarInfo info = new ScriptEngineJarInfo(jar);
			if (!info.getStatusMessage().equals(ScriptEngineJarInfo.OK_MESSAGE)) continue;
			scriptEngineJars.add(new File(jar));
		}		
	}
	
	protected ClassLoader buildClassLoader() {
		ClassLoader cl = null;
		URL [] urls = new URL[scriptEngineJars.size()];
		for(int i=0; i < scriptEngineJars.size(); i++){
			try {
				urls[i] = scriptEngineJars.get(i).toURI().toURL();
			} catch(MalformedURLException e){
				// shouldn't happen because the entries in 'scriptEngineJars' 
				// are existing, valid files. Ignore the exception.
				e.printStackTrace();
				continue;
			}
		}
		if (urls.length > 0){
			return new URLClassLoader(
					urls,
					getClass().getClassLoader()
			);
		} else {
			return getClass().getClassLoader();
		}
	}
		
	protected void loadScriptEngineFactories() {
		try {
			ClassLoader cl = buildClassLoader();
			factories.clear();
			ScriptEngineManager manager = new ScriptEngineManager(cl);
			factories.addAll(manager.getEngineFactories());
		} catch(Throwable t){
			t.printStackTrace();
			return;
		} 
		
		Collections.sort(factories,
				new Comparator<ScriptEngineFactory>() {
					@Override
					public int compare(ScriptEngineFactory f1, ScriptEngineFactory f2) {
						return f1.getEngineName().compareTo(f2.getEngineName());
					}
				}
		);	
	}
	
	private ScriptEngineProvider(){
		restoreScriptEngineUrlsFromPreferences();
		loadScriptEngineFactories();
		loadMimeTypesMap();
		fireContentsChanged(this, 0, scriptEngineJars.size());
	}
	
	/**
	 * <p>Replies the list of jar files from which script engines are loaded.</p>
	 * 
	 * @return the list of jar files
	 */
	public List<File> getScriptEngineJars() {
		return new ArrayList<File>(scriptEngineJars);
	}
	
	/**
	 * <p>Replies a script engine by name or null, if no such script engine exists.</p>
	 * 
	 * @param name the name 
	 * @return the script engine
	 * @see ScriptEngineManager#getEngineByName(String)
	 */
	public ScriptEngine getEngineByName(String name) {
		ScriptEngineManager mgr = new ScriptEngineManager(buildClassLoader());
		return mgr.getEngineByName(name);
	}
	
	/**
	 * <p>Replies a suitable script engine for a mime type or null, if no such script engine exists.</p>
	 * 
	 * @param name the mime type 
	 * @return the script engine
	 * @see ScriptEngineManager#getEngineByMimeType(String)
	 */
	public ScriptEngine getEngineByMimeType(String mimeType) {
		ScriptEngineManager mgr = new ScriptEngineManager(buildClassLoader());
		return mgr.getEngineByMimeType(mimeType);
	}
	
	/**
	 * <p>Replies a suitable script engine for a script file, if no such script engine exists.</p>
	 * 
	 * <p>Derives a mime type from the file suffix and replies a script engine suitable for this
	 * mime type.</p>
	 * 
	 * @param scriptFile the script file
	 * @return the script engine
	 */
	public ScriptEngine getEngineForFile(File scriptFile) {
		if (scriptFile == null) return null;
		ScriptEngineManager mgr = new ScriptEngineManager(buildClassLoader());
		return mgr.getEngineByMimeType(mimeTypesMap.getContentType(scriptFile));
	}
	
	/**
	 * <p>Sets the list of jar files which provide JSR 226 compatible script
	 * engines.</p>
	 * 
	 * <p>null entries in the list are ignored. Entries which aren't 
	 * {@link ScriptEngineJarInfo#getStatusMessage() valid} are ignored.</p>
	 * 
	 * @param jars the list of jar files. Can be null to set an empty list of jar files.
	 */
	public void setScriptEngineJars(List<File> jars){
		this.scriptEngineJars.clear();
		if (jars != null){
			for (File jar: jars){
				if (jar == null) continue;
				ScriptEngineJarInfo info = new ScriptEngineJarInfo(jar.toString());
				if (! info.getStatusMessage().equals(ScriptEngineJarInfo.OK_MESSAGE)) continue;
				this.scriptEngineJars.add(jar);
			}
		}
		loadScriptEngineFactories();
		fireContentsChanged(this, 0, scriptEngineJars.size());
	}
			
	/**
	 * <p>Replies a script engine created by the i-th script engine factory.</p>
	 * 
	 * @param i the index
	 * @return the engine
	 */
	public ScriptEngine getScriptEngine(int i){
		ScriptEngine engine = factories.get(i).getScriptEngine();
		return engine;
	}

	/* ------------------------------------------------------------------------------------ */
	/* ListModel                                                                            */
	/* ------------------------------------------------------------------------------------ */
	@Override
	public Object getElementAt(int i) {
		return factories.get(i);
	}

	@Override
	public int getSize() {
		return factories.size();
	}		
}
