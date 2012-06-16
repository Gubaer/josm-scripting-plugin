package org.openstreetmap.josm.plugins.scripting.js;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScript;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * <p>Simple module script provider. It loads modules only from the local file system, either
 * as file or as jar file entry. It doesn't accept HTTP URLs as repository location. Caching
 * is simple too: modules are loaded exactly once and then served from memory until the JOSM
 * application is terminated.</p>
 * 
 */
public class JOSMModuleScriptProvider implements ModuleScriptProvider {
	static private final Logger logger = Logger.getLogger(JOSMModuleScriptProvider.class.getName());
	private static boolean DO_TRACE = false;
	
	static private final JOSMModuleScriptProvider instance = new JOSMModuleScriptProvider();
	static public JOSMModuleScriptProvider getInstance() {
		return instance;
	}
	
	static public String normalizeModuleId(String moduleId) {
		return moduleId.trim()
				.replace('\\', '/').replaceAll("^\\/+", "").replaceAll("\\/+$","")
				.replaceAll("\\.[jJ][sS]$", "");
	}
	
	private final List<URL> moduleRepositories = new ArrayList<URL>();

	/**
	 * Adds a repository to the list of repositories where modules are looked up.
	 * 
	 * @param repository the repository. Must not be null. Expects an URL with either file or jar protocol.  
	 * @throws IllegalArgumentException thrown if repository is null
	 * @throws IllegalArgumentException thrown if repository is neither a jar nor a file URL
	 */
	public void addRepository(URL repository) throws IllegalArgumentException {
		Assert.assertArgNotNull(repository, "repository");
		Assert.assertArg(repository.getProtocol().equals("file") || repository.getProtocol().equals("jar"), "Expected a file:/ or a jar:/ URL, got {0}", repository);
		synchronized(moduleRepositories) {
			if (! moduleRepositories.contains(repository)) {
				moduleRepositories.add(repository);
			}
		}
	}
	
	/** the cache of compiled modules */
	private final Map<String, ModuleScript> cache = new HashMap<String, ModuleScript>(); 

	
	protected void trace(String msg, Object...args) {
		if (!DO_TRACE) return;
		System.out.println("require: " + MessageFormat.format(msg, args));
	}
	
	protected void warning(String msg, Object...args) {
		logger.log(Level.WARNING, "require: " + MessageFormat.format(msg, args));
	}
	
	protected void warning(Throwable t, String msg, Object...args) {
		logger.log(Level.WARNING, "require: " + MessageFormat.format(msg, args),t);
	}

	
	
	
	protected URL lookupInDirectory(URL fileUrl, String moduleId) {
		if (!fileUrl.getProtocol().equals("file")) return null;
		File dir = new File(fileUrl.getFile());
		trace("''{0}'': Looking up in directory <{1}>", moduleId, dir.toString());

		if (dir == null || !dir.isDirectory()) {
			trace("''{0}'': <{1}> isn''t a directory. Failed to lookup module.", moduleId, dir.toString());
			return null;
		}
		File candidate;
		candidate = new File(dir, moduleId);
		if (! candidate.isFile() || ! candidate.canRead()) {
			candidate = new File(dir, moduleId + ".js");
			if (!candidate.isFile() || !candidate.canRead()) {
				trace("''{0}'': MISS - didn''t find <{1}> or <{1}.js> in directory <{2}>. Failed to lookup module.", moduleId, moduleId, dir.toString());
				return null;
			}
		}
		try {
			trace("''{0}'': HIT - found in <{1}>", moduleId, candidate.toString());
			return candidate.toURI().toURL();
		} catch(MalformedURLException e){
			warning(e, "''{0}'': Failed to convert file <{1}> to URL.", moduleId, candidate.toString());
			return null;
		}
	}
	
	protected URL lookupInJar(URL jarUrl, String moduleId) {
		if (jarUrl == null || ! jarUrl.getProtocol().equals("jar")) return null;
		trace("''{0}'': Looking up in jar ''{1}''", moduleId, jarUrl);
		URL fileUrl;
		try {
			fileUrl = new URL(jarUrl.getPath());
		} catch(MalformedURLException e){
			warning(e, "Failed to create URL for jar URL path ''{0}''. Failed to lookup module ''{1}'' in this jar.", jarUrl.getPath(), moduleId);
			return null;
		}
		if (!fileUrl.getProtocol().equals("file")) return null;
		String[] parts = fileUrl.toString().split("!");
		if (parts.length != 2 || !parts[0].toLowerCase().startsWith("file:")) {
			warning("''{0}'': Unexpected format of jar url, got <{1}>", moduleId, fileUrl);
		}
		parts[0] = parts[0].substring(5).replaceAll("^[\\\\\\/]+", "/");
		File jarFile = new File(parts[0]); 
		String jarPath = parts[1];
		if (!jarFile.exists() || !jarFile.canRead()) {
			trace("''{0}'': jar lookup failed: jar file ''{1}'' doesn''t exist.", moduleId, jarFile.toString());
			return null;
		}
		
		jarPath = jarPath.replace('\\', '/').replaceAll("^\\/+", "").replaceAll("\\/+$", "");
		JarFile jf = null;
		try {
			try {
				jf = new JarFile(jarFile);
			} catch(IOException e){
				warning(e, "Failed to create JarFile for file''{0}''. Failed to lookup module ''{1}'' in this jar.", jarFile.getPath(), moduleId);
				return null;
			}
			JarEntry eDir = jf.getJarEntry(jarPath + "/" + moduleId + "/"); 
			JarEntry eNoSuffix = jf.getJarEntry(jarPath + "/" + moduleId);
			JarEntry eWithSuffix = jf.getJarEntry(jarPath + "/" + moduleId + ".js");
			JarEntry eFound = null;
			if (eWithSuffix != null) {
				eFound = eWithSuffix;						
			} else if (eNoSuffix != null && eDir == null) {
				eFound = eNoSuffix;
			}
			if (eFound == null) {
				trace("''{0}'': MISS - didn''t find either <{1}> or <{1}.js> in jar file <{2}>", moduleId, jarPath + "/" + moduleId, jf.getName());
				return null;
			} else {
				trace("''{0}'': HIT - found in entry <{1}> of jar  <{2}>", moduleId, eFound.getName(), jf.getName());
				String moduleUrl = "jar:" + jarFile.toURI().toString() + "!/" + eFound.getName();
				try {					
					return new URL(moduleUrl);
				} catch(MalformedURLException e) {
					warning(e, "Failed to create URL for ''{0}''. Failed to lookup module ''{1}''.", moduleUrl, moduleId);
				}
			}
			return null;
		} finally {
			if (jf != null) try {jf.close();} catch (IOException e) {/* ignore */}
		}
	}
	
	protected ModuleScript load(URL module, URL base) throws IOException, URISyntaxException{
		Reader reader = null;				
        try {
			reader = new InputStreamReader(
				module.openStream(),
				"UTF8"
			);
            Script script = Context.getCurrentContext().compileReader(reader, module.toString(),1,null);     
            return new ModuleScript(script, module.toURI(), base == null ? null : base.toURI());
        } catch(UnsupportedEncodingException e) {
        	// should not happen -  just in case 
        	e.printStackTrace();
        } finally {
            if (reader != null) reader.close();
        }
        return null;
	}
	
	public URL lookup(String moduleId) {
		moduleId = normalizeModuleId(moduleId);
		for(URL base: moduleRepositories) {
			URL url = null;
			if (base.getProtocol().equals("file")) {
				url = lookupInDirectory(base, moduleId);
			} else if (base.getProtocol().equals("jar")) {
				url =  lookupInJar(base, moduleId);
			} 
			if (url != null) return url;
		}
		return null;
	}
	
	public synchronized ModuleScript getModuleScript(Context cx, String moduleId) throws Exception {
		moduleId = normalizeModuleId(moduleId);
		if (cache.containsKey(moduleId)) return cache.get(moduleId);
		URL url = lookup(moduleId);
		if (url == null) return null;
		ModuleScript script = load(url,null);
		if (script == null) return null;
		cache.put(moduleId, script);
		return script;
	}
	
	@Override
	public  ModuleScript getModuleScript(Context cx, String moduleId, URI moduleUri, Scriptable paths) throws Exception {
		// moduleUri and paths are ignored 
		//
		return getModuleScript(cx, moduleId);		
	}
}