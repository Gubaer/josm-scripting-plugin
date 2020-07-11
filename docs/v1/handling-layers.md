---
layout: page
title: Layers
parent: API V1
nav_order: 2
---

{% include v1-deprecated.md %}

# Working with layers

The global object `josm.layers` represents the current layers in josm. The methods
and properties of this object are defined in the module <a data-js-object="module:josm/layers">layers</a>

The following scripts prints the names of the currently opened layers:

```js
var util = require("josm/util");
var l = josm.layers.length;
for (var i=0; i < l; i++) {
   util.println("Layer {0}: name is ''{1}''", i, josm.layers.get(i));
}
```

## Working with data layers

Data layers are instances of the JOSM native class <a data-josm-class="org.openstreetmap.josm.gui.layer.OsmDataLayer">OsmDataLayer</a>.
You can **open** a file with OSM data in a new data layer with the method
`josm.open()`:

```js
// Opens a new data layer for the file '/my/data/file.osm'
josm.open("/my/data/file.osm");
```

Alternatively, you can create a data layer for a dataset, in particular for a dataset
which has been <a data-js-object="class:Api">downloaded</a> from the central OSM server.

```js
var api = require("josm/api").Api;
var dataset = api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484}, 
      max: {lat: 46.9497642, lon: 7.4660683}  
});
josm.layers.addDataLayer({ds: dataset, name: "Obstberg"});
```

