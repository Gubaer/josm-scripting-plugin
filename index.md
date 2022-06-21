---
layout: page
title: Home
nav_order: 1
---


# JOSM Scripting Plugin

## Run scripts in the Open Street Map editor JOSM

The JOSM Scripting Plugin is a plugin for the [Open Street Map]{:target="external"} editor [JOSM]{:target="external"}.

It can execute scripts using the [Graal.JS](Graal.js){:target="external"} scripting engine for Javascript and the [API V3](api/v3) to manipulate JOSMs internal application objects with JavaScript.

For historical reasons it also includes 

* the [Rhino](Mozilla Rhino){:target="external"} scripting engine for Javascript  and the [API V1](api/v1) to manipulate JOSMs internal application objects with JavaScript. This API is **deprecated**. The Rhino engine and the API V1 will be removed from the scripting plugin end of 2022.

* the [API V2](api/v2) based on CommonJS-Modules and used in [Graal.JS](Graal.js){:target="external"}. [API V2](api/v2) is **deprecated**. It will be removed from the scripting plugin end of 2022.

```js
import josm from 'josm'
import { buildAddCommand } from 'josm/command'
import { NodeBuilder } from 'josm/builder'
const layer = josm.layers.addDataLayer('Obstberg')
const restaurant = NodeBuilder
  .withTags({amenity: 'restaurant'})
  .withPosition(12.34,45.67)
  .create()
buildAddCommand(restaurant)
  .applyTo(layer)
josm.alert('Added restaurant Obstberg')
```

<img id="console-img" src="assets/img/scripting-console-sample.png"/>

The plugin can execute scripts written in [Groovy], [Ruby], or any other language for which a JSR-223 compatible script engine is available.

[Open Street Map]: http://www.openstreetmap.org
[JOSM]: http://josm.openstreetmap.de
[Mozilla Rhino]: http://www.mozilla.org/rhino/
[Groovy]: http://groovy.codehaus.org/
[Ruby]: http://www.ruby-lang.org