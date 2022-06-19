---
layout: page
title: Using modules
parent: API V3
nav_order: 1
---

# Working with modules
{: .no_toc }

1. TOC
{: toc }

## Loading and using ES Modules from API V3

The embedded `Graal.JS` scripting engine can import [ES Modules][es-module-spec].

Here's an example scripts which loads the built in module [josm/util]{:target="apidoc"}.

```js
import * as util from 'josm/util'
util.println('Hello world!')

// or:
import {println} from 'josm/util' 
println('Hello World!')
```

## Implementing and using custom ES Modules

You can implement and use your own ES Modules.

Here's an example of a simple module that exports the function `sayHello()`.

```js
// file: helloworld.js
export function sayHello() {
    const System = Java.type('java.lang.System')
    System.out.println("Hello world!")
}
```

A client script can load and use the module. Here's an example:

```js
import {sayHello} from 'helloworld'
sayHello()
```

In the JOSM preferences, you can configure where the scripting plugin is looking
for modules. The plugin looks for modules in two places:

* First, it tries to load modules from the plugin jar file in the <code class="inline">/js/v3</code> directory.

* Then, it tries to load it from one of the configured **module repositories**. Each
  repository is either a directory in the local file system or in a jar/zip file in the local file system.


<img src="../../assets/img/v3/configure-es-module-repositories.png"/>


[es-modules-spec]: https://262.ecma-international.org/6.0/#sec-modules
[CommonJS module]: http://www.commonjs.org/specs/modules/1.0/
[josm/util]: ../../api/v3/module-josm_util.html