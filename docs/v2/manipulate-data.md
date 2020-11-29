---
layout: page
title: Manipulate Data
parent: API V2
nav_order: 3
---


# Manipulating data

JOSM is a powerful tool to create maps, although the structure of the map data is quite simple. There are only three basic types of objects, or *OSM primitives*, as they are called in JOSM:

  1. **nodes** &ndash; individual points at a specific position
  2. **ways**  &ndash; sequences of nodes
  3. **relations** &ndash;  arbitrary ordered lists of nodes, ways, or other relations

Most of the scripts run by the Scripting Plugin will have to manipulate these primitives in one way or the other. A script manipulates the same java objects representing data primitives, as JOSM does. The public methods and fields of the respective Java classes are available for scripting. In addition, there are JavaScript properties and functions mixed into the Java classes. They don't replace the native fields and methods, but they extend them with properties and and functions which are more "natural" to a script in a JavaScript-environment.

The following table lists the names of the basic Java classes for data primitives and the names of the corresponding JavaScript *mixins*.

| **Kind of primitive** | **Java class** | **JavaScript builder class** |
| node | [Node]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [NodeBuilder]{:target="apidoc"} |
| way | [Way]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [WayBuilder]{:target="apidoc"} |
| relation | [Relation]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [RelationBuilder]{:target="apidoc"} |



## Creating OSM primitives
Nodes, ways, and relations can be created by invoking one of the native constructors 
of the respective Java classes. Here are two basic examples: 

```js
const Node = Java.type('org.openstreetmap.josm.data.osm.Node')
const Relation = Java.type('org.openstreetmap.josm.data.osm.Relation')
const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')
const console = require('josm/scriptingconsole')

// Create a new node at position [12.45, 45.56]
const node = new Node(new LatLon(12.45, 45.56))
console.println(`Created a node - id=${node.getUniqueId()}`)

// Create a new empty relation with global id 12345 and global version 6
const relation = new Relation(12345, 6)
console.println(`Created a relation - id=${relation.getUniqueId()}`)
```

The JOSM Scripting Plugin includes three [builders][josm/builder]{:target="apidoc"} to create OSM primitives in JavaScript, see the overview table above. The primitives from the previous example could be created as follows:


```js
const console = require('josm/scriptingconsole')
const { NodeBuilder, RelationBuilder } = require('josm/builder')
    
// Create a new node at position [12.45, 45.56]
let node = NodeBuilder.withPosition(12.45,45.56).create()

// ... or ...
let node = NodeBuilder.create({lat: 12.45, lon: 45.56})
console.println(`Created a node - id=${node.getUniqueId()}`)

// Create a new  relation with global id 12345 and global version 6
let relation  = RelationBuilder.withId(12345,6).create()
// .. or ..
let relation = RelationBuilder.create(12345, {version: 6})
console.println(`Created a relation - id=${relation.getUniqueId()}`)
```

## Setting and getting properties of OSM primitives

The native JOSM classes provide public setter and getter methods to set and get property values on OSM primitives. 

See javadoc for

* [OsmPrimitive] and [AbstractPrimitive]
* [Node]
* [Way]
* [Relation]

A few examples:

```js
const console = require('josm/scriptingconsole')
const { NodeBuilder, RelationBuilder } = require('josm/builder')
const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')

let node = NodeBuilder.create({lat: 12.45, lon: 45.56})

// set whether a node is modified
node.setModified(true)
console.println(`node is modified: ${node.isModified()}`)

// set the coordinates of a node
node.setCoor(new LatLon(1.0, 2.0))
console.println(`node coordinates: \
  lat=${node.getCoor().lat()}, \
  lon=${node.getCoor().lon()}`)
```

## Primitives and datasets

JOSM manages interconnected primitives in **datasets**, a kind of container for nodes, ways, and relations. 
A primitive doesn't have to belong to a dataset, but if it does, it can belong to at most one dataset. 
The dataset is said to be its **parent**.


|           | **Java class** | 
| data set  | [DataSet]{:target="apidoc"} |

There are two major differences between detached primitives and those attached to a dataset:


1.   more integrity constraints are checked for attached primitives

      **Consequence:** what may work on a detached primitive, may fail on an attached
      one

2. data changes are notified to listeners listing on change events on the parent data set

    **Consequence**: even simple property assignements on primitives may result in costly event
    propagation and UI refreshing.

  Consider to group batches of updates on attached primitives in a **batch** which 
  notifies listeners only once about data change events for the entire batch:

  ```js
  const { DataSetUtil } = require('josm/ds')
  const ds = ... // assume ds is an already initialized data set
  // runs the updates on two primitives in a "batch"
  new DataSetUtil(ds).batch((ds) => {
     //TODO: check and fix
     ds.node(12345).lat = 12.34
     ds.relation(67890).tags.name = 'a new name'
  })
  ``` 

## Primitives and layers

JOSM provides an UI to display primitives and manipulate them interactively in **data layers**.  
If primitives are modified interactively, the respective changes can be **undone** and **redone**.

If you manipulate primitives attached to a dataset which is itself attached to a data layer, 
you are better off to apply **data commands** to the primitives, instead of manipulating them directly.
For this purpose, the Scripting Plugin provides a [command API][josm/command]{:target="apidoc"}.

```js
const { change } = require('josm/command')
const { DataSetUtil } = require('josm/ds')
const layer = josm.layers.get('my data layer')

const ds = new DataSetUtil(layer.data)

// creates and applies two undoable/redoable commands 
layer.apply(
  command.change(ds.node(12345), {lat: 12.45}),
  command.change(ds.relation(67890), {tags: {name: 'a new name'}})

  // to remove a tag, set its value to null
  command.change(ds.way(87632), {tags: {width: null}})    
)
```

## Find primitives in dataset

The easiest way to get hold on a primitive in a dataset is to access it by its unique numeric id.

```js
const { DataSetUtil } = require('josm/ds')
let ds = .... // a dataset

ds = new DataSetUtil(ds)

const node = ds.get("node", 12345);
// .. or
const node = ds.node(12345)

const way = ds.get("way", 12345)
// ... or
const way = ds.way(12345)

const relation = ds.get("relation", -27222) // this is a local id
// ... or
const relation = ds.relation(-27222)
```

In addition, you can *search* in a dataset using the method `query()`. 
`query()` accepts two types of search expressions:

1.  a search expression as you would enter it in the JOSM search field
2.  a predicate as JavaScript function, which replies either true or false for a primitive


```js
const { DataSetUtil } = require('josm/ds')
let ds = .... // a dataset
dsUtil = new DataSetUtil(ds)

// query the dataset with a predicate 
const restaurants = dsUtil.query((primitive) => {
    return primitive.tags.amenity == "restaurant"
})

// query the dataset with a JOSM search expression
const restaurants = dsUtil.query('amenity=restaurant')
```

[Node]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Node.html
[Way]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Way.html
[Relation]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Relation.html
[DataSet]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/DataSet.html
[OsmPrimitive]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/OsmPrimitive.html
[AbstractPrimitive]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/AbstractPrimitive.html
[NodeBuilder]: /api/v2/module-josm_builder.NodeBuilder.html
[WayBuilder]: /api/v2/module-josm_builder.WayBuilder.html
[RelationBuilder]: /api/v2/module-josm_builder.RelationBuilder.html
[josm/builder]: /api/v2/module-josm_builder.html
[josm/command]: /api/v2/module-josm_command.html