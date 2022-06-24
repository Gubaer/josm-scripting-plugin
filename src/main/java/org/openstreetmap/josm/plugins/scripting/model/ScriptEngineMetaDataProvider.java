package org.openstreetmap.josm.plugins.scripting.model;

import org.openstreetmap.josm.plugins.scripting.graalvm.GraalVMFacadeFactory;
import org.openstreetmap.josm.plugins.scripting.jsr223.JSR223ScriptEngineProvider;

import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

/**
 * Provides meta-data about available script engines
 */
@SuppressWarnings("unused")
public class ScriptEngineMetaDataProvider {

    /**
     * Replies a stream of the available JSR223 compatible script engines.
     *
     * @return the stream of engines. Empty stream, if no engines are available
     */
    static public @NotNull
        Stream<ScriptEngineDescriptor> getAvailablePluggedScriptEngines() {
        return JSR223ScriptEngineProvider
            .getInstance().getScriptEngineFactories()
            .stream()
            .map(ScriptEngineDescriptor::new);
    }

    /**
     * Replies the stream of available script engines supported by the
     * GraalVM.
     *
     * @return the stream of engines. Empty, if no engines are available
     */
    static public @NotNull
        Stream<ScriptEngineDescriptor> getAvailableGraalVMScriptEngines() {
        if (!GraalVMFacadeFactory.isGraalVMPresent()) {
            return Stream.of();
        }
        return GraalVMFacadeFactory
            .getOrCreateGraalVMFacade()
            .getScriptEngineDescriptors()
            .stream();
    }

    /**
     * Replies the stream of available script engines, either script engines
     * compatible with JSR223, or script engines supported by the GraalVM
     *
     * @return the stream of script engines. Empty, if no script engines
     * are available
     */
    static public @NotNull
        Stream<ScriptEngineDescriptor> getAvailableEngines() {

        return Stream.concat(
            getAvailablePluggedScriptEngines(),
            getAvailableGraalVMScriptEngines()
        );
    }
}
