---
  layout: page
  tile: Available Scripting Engines
  nav_order: 4
---

# Available Scripting Engines

## Using a Jython engine
The JOSM Scripting Plugin can execute scripts written in Python. It can either execute Python scripts  using the JSR-223 compatible script engine provided by Jython or load and execute Python plugins  using Jythons embedding API.

*   [Implementing and loading Python plugins](python.md)

    How to implement a Python plugin which is automatically loaded at startup time similar to the traditional JOSM plugins written in Java.



## Using the embedded JavaScript engine

The JOSM Scripting Plugin is shipped with an embedded scripting engine based on Mozilla Rhino. It consists of a custom instance of the Rhino engine, not identical with the engine shipped with the Java runtime environment (JRE).

<p style="padding: 5pt;border-width: 1pt; border-style: dotted; border-color: red;">
Please note that the <a href="js-doc/index.html">JavaScript API</a> isn't frozen yet. The current API may
change in future plugin releases. Please <a href="https://github.com/Gubaer/josm-scripting-plugin/issues">report bugs or feature request</a>.
</p>


<dl>
  <dt><a href="scripting-environment.html">Scripting environment</a></dt>
  <dd>Global objects in scripts, loading CommonJS modules</dd>

  <dt><a href="scripting.html">Scripting JOSM classes</a></dt>
  <dd>How to access JOSM classe and use mixins implemented in JavaScript</dd>

  <dt><a href="manipulating-data.html">Manipulating data</a></dt>
  <dd>How to create data primitives like nodes, ways, or relations. How to add them to datasets, remove
  them from datasets, and find them in datasets.</dd>

  <dt><a href="handling-layers.html">Handling layers</a></dt>
  <dd>How to create, access, and remove layers.</dd>

  <dt><a href="api.html">Downloading and uploading data</a></dt>
  <dd>How to download and upload data to and from the OSM server.</dd>

  <dt><a href="menu.html">Extending the JOSM menu and the JOSM toolbar</a></dt>
  <dd>How to extend the JOSM menu and the JOSM toolbar with actions implemented in JavaScript.</dd>
 </dl>

## Using a pluggable scripting engine

<dl>
 <dt><a href="pluggable.html">Adding and removing a pluggable scripting engine</a></dt>
 <dd>How to add or remove a pluggable scripting engine.</dd>

 <dd>Selecting a pluggable scripting engine</dd>
 <dd>How to select a pluggable scripting engine</dd>

 <dt>Sample scripts</dt>
 <dd>Sample scripts in JavaScript, Python, Groovy, and Ruby are available
 on <a href="https://github.com/Gubaer/josm-scripting-plugin/tree/master/scripts">GitHub</a>.</dd>
</dl>
