package org.openstreetmap.josm.plugins.scripting.model;

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
import org.openstreetmap.josm.plugins.scripting.ScriptingPlugin;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType;
import org.openstreetmap.josm.plugins.scripting.preferences.ScriptEngineJarInfo;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.plugins.scripting.util.IOUtil;
/**
 * <p>Provides a list model for the list of available script engines.</p>
 * 
 */
public class JSR223ScriptEngineProvider extends AbstractListModel implements PreferenceKeys {
	
	/**
	 * The list of default mime types, mapping file suffixes to content mime types, provided
	 * as resource in the jar.
	 */
	static public final String DEFAULT_MIME_TYPES = "/resources/mime.types.default";
	
	static private final Logger logger = Logger.getLogger(JSR223ScriptEngineProvider.class.getName());
	
	static private JSR223ScriptEngineProvider instance;
	
	/**
	 * Replies the unique instance
	 * @return the unique instance
	 */
	static public JSR223ScriptEngineProvider getInstance() {
		if (instance == null) {
			instance = new JSR223ScriptEngineProvider();
		}
		return instance;
	}
	
	private final List<ScriptEngineFactory> factories = new ArrayList<ScriptEngineFactory>();
	private final List<File> scriptEngineJars = new ArrayList<File>();
	private MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
	private ClassLoader scriptClassLoader = getClass().getClassLoader();
	private ScriptEngineManager manager = null;
	
	protected ScriptEngineManager getScriptEngineManager() {
		if (manager == null){
			manager = new ScriptEngineManager(scriptClassLoader);
		}
		return manager;
	}
		
	protected void loadMimeTypesMap() {
		// skip loading mime types if we aren't running in the context of a plugin
		// instance
		if (ScriptingPlugin.getInstance() != null)  {
			String dir = ScriptingPlugin.getInstance().getPluginDir();
			if (dir == null){
				System.err.println(tr("Warning: no plugin directory is configured."));
				return;
			}
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
		}
		
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(DEFAULT_MIME_TYPES);
			if (is == null){
				System.err.println(tr("Warning: failed to load default mime types from  resource ''0''.", DEFAULT_MIME_TYPES));
				return;
			}
			mimeTypesMap = new MimetypesFileTypeMap(is);
		} finally {
			IOUtil.close(is);
		}
	}
	
	protected void restoreScriptEngineUrlsFromPreferences() {
		scriptEngineJars.clear();
		if (Main.pref != null) {
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
		buildClassLoader();
	}
	
	protected void buildClassLoader() {
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
			scriptClassLoader = new URLClassLoader(
					urls,
					getClass().getClassLoader()
			);
		} else {
			scriptClassLoader = getClass().getClassLoader();
		}
	}
		
	protected void loadScriptEngineFactories() {
		Assert.assertNotNull(scriptClassLoader, "expected scriptClassLoader != null");
		factories.clear();
		ScriptEngineManager manager = new ScriptEngineManager(scriptClassLoader);
		factories.addAll(manager.getEngineFactories());
		
		Collections.sort(factories,
				new Comparator<ScriptEngineFactory>() {
					@Override
					public int compare(ScriptEngineFactory f1, ScriptEngineFactory f2) {
						return f1.getEngineName().compareTo(f2.getEngineName());
					}
				}
		);	
	}
	
	private JSR223ScriptEngineProvider(){
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
	 * @param name the name. Must not be null.
	 * @return the script engine
	 * @see ScriptEngineManager#getEngineByName(String)
	 */
	public ScriptEngine getEngineByName(String name) {
		Assert.assertArgNotNull(name, "name");
		return getScriptEngineManager().getEngineByName(name);
	}
	
	/**
	 * <p>Replies true, if a JSR223-compatible scripting engine with name <code>name</code> is
	 * currently available.</p>
	 * 
	 * @param name the name. Must not be null.
	 * @return true, if a JSR223-compatible scripting engine with name <code>name</code> is
	 * currently available; false, otherwise 
	 */
	public boolean hasEngineWithName(String name){
		Assert.assertArgNotNull(name, "name");
		return getEngineByName(name) != null;		
	}
	
	/**
	 * <p>Replies a suitable script engine for a mime type or null, if no such script engine exists.</p>
	 * 
	 * @param name the mime type 
	 * @return the script engine
	 * @see ScriptEngineManager#getEngineByMimeType(String)
	 */
	public ScriptEngine getEngineByMimeType(String mimeType) {
		return getScriptEngineManager().getEngineByMimeType(mimeType);
	}
	
	/**
	 * <p>Derives a mime type from the file suffix and replies a script engine descriptor suitable for this
	 * mime type.</p>
	 * 
	 * @param scriptFile the script file
	 * @return the script engine descriptor. null, if no suitable script engine is available
	 */
	public ScriptEngineDescriptor getEngineForFile(File scriptFile) {
		if (scriptFile == null) return null;
		
		String mimeType = getContentTypeForFile(scriptFile);
		
		for (ScriptEngineFactory factory: getScriptEngineManager().getEngineFactories()) {
			if (factory.getMimeTypes().contains(mimeType)) {
				return new ScriptEngineDescriptor(factory);
			}
		}
		return null;
	}
	
	
	/**
	 * Replies the content type for file {@code scriptFile}.
	 * 
	 * @param scriptFile the file. Must not be null.
	 * @return the content type 
	 * @throws IllegalArgumentException thrown if <code>scriptFile</code> is null
	 */
	public String getContentTypeForFile(File scriptFile) throws IllegalArgumentException {
		Assert.assertArgNotNull(scriptFile, "scriptFile");
		return mimeTypesMap.getContentType(scriptFile);
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
		buildClassLoader();
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
	
	/**
	 * <p>Replies a script engine for the first first script engine factory whose name matches with
	 * the name in the descriptor <code>desc</code>, or null, if no such scripting engine is found.
	 * 
	 * @param desc the descriptor. Must not be null. It's type must be {@link ScriptEngineType#PLUGGED}
	 * @return the script engine or null
	 */
	public ScriptEngine getScriptEngine(ScriptEngineDescriptor desc) {
		Assert.assertArgNotNull(desc, "desc");
		Assert.assertArg(desc.getEngineType().equals(ScriptEngineType.PLUGGED), "Expected a descriptor for a plugged script engine, got ''{0}''", desc);
		for (ScriptEngineFactory factory: factories) {
			if (desc.getEngineId().equals(factory.getNames().get(0))) {
				return factory.getScriptEngine();
			}
		}
		return null;
	}
	
	/**
	 * <p>Replies the first script engine factory with name {@code name}, or null,
	 * if no such factory exists. Replies null, if {@code name} is null.</p>
	 * 
	 * <p>A script engine factory is matching with <code>name</code> if its 
	 * {@link ScriptEngineFactory#getEngineName() engine name} or one of its
	 * {@link ScriptEngineFactory#getNames() short names} is equal to
	 * <code>name</code>.</p>
	 * 
	 * @param name the name
	 * @return the script engine factory
	 */
	public ScriptEngineFactory getScriptFactoryByName(String name){
		if (name == null) return null;		
		for (ScriptEngineFactory factory: factories) {
			if (factory.getEngineName().equals(name)) return factory;
			for (String n: factory.getNames()) {
				if (n.equals(name)) return factory;
			}
		}
		return null;
	}
	
	/**
	 * <p>Replies a list of the available script engine factories.</p>
	 * 
	 * @return the factories
	 */
	public List<ScriptEngineFactory> getScriptEngineFactories() {
		return new ArrayList<ScriptEngineFactory>(factories);
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
