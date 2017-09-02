/*
 * Suite of unit tests.
 * 
 * How to run
 * ==========
 * - Add the path  
 *       $JOSM_SCRIPTING_PLUGIN_ROOT/test/script-api
 *   to the list of module repositories
 *        o  launch JOSM
 *        o  Scripting -> Configure ...
 *        o  Select Tab 'Embedded Rhino Engine'
 *        o  Add the module repository for unit tests
 *        
 * - Launch JOSM, open the scripting console and enter
 *      require("suite").run();
 *      
 * - Run the unit test
 */
var tests = [
    "DataSetWrapperTest",
    "josm/ChangesetMixinTest",
    "josm/commandTest",
    "josm/DataSetMixinTest",
    "josm/LatLonMixinTest",
    "josm/NodeBuilderTest",
    "josm/NodeMixinTest",
    "josm/OsmPrimitiveMixinTest",
    "josm/RelationBuilderTest",
    "josm/RelationMixinTest",
    "josm/UploadStrategyMixinTest",
    "josm/utilTest",
    "josm/WayBuilderTest",
    "josm/WayMixinTest",
];

function run() {
    for (var i=0; i<tests.length; i++) {
    	require(tests[i]).run();
    }
};

if (typeof exports === "undefined") {
    // not loaded as module. Run the tests immediately.
    run();
} else {
    // loaded as module. Export the run function but don't
    // execute it here. 
    exports.run = run;
}


