package org.openstreetmap.josm.plugins.scripting.model;

import static org.junit.Assert.*;
import org.junit.*;
import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType;

class ScriptEngineTypeTest {

	@Test
	public void fromLegalPreferenceValue() {
		def type = ScriptEngineType.fromPreferencesValue("embedded/rhino");
		assert type == ScriptEngineType.EMBEDDED;
		
		type = ScriptEngineType.fromPreferencesValue("plugged/rhino");
		assert type == ScriptEngineType.PLUGGED;

		type = ScriptEngineType.fromPreferencesValue("embedded");
		assert type == ScriptEngineType.EMBEDDED;
		
		type = ScriptEngineType.fromPreferencesValue("plugged");
		assert type == ScriptEngineType.PLUGGED;
	}
	
	@Test
	public void fromNormalizedPreferenceValue() {
		def type = ScriptEngineType.fromPreferencesValue("  embedded/rhino  ");
		assert type == ScriptEngineType.EMBEDDED;
		
		type = ScriptEngineType.fromPreferencesValue("  emBeDded/rhino  ");
		assert type == ScriptEngineType.EMBEDDED;

		
		type = ScriptEngineType.fromPreferencesValue("\t plugged/rhino  \t");
		assert type == ScriptEngineType.PLUGGED;

		type = ScriptEngineType.fromPreferencesValue("   embedded   ");
		assert type == ScriptEngineType.EMBEDDED;
		
		type = ScriptEngineType.fromPreferencesValue("   plugged  ");
		assert type == ScriptEngineType.PLUGGED;
	}
}
