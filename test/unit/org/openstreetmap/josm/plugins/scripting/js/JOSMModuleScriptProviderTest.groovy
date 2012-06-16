package org.openstreetmap.josm.plugins.scripting.js;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.LogManager;

import org.junit.*
import org.openstreetmap.josm.plugins.scripting.util.Assert;

class JOSMModuleScriptProviderTest {
	
	@BeforeClass
	static public void init() {
		String home = System.getenv("JOSM_SCRIPTING_PLUGIN_HOME");
		if (home == null) {
			fail("Environment variable JOSM_SCRIPTING_PLUGIN_HOME not set. Check env.sh");
		}
		
		JOSMModuleScriptProvider.getInstance().addRepository(
			new File(new File(home), "test/data/require/modules").toURI().toURL()
		);	
	
		def jarfile = new File(new File(home), "test/data/require/jarmodules.jar").toURI().toURL().toString();
		JOSMModuleScriptProvider.getInstance().addRepository(			
			new URL("jar:" + jarfile + "!/modules")
		);
	}
	
	def provider;
	
	@Before
	public void setUp() {
		provider = JOSMModuleScriptProvider.getInstance();	
	}
	
	@Test
	public void lookupExistingModule() {
		def url = provider.lookup("module1");
		assert url != null
		
		// various module names which are normalized 
		
		url = provider.lookup("   module1");
		assert url != null;
		
		url = provider.lookup("module1  ");
		assert url != null;
		
		url = provider.lookup("//module1//");
		assert url != null;

		url = provider.lookup("\\module1//");
		assert url != null;
	}
	
	@Test
	public void lookupExistingSubModel() {
		assert provider.lookup("sub/module3");
		assert provider.lookup("sub\\module3")
		assert provider.lookup(" sub/module3")
		assert provider.lookup(" \\\\sub/module3//   ")
		assert provider.lookup("sub/module4")		
	}
	
	@Test
	public void lookupExistingModuleInJar() {
		assert provider.lookup("module10")
		assert provider.lookup(" module10  ")
		assert provider.lookup("//module10  ")
		assert provider.lookup("\\\\module10  ")
		
		assert provider.lookup("module11")
	}
	
	@Test
	public void lookupExistingSubModuleInJar() {
		assert provider.lookup("sub/module12");
		assert provider.lookup("sub\\module12")
		assert provider.lookup(" sub/module12")
		assert provider.lookup(" \\\\sub/module12//   ")
		assert provider.lookup("sub/module13")			
	}	
	
	@Test
	public void lookupAModuleWithIdenticalDirectoryName() {
		// the jar contains a directory 'josm' and a module 'josm.js'.
		// The module should be found despite the directory with
		// the same name.
		assert provider.lookup("josm");
	}
	
}
