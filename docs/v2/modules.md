---
layout: page
title: Using modules
parent: API V2
nav_order: 1
---

{% include v2-deprecated.md %}

# Working with modules
{: .no_toc }

1. TOC
{: toc }

## Loading and using provided modules

The embedded `Graal.JS` scripting engine can load CommonJS-compatible [modules][CommonJS module].

Here's an example scripts which loads the built built in module [josm/util]{:target="apidoc"}.

```js
const util = require("josm/util")
util.println("Hello world!")
```

## Implementing and using custom modules

You can implement and use your own modules.

Here's an example of a simple CommonJS-module that exports the function `sayHello()`.

```js
// file: helloworld.js
exports.sayHello = function() {
    const System = Java.type('java.lang.System')
    System.out.println("Hello world!")
}
```

A client script can load and use the module. Here's an example:

```js
const { sayHello } = require("helloworld")
sayHello()
```

In the JOSM preferences, you can configure where the scripting plugin is looking
for modules. The plugin looks for modules in two places:

* First, it tries to load modules from the plugin jar file in the <code class="inline">/js/v2</code> directory.

* Then, it tries to load it from one of the configured **module repositories**. Each
  repository is either a directory in the local file system or in a jar/zip file in the local file system.


<img src="../../assets/img/v2/configure-script-repositories.png"/>



[CommonJS module]: http://www.commonjs.org/specs/modules/1.0/
[josm/util]: ../../api/v2/module-josm_util.html