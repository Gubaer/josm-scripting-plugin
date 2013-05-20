/*
 * Checks whether one of the provided CommonJS modules can be loaded
 * and lists the properties, the module exports.
 * 
 * Usage:
 *   groovy check-module.groovy moduleId
 */
import org.mozilla.javascript.Context
import org.mozilla.javascript.commonjs.module.Require
import org.openstreetmap.josm.plugins.scripting.js.JOSMModuleScriptProvider

if (args.length != 1) {
	println "usage: groovy check-module.groovy moduleId";
	System.exit(1);
}
def moduleId = args[0];
def home = System.getenv()["JOSM_SCRIPTING_PLUGIN_HOME"];
if (!home) {
	println "FATAL: environment variable JOSM_SCRIPTING_PLUGIN_HOME not set. See 'env.sh'."
	System.exit(1);
} 

def ctx = Context.getCurrentContext();
if (ctx == null) ctx = Context.enter();
def scope = ctx.initStandardObjects();
JOSMModuleScriptProvider.getInstance().addRepository(new File("${home}/javascript").toURI().toURL());
require = new Require(ctx,scope, JOSMModuleScriptProvider.getInstance(), null, null, false);
require.install(scope);
def module = require.call(ctx, scope, null, [moduleId].toArray());
println "Module ${moduleId} - OK";
println "Exporting: ";
module.getIds().each { println "* ${it}"}

