---
  layout: page
  title: Download or upload data
  parent: API V3
  nav_order: 4
---

# Downloading and uploading data
{: .no_toc }

1. TOC
{: toc }

The module [josm/api]{:target="apidoc"} provides two classes for downloading data from and uploading data to the central OSM server:

  * [Api]{:target="apidoc"} - provides methods for downloading and uploading data

  * [ChangesetApi]{:target="apidoc"} - provides methods for opening, updating, closing, and downloading changesets

## Downloading data

The class [Api]{:target="apidoc"} provides several methods to download data using the [OSM API]{:target="apidoc"}.

Here's a sample scripts which downloads data in a bounding box:

```js
import josm from 'josm'
import {Api} from 'josm/api'
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

```js
import {DataSet, DataSetUtil} from 'josm/ds'
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Be careful when uploading to the OSMs main database!
//
// This example uploads to the development instance
// of the OSM database.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
const DEV_API_URL = "https://master.apis.dev.openstreetmap.org/api"
const DEV_API_HOST = "master.apis.dev.openstreetmap.org"
ApiConfig.serverUrl = DEV_API_URL
ApiConfig.authMethod = 'basic'
ApiConfig.setCredentials(
  'basic',
  {
    user: 'my-user-id',
    password:'my-password'
  },
  {host: DEV_API_HOST}
)

const dsUtil = new DataSetUtil(new DataSet())
// create two nodes ...
const nodes = [
  dsUtil.nodeBuilder.create(),
  dsUtil.nodeBuilder.create()
]
// ... and a way with these nodes
const ways = [
  dsUtil.wayBuilder.withNodes(...nodes).create()
]

// upload the new data
let processedPrimitives = Api.upload(dsUtil.ds)
processedPrimitives.forEach(primitive => {
  // after uploading the primitive is assigned a globally unique id
  util.assert(primitive.getUniqueId() > 0)
  // after uploading the initial version is 1
  util.assert(primitive.getVersion() === 1)
})
```

[Api]: ../../api/v3/module-josm_api.Api.html
[ChangesetApi]: ../../api/v3/module-josm_api.ChangesetApi.html
[josm/api]: ../../api/v3/module-josm_api.html
[OSM API]: http://wiki.openstreetmap.org/wiki/API_v0.6
