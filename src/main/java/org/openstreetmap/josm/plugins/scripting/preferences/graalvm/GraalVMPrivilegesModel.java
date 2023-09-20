package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;


import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.spi.preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;
import org.openstreetmap.josm.spi.preferences.Setting;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys.*;
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.DefaultAccessPolicy.ALLOW_ALL;
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.TernaryAccessPolicy.DERIVE;

@SuppressWarnings("unused")
//TODO(karl): implement interactive editor for this model and add it to the preferences editor
public class GraalVMPrivilegesModel implements PreferenceChangedListener {

    static public final Logger logger =
            Logger.getLogger(GraalVMPrivilegesModel.class.getName());

    static private GraalVMPrivilegesModel instance = null;

    static public GraalVMPrivilegesModel getInstance() {
        if (instance == null) {
            instance = new GraalVMPrivilegesModel();
            instance.initFromPreferences(Preferences.main());
            //TODO(Gubaer): Register as preferences listener
        }
        return instance;
    }

    public GraalVMPrivilegesModel() {
        resetToDefaults();
    }

    /**
     *
     * @see org.graalvm.polyglot.Context.Builder#allowAllAccess(boolean)
     */
    public enum DefaultAccessPolicy {
        ALLOW_ALL,
        DENY_ALL;

        public String toPreferenceValue() {
            switch(this) {
                case ALLOW_ALL: return "allow-all";
                case DENY_ALL: return "deny-all";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public DefaultAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return DENY_ALL;
            switch(value.toLowerCase().trim()) {
                case "allow-all": return ALLOW_ALL;
                case "deny-all": return DENY_ALL;
                default:
                    final String message =
                        "unsupported preference value for default access policy. "
                      + "Using 'deny-all' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return DENY_ALL;
            }
        }
    }

    public enum TernaryAccessPolicy {
        /** explicitly grant the privilege */
        ALLOW,
        /** explicitly deny the privilege */
        DENY,
        /** derive the privilege from the default access policy
         * @see DefaultAccessPolicy
         */
        DERIVE;

        public String toPreferenceValue() {
            switch(this) {
                case ALLOW: return "allow";
                case DENY: return "deny";
                case DERIVE: return "derive";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public TernaryAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return DERIVE;
            switch(value.toLowerCase().trim()) {
                case "allow": return ALLOW;
                case "deny": return DENY;
                case "derive":
                case "":
                    return DERIVE;
                default:
                    final String message =
                        "unsupported preference value for ternary access policy. "
                      + "Using 'derive' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return DERIVE;
            }
        }
    }

    public enum EnvironmentAccessPolicy {
        /** deny access to environment variables */
        NONE,
        /** derive the privilege from the default access policy
         * @see DefaultAccessPolicy
         */
        DERIVE;

        public static EnvironmentAccessPolicy getDefault() {
            return DERIVE;
        }

        public String toPreferenceValue() {
            switch(this) {
                case NONE: return "none";
                case DERIVE: return "derive";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public EnvironmentAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return DERIVE;

            switch(value.toLowerCase().trim()) {
                case "none": return NONE;
                case "derive": return DERIVE;
                default:
                    final String message =
                        "unsupported preference value for environment access policy. "
                      + "Using 'derive' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return getDefault();
            }
        }

        public @NotNull EnvironmentAccess toEnvironmentAccess() {
            switch(this) {
                case NONE: return EnvironmentAccess.NONE;
                case DERIVE: return EnvironmentAccess.INHERIT;
            }
            // should not happen
            throw new IllegalStateException();
        }
    }

    public enum HostAccessPolicy {
        ALL,
        EXPLICIT,
        NONE;

        public static HostAccessPolicy getDefault() {
            return EXPLICIT;
        }

        public String toPreferenceValue() {
            switch(this) {
                case ALL: return "all";
                case EXPLICIT: return "explicit";
                case NONE: return "none";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public HostAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return getDefault();
            switch(value.toLowerCase().trim()) {
                case "all": return ALL;
                case "explicit": return EXPLICIT;
                case "none": return NONE;
                default:
                    final String message =
                            "unsupported preference value for host access policy. "
                                    + "Using 'explicit' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return getDefault();
            }
        }
    }


    private DefaultAccessPolicy defaultAccessPolicy;
    private TernaryAccessPolicy createProcessPolicy;
    private TernaryAccessPolicy createThreadPolicy;
    private TernaryAccessPolicy useExperimentalOptionsPolicy;
    private TernaryAccessPolicy hostClassLoadingPolicy;
    private TernaryAccessPolicy ioPolicy;
    private TernaryAccessPolicy nativeAccessPolicy;
    //TODO(karl): add polyglot access policy

    private EnvironmentAccessPolicy environmentAccessPolicy;
    private HostAccessPolicy hostAccessPolicy;

    public void resetToDefaults() {
        defaultAccessPolicy = ALLOW_ALL;

        createProcessPolicy = DERIVE;
        createThreadPolicy = DERIVE;
        useExperimentalOptionsPolicy = DERIVE;
        hostClassLoadingPolicy = DERIVE;
        ioPolicy = DERIVE;
        nativeAccessPolicy = DERIVE;

        environmentAccessPolicy = EnvironmentAccessPolicy.getDefault();
        hostAccessPolicy = HostAccessPolicy.getDefault();
    }

    public @NotNull GraalVMPrivilegesModel initFromPreferences(
        @NotNull final Preferences prefs) {
        Objects.requireNonNull(prefs);
        createProcessPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_CREATE_PROCESS_POLICY)
        );
        createThreadPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_CREATE_THREAD_POLICY)
        );
        useExperimentalOptionsPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY)
        );
        hostClassLoadingPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_HOST_CLASS_LOADING_POLICY)
        );
        ioPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_IO_POLICY)
        );
        nativeAccessPolicy = TernaryAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_NATIVE_ACCESS_POLICY)
        );
        environmentAccessPolicy = EnvironmentAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_ENVIRONMENT_ACCESS_POLICY)
        );
        hostAccessPolicy = HostAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_HOST_ACCESS_POLICY)
        );

        return this;
    }

    public void saveToPreferences(@NotNull final Preferences prefs) {
        Objects.requireNonNull(prefs);
        prefs.put(
            GRAALVM_CREATE_PROCESS_POLICY,
            createProcessPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_CREATE_THREAD_POLICY,
            createThreadPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY,
            useExperimentalOptionsPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_HOST_CLASS_LOADING_POLICY,
            hostClassLoadingPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_IO_POLICY,
            ioPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_NATIVE_ACCESS_POLICY,
            nativeAccessPolicy.toPreferenceValue()
        );
        prefs.put(
            GRAALVM_ENVIRONMENT_ACCESS_POLICY,
            environmentAccessPolicy.toPreferenceValue()
        );
    }

    public boolean isDefaultAccessAllowed() {
        return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
    }

    public void setDefaultAccessPolicy(@NotNull final DefaultAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.defaultAccessPolicy = policy;
    }

    public @NotNull DefaultAccessPolicy getDefaultAccessPolicy() {
        return defaultAccessPolicy;
    }

    public boolean allowCreateProcess(){
        switch(createProcessPolicy) {
            case ALLOW: return true;
            case DENY: return false;
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
        }
        // should not happen
        throw new IllegalStateException();
    }

    public TernaryAccessPolicy getCreateProcessPolicy() {
        Objects.requireNonNull(createProcessPolicy);
        return createProcessPolicy;
    }

    public void setCreateProcessPolicy(@NotNull final TernaryAccessPolicy policy) {
        this.createProcessPolicy = policy;
    }

    public boolean allowCreateThread(){
        switch(createThreadPolicy) {
            case ALLOW: return true;
            case DENY: return false;
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
        }
        // should not happen
        throw new IllegalStateException();
    }

    public TernaryAccessPolicy getCreateThreadPolicy() {
        return createThreadPolicy;
    }

    public void setCreateThreadPolicy(@NotNull final TernaryAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.createThreadPolicy = policy;
    }

    public boolean allowExperimentalOptions(){
        switch(useExperimentalOptionsPolicy) {
            case ALLOW:
                return true;
            case DENY:
            case DERIVE:
                // experimental options are always disabled, unless
                // explicitly enabled
                return false;
        }
        // should not happen
        throw new IllegalStateException();
    }

    public @NotNull TernaryAccessPolicy getUseExperimentalOptionsPolicy() {
        return useExperimentalOptionsPolicy;
    }

    public void setUseExperimentalOptionsPolicy(@NotNull final TernaryAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.useExperimentalOptionsPolicy = policy;
    }

    public boolean allowHostClassLoading(){
        switch(hostClassLoadingPolicy) {
            case ALLOW: return true;
            case DENY: return false;
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
        }
        // should not happen
        throw new IllegalStateException();
    }

    public @NotNull TernaryAccessPolicy getHostClassLoadingPolicy() {
        return hostClassLoadingPolicy;
    }

    public void setHostClassLoadingPolicy(@NotNull final TernaryAccessPolicy policy) {
        Objects.requireNonNull(policy);
        hostClassLoadingPolicy = policy;
    }

    public boolean allowIO(){
        switch(ioPolicy) {
            case ALLOW: return true;
            case DENY: return false;
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
        }
        // should not happen
        throw new IllegalStateException();
    }

    public @NotNull TernaryAccessPolicy getIOPolicy() {
        return ioPolicy;
    }

    public void setIOPolicy(@NotNull final TernaryAccessPolicy policy) {
        Objects.requireNonNull(policy);
        ioPolicy = policy;
    }

    public boolean allowNativeAccess(){
        switch(nativeAccessPolicy) {
            case ALLOW: return true;
            case DENY: return false;
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
        }
        // should not happen
        throw new IllegalStateException();
    }

    public @NotNull TernaryAccessPolicy getNativeAccessPolicy() {
        return nativeAccessPolicy;
    }

    public void setNativeAccessPolicy(@NotNull final TernaryAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.nativeAccessPolicy = policy;
    }

    public boolean allowEnvironmentAccess() {
        switch(environmentAccessPolicy) {
            case DERIVE:
                return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
            case NONE:
                return false;
        }
        throw new IllegalStateException();
    }

    public @NotNull EnvironmentAccessPolicy getEnvironmentAccessPolicy() {
        return environmentAccessPolicy;
    }

    public void setEnvironmentAccessPolicy(@NotNull final EnvironmentAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.environmentAccessPolicy = policy;
    }


    public @NotNull HostAccessPolicy getHostAccessPolicy() {
        return hostAccessPolicy;
    }

    public void setHostAccessPolicy(@NotNull final HostAccessPolicy policy) {
        Objects.requireNonNull(policy);
        this.hostAccessPolicy = policy;
    }

    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Context.Builder prepareContextBuilder(
            @NotNull final Context.Builder builder) {
        Objects.requireNonNull(builder);

        // individual policies
        builder.allowCreateProcess(allowCreateProcess());
        builder.allowCreateThread(allowCreateThread());
        builder.allowHostClassLoading(allowHostClassLoading());
        builder.allowAllAccess(allowIO());
        builder.allowExperimentalOptions(allowExperimentalOptions());
        builder.allowNativeAccess(allowNativeAccess());
        builder.allowEnvironmentAccess(
            environmentAccessPolicy.toEnvironmentAccess());

        return builder;
    }

    @Override
    public void preferenceChanged(PreferenceChangeEvent event) {
        final String key = event.getKey();
        @SuppressWarnings("unchecked")
        final Setting<String> value = (Setting<String>) event.getNewValue();
        switch(key) {
            case GRAALVM_CREATE_PROCESS_POLICY:
                createProcessPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;
            case GRAALVM_CREATE_THREAD_POLICY:
                createThreadPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;

            case GRAALVM_USE_EXPERIMENTAL_OPTIONS_POLICY:
                useExperimentalOptionsPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;
            case GRAALVM_HOST_CLASS_LOADING_POLICY:
                hostClassLoadingPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;

            case GRAALVM_IO_POLICY:
                ioPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;

            case GRAALVM_NATIVE_ACCESS_POLICY:
                nativeAccessPolicy = TernaryAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;

            case GRAALVM_ENVIRONMENT_ACCESS_POLICY:
                environmentAccessPolicy = EnvironmentAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;

            case GRAALVM_HOST_ACCESS_POLICY:
                hostAccessPolicy = HostAccessPolicy.fromPreferenceValue(
                    value.getValue()
                );
                break;
        }
    }
}


