---
layout: page
title: Handling Layers
parent: API V2
nav_order: 1
---

# Handling layers

The global object `josm.layers` represents the current layers in josm. The methods
and properties of this object are defined in the module <a data-js-object="module:josm/layers">layers</a>

The following scripts prints the names of the currently opened layers:

{% highlight javascript %}
const util = require("josm/util")
const l = josm.layers.length
for (let i=0; i < l; i++) {
   util.println("Layer {0}: name is ''{1}''", i, josm.layers.get(i))
}
{% endhighlight %}

## Working with data layers

Data layers are instances of the JOSM native class <a data-josm-class="org.openstreetmap.josm.gui.layer.OsmDataLayer">OsmDataLayer</a>.
You can **open** a file with OSM data in a new data layer with the method
`josm.open()`:

{% highlight javascript %}
// Opens a new data layer for this file
josm.open("/my/data/file.osm")
{% endhighlight %}

Alternatively, you can create a data layer for a dataset, in particual for a dataset
which has been <a data-js-object="class:Api">downloaded</a> from the central OSM server.

{% highlight javascript %}
const api = require("josm/api").Api
const dataset = api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484}, 
      max: {lat: 46.9497642, lon: 7.4660683}  
})
josm.layers.addDataLayer({ds: dataset, name: "Obstberg"})  
{% endhighlight %}

