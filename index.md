---
layout: page
title: Home
nav_order: 1
---


# JOSM Scripting Plugin

## Run scripts in the Open Street Map editor JOSM

The JOSM Scripting Plugin is a plugin for the [Open Street Map]{:target="external"} editor [JOSM]{:target="external"}.

It can execute scripts 

1. written in ECMAscript using the [Graal.JS](Graal.js){:target="external"} scripting engine for JavaScript and the [API V3](api/v3) to manipulate JOSMs internal application objects.

2. written in [Groovy], [Ruby], or any other language for which a JSR-223 compatible script engine is available.

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


[Open Street Map]: http://www.openstreetmap.org
[JOSM]: http://josm.openstreetmap.de
[Mozilla Rhino]: http://www.mozilla.org/rhino/
[Groovy]: http://groovy.codehaus.org/
[Ruby]: http://www.ruby-lang.org