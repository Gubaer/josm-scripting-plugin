package org.openstreetmap.josm.plugins.scripting.preferences.graalvm

import org.junit.jupiter.api.Test
import org.openstreetmap.josm.data.Preferences
import org.openstreetmap.josm.plugins.scripting.JOSMFixtureBasedTest

import static org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys.*
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.DefaultAccessPolicy.ALLOW_ALL
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.TernaryAccessPolicy.*

class GraalVMPrivilegesModelTest extends JOSMFixtureBasedTest {

    @Test
    void "should init default preferences"() {
        def model = new GraalVMPrivilegesModel()

        assertNotNull(model.getDefaultAccessPolicy())
        assertNotNull(model.getCreateProcessPolicy())
        assertNotNull(model.getCreateThreadPolicy())
        assertNotNull(model.getHostClassLoadingPolicy())
        assertNotNull(model.getIOPolicy())
        assertNotNull(model.getUseExperimentalOptionsPolicy())
    }

    @Test
    void "should properly init default access policy from preferences"() {

        def model = new GraalVMPrivilegesModel()
        // fall back to default value for a missing configuration value
        assertEquals(ALLOW_ALL, model.getDefaultAccessPolicy())
    }

    @Test
    void "should properly init the create-process policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_CREATE_PROCESS_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateThreadPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_CREATE_PROCESS_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getCreateProcessPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_CREATE_PROCESS_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getCreateProcessPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_CREATE_PROCESS_POLICY, "an unsupported value")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateProcessPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateProcessPolicy())
    }

    @Test
    void "should properly init the create-thread policy from preferences"() {
        Preferences prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_CREATE_THREAD_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateThreadPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_CREATE_THREAD_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getCreateThreadPolicy())

        // accept supported configuration value "deny"
        prefs.put(GRAALVM_CREATE_THREAD_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getCreateThreadPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_CREATE_THREAD_POLICY, "an unsupported value")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateThreadPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getCreateThreadPolicy())
    }

    @Test
    void "should properly init the host-class-loading policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_HOST_CLASS_LOADING_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getHostClassLoadingPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_HOST_CLASS_LOADING_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getHostClassLoadingPolicy())

        // accept supported configuration value "deny"
        prefs.put(GRAALVM_HOST_CLASS_LOADING_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getHostClassLoadingPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_HOST_CLASS_LOADING_POLICY, "an unsupported value")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getHostClassLoadingPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getHostClassLoadingPolicy())
    }

    @Test
    void "should properly init the io policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_IO_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getIOPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_IO_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getIOPolicy())

        // accept supported configuration value "deny"
        prefs.put(GRAALVM_IO_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getIOPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_IO_POLICY, "an unsupported value")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getIOPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getIOPolicy())
    }

    @Test
    void "should properly init the native-access policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_NATIVE_ACCESS_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getNativeAccessPolicy())

        // accept supported configuration value "deny-all"
        prefs.put(GRAALVM_NATIVE_ACCESS_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getNativeAccessPolicy())

        // accept supported configuration value "deny"
        prefs.put(GRAALVM_NATIVE_ACCESS_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getNativeAccessPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_NATIVE_ACCESS_POLICY, "unsupported")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getNativeAccessPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getNativeAccessPolicy())
    }

    @Test
    void "should properly init the use-experimental-options policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getUseExperimentalOptionsPolicy())

        // accept supported configuration value "allow"
        prefs.put(GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY, "allow")
        model.initFromPreferences(prefs)
        assertEquals(ALLOW, model.getUseExperimentalOptionsPolicy())

        // accept supported configuration value "deny"
        prefs.put(GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY, "deny")
        model.initFromPreferences(prefs)
        assertEquals(DENY, model.getUseExperimentalOptionsPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY, "unsupported")
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getUseExperimentalOptionsPolicy())


        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(DERIVE, model.getUseExperimentalOptionsPolicy())
    }

    @Test
    void "should properly init the environment-access-policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "derive"
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "derive")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.EnvironmentAccessPolicy.DERIVE,
            model.getEnvironmentAccessPolicy())

        // accept supported configuration value "none"
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "none")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.EnvironmentAccessPolicy.NONE,
            model.getEnvironmentAccessPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "unsupported-value")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.EnvironmentAccessPolicy.DERIVE,
            model.getEnvironmentAccessPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.EnvironmentAccessPolicy.DERIVE,
            model.getEnvironmentAccessPolicy())

        // deny access if default access is deny-all
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "derive")
        model.setDefaultAccessPolicy(GraalVMPrivilegesModel.DefaultAccessPolicy.DENY_ALL)
        model.initFromPreferences(prefs)
        assertFalse(model.allowEnvironmentAccess())

        // allow access if default access is allow-all
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "derive")
        model.initFromPreferences(prefs)
        model.setDefaultAccessPolicy(ALLOW_ALL)
        assertTrue(model.allowEnvironmentAccess())

        // deny access if environment access policy is 'none'
        prefs.put(GRAALVM_ENVIRONMENT_ACCESS_POLICY, "none")
        model.initFromPreferences(prefs)
        assertFalse(model.allowEnvironmentAccess())
    }

    @Test
    void "should properly init the host-access-policy from preferences"() {
        def prefs = new Preferences()
        def model = new GraalVMPrivilegesModel()

        // accept supported configuration value "all"
        prefs.put(GRAALVM_HOST_ACCESS_POLICY, "all")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.HostAccessPolicy.ALL,
            model.getHostAccessPolicy())

        // accept supported configuration value "explicit"
        prefs.put(GRAALVM_HOST_ACCESS_POLICY, "explicit")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.HostAccessPolicy.EXPLICIT,
            model.getHostAccessPolicy())

        // accept supported configuration value "none"
        prefs.put(GRAALVM_HOST_ACCESS_POLICY, "none")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.HostAccessPolicy.NONE,
            model.getHostAccessPolicy())

        // fall back to default value for an unsupported configuration value
        prefs.put(GRAALVM_HOST_ACCESS_POLICY, "unsupported-value")
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.HostAccessPolicy.getDefault(),
                model.getHostAccessPolicy())

        // fall back to default value for a missing configuration value
        prefs = new Preferences()
        model.initFromPreferences(prefs)
        assertEquals(GraalVMPrivilegesModel.HostAccessPolicy.getDefault(),
                model.getHostAccessPolicy())

    }
}
