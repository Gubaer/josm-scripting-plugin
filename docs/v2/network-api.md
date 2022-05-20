---
  layout: page
  title: Download or upload data
  parent: API V2
  nav_order: 4
---

# Downloading and uploading data

The module [josm/api]{:target="apidoc"} provides two classes for downloading data from and uploading data to the central OSM server:

  * [Api]{:target="apidoc"} - provides methods for downloading and uploading data

  * [ChangesetApi]{:target="apidoc"} - provides methods for opening, updating, closing, and downloading changesets

## Downloading data

The class [Api]{:target="apidoc"} provides several methods to download data using the [OSM API]{:target="apidoc"}.

Here's a sample scripts which downloads data in a bounding box:

```js
const josm = require('josm')
const { Api } = require('josm/api')
const dataset = Api.downloadArea({
      min: {lat: 46.9479186, lon: 7.4619484},
      max: {lat: 46.9497642, lon: 7.4660683}
})
josm.layers.addDataLayer({ds: dataset, name: 'Obstberg'})
```

## Uploading data

The class [Api]{:target="apidoc"} provides the method `upload()` to upload data to the server. You can use it to upload data using one of
the upload strategies JOSM supports:

*   uploading each object individually
*   uploading objects in chunks
*   uploading objects in one go

[Api]: /api/v2/module-josm_api-Api.html
[ChangesetApi]: /api/v2/module-josm_api-ChangesetApi.html
[josm/api]: /api/v2/module-josm_api.html
[OSM API]: http://wiki.openstreetmap.org/wiki/API_v0.6
