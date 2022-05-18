---
layout: page
title: Using JSR 223
nav_order: 4
---

# Using a pluggable scripting engine (JSR 223)

[JSR 223][jsr223] defines how scripting engines can be embeeded on the Java platform.

The JOSM Scripting Plugin makes use of this standard. You can add any JSR223-compatible scripting engine and use it for scripting in JOSM.


## Adding a scripting engine

1. Select the menu item **Scripting** -&gt; **Configure ...**

2. Select the tab **Script engines**

3. Download one of the scripting engines for Python, Groovy, or Ruby. Or download another scripting language and add its jar file on this tab.

<img id="configure-scripting-engines" src="/assets/img/v2/configure-scripting-engine.png"/>


## Selecting/activating a scripting engine

In the scripting console you can select a scripting engine to run a script.

1. Display the scripting console, select the menu item **Scripting** -&gt; **Show Scripting Console**

2. Clich on **Change** to select a scripting engine

3. Select one of the configured scripting engines

<img id="configure-scripting-engines" src="/assets/img/v2/select-scripting-engine.png"/>



## Implementing a script

## Running a script

## Sample Scripts

Sample scripts for JavaScript, Python, Groovy, and Ruby are available [here][sample-scripts].


<dl>
 <dt><a href="pluggable.html">Adding and removing a pluggable scripting engine</a></dt>
 <dd>How to add or remove a pluggable scripting engine.</dd>

 <dd>Selecting a pluggable scripting engine</dd>
 <dd>How to select a pluggable scripting engine</dd>

 <dt>Sample scripts</dt>
 <dd>Sample scripts in JavaScript, Python, Groovy, and Ruby are available
 on <a href="https://github.com/Gubaer/josm-scripting-plugin/tree/master/scripts">GitHub</a>.</dd>
</dl>


[jsr223]: https://jcp.org/en/jsr/detail?id=223
[sample-scripts]: https://github.com/Gubaer/josm-scripting-plugin/tree/master/scripts