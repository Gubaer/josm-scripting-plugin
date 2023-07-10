A collection of sample ECMAScript modules.

These ES modules are used as test cases. Some of them are very basic and export just a simple function provided in a single source file. Others have richer internal structure, internally import and use sub modules, or even import functions from other modules in this collection.

# How to use?

They are useful for two purposes:

1. as test case in **automatized functional tests**

    Test scripts import them and use the exported functions. The main goal is to test whether `import` statements are correctly resolved.
    See [ESModuleUsageTest.groovy](../../../test/functional/groovy/org/openstreetmap/josm/plugins/scripting/esmodules/ESModuleTest.groovy) for an example.

2. as test case in **manual interactive tests**

    To test the scripting plugin interactively in a running JOSM process the modules can be used as test modules. 

    In order to use them, you first have to configure an ES module repository in the scripting plugin preferences.
    1. Select menu item *Scripting -> Settings ...*
    2. Select the tab **ES Modules**
    3. Add a new repository pointing this directory