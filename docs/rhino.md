---
layout: page
title: Using Mozilla Rhino (deprecated)
nav_order: 3
---

# Using Mozilla Rhino (deprecated)

The JOSM Scripting Plugin ships with an embedded scripting engine based on [Mozilla Rhino].

Support for Moziall Rhino and the [API V1](v1/v1) is **deprecated**. It will be removed end of 2022.
Consider to migrate your scripts to [GraalJS][graaljs] and [API V2](v2/v2).
{: .m-4 .p-4 .bg-red-000}


## Using the JavaScript [API V1](v1/v1)

The JOSM Scripting Plugin ships with a JavaScript API. [API V1](v1/v1) can be used together with Mozilla Rhino as a scripting engine.

[API V1](v1/v1) has the following main features:

* loading and using CommonJS-compatible modules
* lookup, add, delete, and rename layers
* load data into a layer, save data from a layer
* download objects and changesets from the OSM server, upload objects and changesets to the OSM server
* create data sets
* create nodes, ways, and relations in a data set
* update nodes, ways, and relations
* query data sets for nodes, ways, or relations
* create undoable/redoable change objects to update nodes, ways, and relations
* write to the scripting console

Refer to the [API V1](v1/v1) documentation.

[graaljs]: https://github.com/oracle/graaljs
[Mozilla Rhino]: http://www.mozilla.org/rhino/