package org.openstreetmap.josm.plugins.scripting.preferences.graalvm;


import org.openstreetmap.josm.data.Preferences;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.model.PreferenceKeys.*;
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.DefaultAccessPolicy.DENY_ALL;
import static org.openstreetmap.josm.plugins.scripting.preferences.graalvm.GraalVMPrivilegesModel.TernaryAccessPolicy.DERIVE;

@SuppressWarnings("unused")
public class GraalVMPrivilegesModel {

    static public final Logger logger =
        Logger.getLogger(GraalVMPrivilegesModel.class.getName());

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
                case "derive": return DERIVE;
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
        /** allow for read-only access to environment variables */
        READ_ONLY,
        /** deny access to environment variables */
        NONE,
        /** derive the privilege from the defeault access policy
         * @see DefaultAccessPolicy
         */
        DERIVE;

        public String toPreferenceValue() {
            switch(this) {
                case READ_ONLY: return "read-only";
                case NONE: return "none";
                case DERIVE: return "derive";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public EnvironmentAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return DERIVE;

            switch(value.toLowerCase().trim()) {
                case "read-only": return READ_ONLY;
                case "none": return NONE;
                case "derive": return DERIVE;
                default:
                    final String message =
                        "unsupported preference value for environment access policy. "
                      + "Using 'derive' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return DERIVE;
            }
        }
    }

    public enum HostAccessPolicy {
        ALL,
        EXPLICIT,
        NONE,
        DERIVE;

        public String toPreferenceValue() {
            switch(this) {
                case ALL: return "all";
                case EXPLICIT: return "explicit";
                case NONE: return "none";
                case DERIVE: return "derive";
            }
            // should not happen
            throw new IllegalStateException();
        }

        static public HostAccessPolicy fromPreferenceValue(@Null final String value) {
            if (value == null) return DERIVE;
            switch(value.toLowerCase().trim()) {
                case "all": return ALL;
                case "explicit": return EXPLICIT;
                case "none": return NONE;
                case "derive": return DERIVE;
                default:
                    final String message =
                        "unsupported preference value for host access policy. "
                      + "Using 'derive' instead. Got '%s'";
                    logger.log(Level.WARNING, String.format(message, value));
                    return DERIVE;
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
    //Note: polyglot access policy is fixed and not configurable

    //TODO(karl): support environment access policy
    private EnvironmentAccessPolicy environmentAccessPolicy;
    //TODO(karl): support host access policy
    private HostAccessPolicy hostAccessPolicy;

    public void resetToDefaults() {
        defaultAccessPolicy = DENY_ALL;

        createProcessPolicy = DERIVE;
        createThreadPolicy = DERIVE;
        useExperimentalOptionsPolicy = DERIVE;
        hostClassLoadingPolicy = DERIVE;
        ioPolicy = DERIVE;
        nativeAccessPolicy = DERIVE;
    }

    public void initFromPreferences(@NotNull final Preferences prefs) {
        Objects.requireNonNull(prefs);
        defaultAccessPolicy = DefaultAccessPolicy.fromPreferenceValue(
            prefs.get(GRAALVM_DEFAULT_ACCESS_POLICY)
        );
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
    }

    public void saveToPreferences(@NotNull final Preferences prefs) {
        Objects.requireNonNull(prefs);
        prefs.put(
            GRAALVM_DEFAULT_ACCESS_POLICY,
            defaultAccessPolicy.toPreferenceValue()
        );
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
    }

    public boolean isDefaultAccessAllowed() {
        return DefaultAccessPolicy.ALLOW_ALL.equals(defaultAccessPolicy);
    }

    public void setDefaultAccessPolicy(@NotNull final DefaultAccessPolicy policy) {
        this.defaultAccessPolicy = policy;
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
}
