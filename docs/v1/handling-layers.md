---
layout: page
title: Working with layers
parent: API V1
has_toc: true
nav_order: 2
---

{% include v1-deprecated.md %}

# Working with layers
{: .no_toc }

1. TOC
{: toc }

The global object <code class="inline">josm.layers</code> represents the current layers in josm. The methods
and properties of this object are defined in the module [josm/layers]{:target="apidoc"}.

The following scripts prints the names of the currently opened layers:

```js
var util = require("josm/util");
var len = josm.layers.length;
for (var i=0; i < len; i++) {
   util.println("Layer {0}: name is ''{1}''", i, josm.layers.get(i));
}
```

## Working with data layers

Data layers are instances of the JOSM native class [OsmDataLayer]{:target="apidoc"}.

You can **open** a file with OSM data in a new data layer with the method
<code class="inline">josm.open()</code>:

```js
// Opens a new data layer for the file '/my/data/file.osm'
josm.open("/my/data/file.osm");
```

Alternatively, you can create a data layer for a dataset, in particular for a dataset
which has been [Api]{:target="apidoc"} from the OSM server.

```js
var api = require("josm/api").Api;
var dataset = api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484},
      max: {lat: 46.9497642, lon: 7.4660683}
});
josm.layers.addDataLayer({ds: dataset, name: "Obstberg"});
```

[Api]: ../../api/v1/module-josm_api.Api.html
[josm/layers]: /api/v1/module-josm_layers.html
[OsmDataLayer]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/gui/layer/OsmDataLayer.html


