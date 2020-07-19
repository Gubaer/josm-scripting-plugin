---
  layout: page
  title: Server API
  parent: API V1
  nav_order: 4
---

{% include v1-deprecated.md %}

# Downloading and uploading data

The module <a data-js-object="module:josm/api">josm/api</a> provides two classes for downloading data from and uploading data to the central OSM server:

  * <a data-js-object="class:Api">Api</a> - provides methods for downloading and uploading data

  * <a data-js-object="class:ChangesetApi">ChangesetApi</a> - provides methods for opening, updating, closing, and downloading changesets

## Downloading data

The class <a data-js-object="class:Api">Api</a> provides several methods to download data
using the <a href="http://wiki.openstreetmap.org/wiki/API_v0.6">OSM API</a>.

Here's a sample scripts which downloads data in a bounding box:

```js
var api = require("josm/api").Api;
var dataset = api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484}, 
      max: {lat: 46.9497642, lon: 7.4660683}  
});
josm.layers.addDataLayer({ds: dataset, name: "Obstberg"});  
```

## Uploading data

The class <a data-js-object="class:Api">Api</a> provides the method `upload()`
to upload data to the server. It can be used to upload data using one of the upload strategies JOSM supports:

*   uploading each object individually
*   uploading objects in chunks
*   uploading objects in one go