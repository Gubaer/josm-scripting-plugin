---
layout: page
title: Working with layers
parent: API V2
nav_order: 2
---

# Handling layers

The global object `josm.layers` represents the current layers in josm. The methods
and properties of this object are defined in the module [`josm/layers`](josm/layers).

The following scripts prints the names of the currently opened layers:

```js
const josm = require('josm')
const util = require('josm/util')
const numLayers = josm.layers.length
for (let i=0; i < numLayers; i++) {
   util.println(`Layer ${i}: name = '${josm.layers.get(i)}'`)
}
```

## Working with data layers

Data layers are instances of the JOSM native class [OsmDataLayer](org.openstreetmap.josm.gui.layer.OsmDataLayer).
You can **open** a file with OSM data in a new data layer with the method
`josm.open()`:

```js
const josm = require('josm')
// Opens a new data layer for this file
josm.open('/my/data/file.osm')
```

Alternatively, you can create a data layer for a dataset, in particual for a dataset
which has been [downloaded](josm/api/Api) from the central OSM server.

```js
const josm = require('josm')
const { Api } = require('josm/api')
const dataset = Api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484}, 
      max: {lat: 46.9497642, lon: 7.4660683}  
})
josm.layers.addDataLayer({ds: dataset, name: 'Obstberg'})  
```


[josm/layers]: /api/v2/module-josm_layers.html
[org.openstreetmap.josm.gui.layer.OsmDataLayer]: todo
[josm/api/Api]: todo