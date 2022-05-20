---
layout: page
title: Using modules
parent: API V1
nav_order: 1
---

{% include v1-deprecated.md %}

# Working with modules
{: .no_toc }

1. TOC
{: toc }

## Loading and using provided modules

The embedded scripting engine can load CommonJS-compatible [modules][CommonJS module].

Here's an example scripts which loads the built built in module [josm/util]{:target="apidoc"}.

```js
var util = require("josm/util");
util.println("Hello world!");
```

## Implementing and using custom modules

You can implement and use your own modules.

Here's an example of a simple CommonJS-module that exports the function `sayHello()`.


```js
// file: helloworld.js
exports.sayHello = function() {
    java.lang.System.out.println("Hello world!");
}
```

A client script can load and use the module. Here's an example:

```js
var hello = require("helloworld");
hello.sayHello();
```

In the JOSM preferences, you can configure where the Scripting Plugin is looking
for modules. The plugin looks for modules in two places:

* First, it tries to load modules from the plugin jar file in the `/js` directory.

* Then, it tries to load it from one of the configured *plugin repositories*. Each repository is
  either a directory in the local file system or in a jar/zip file in the local filesystem.


<img src="/assets/img/v1/configure-script-repositories.png"/>


[CommonJS module]: http://www.commonjs.org/specs/modules/1.0/
[josm/util]: /api/v1/module-josm_util.html