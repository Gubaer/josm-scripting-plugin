---
layout: page
title: Manipulating Data
parent: API V2
nav_order: 3
---

# Manipulating data
{: .no_toc }

1. TOC
{: toc }

JOSM is a powerful tool for creating maps, although the map data structure is quite simple. There are only three basic types of objects, or *OSM primitives*:

  1. **nodes** &ndash; individual points at a specific position
  2. **ways**  &ndash; sequences of nodes
  3. **relations** &ndash;  arbitrary ordered lists of nodes, ways, or other relations

Most of the scripts run by the Scripting Plugin will have to manipulate these primitives in one way or the other. A script manipulates the same java objects representing data primitives as JOSM does. The public methods and fields of the respective Java classes are available for scripting.

The API V2 provides a [**builder class**][josm/builder]{:target="apidoc"} for each OSM primitive. The following table lists the names of the Java classes for OSM primitives with their JavaScript builder classes.

| **Kind of primitive** | **Java class** | **JavaScript builder class** |
| node | [Node]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [NodeBuilder]{:target="apidoc"} |
| way | [Way]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [WayBuilder]{:target="apidoc"} |
| relation | [Relation]{:target="apidoc"}<br/>(extending [OsmPrimitive]{:target="apidoc"}) | [RelationBuilder]{:target="apidoc"} |



## Creating OSM primitives
Invoke a constructor of the Java class to create a node, a way, or a relation.

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

The JOSM Scripting Plugin includes three [builders][josm/builder]{:target="apidoc"} to create OSM primitives in JavaScript, see the overview table above. You can use a matching builder to create the primitives from the previous example:


```js
const console = require('josm/scriptingconsole')
const { NodeBuilder, RelationBuilder } = require('josm/builder')

let node
// Create a new node at position [12.45, 45.56]
node = NodeBuilder.withPosition(12.45,45.56).create()

// ... or ...
node = NodeBuilder.create({lat: 12.45, lon: 45.56})
console.println(`Created a node - id=${node.getUniqueId()}`)

let relation
// Create a new  relation with global id 12345 and global version 6
relation  = RelationBuilder.withId(12345,6).create()
// .. or ..
relation = RelationBuilder.create(12345, {version: 6})
console.println(`Created a relation - id=${relation.getUniqueId()}`)
```

## Setting and getting properties of OSM primitives

The native JOSM classes provide public setter and getter methods to set and get property values on OSM primitives.

See javadoc for

* [OsmPrimitive]{:target="apidoc"} and [AbstractPrimitive]{:target="apidoc"}
* [Node]{:target="apidoc"}
* [Way]{:target="apidoc"}
* [Relation]{:target="apidoc"}

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

JOSM manages interconnected primitives in a [DataSet]{:target="apidoc"}, a kind of container for nodes, ways, and relations.
A primitive doesn't have to belong to a dataset, but if it does, it can belong to at most one dataset.
The dataset is said to be its **parent**.

There are two main differences between detached primitives and those attached to a dataset:

1.   more integrity constraints are checked for attached primitives

      **Consequence:** what may work on a detached primitive may fail on an attached
      one

2. data changes are notified to listeners listening to change events on the parent data set

    **Consequence**: even simple property assignments on primitives may result in costly event
    propagation and UI refreshing.

    Consider to group batches of updates on attached primitives in a **batch** which
    notifies listeners only once about data change events for the entire batch:

    ```js
    const { NodeBuilder, RelationBuilder } = require('josm/builder')
    const { DataSetUtil, DataSet } = require('josm/ds')
    const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')

    let dsutil = new DataSetUtil(new DataSet())
    dsutil.nodeBuilder
       .withId(1)
       .create({lat: 12.45, lon: 45.56})
    dsutil.relationBuilder
        .withId(2)
        .withTags({'name': 'a-relation'})
        .create()

    // runs the updates on two primitives in a "batch"
    dsutil.batch(() => {
        // set new coordinates on the node
        dsutil.node(1).setCoor(new LatLon(11.11, 22.22))
        // assign a new name to the relation
        dsutil.relation(2).put('name', 'a-new-name')
    })
    ```

## Primitives and layers

JOSM provides a UI to display primitives and manipulate them interactively in **data layers**.
If primitives are modified interactively, the respective changes can be **undone** and **redone**.

Suppose you manipulate primitives attached to a dataset displayed in a layer.
In that case, you are better off applying **data commands** to the primitives instead of manipulating them directly.
For this purpose, the Scripting Plugin provides a [command API][josm/command]{:target="apidoc"}.

```js
const josm = require('josm')
const { change } = require('josm/command')
const { DataSetUtil, OsmPrimitiveType} = require('josm/ds')

const dsutil = new DataSetUtil()
dsutil.nodeBuilder.withId(1).withPosition(1.0, 2.0).create()
dsutil.nodeBuilder.withId(2).withPosition(3.0, 4.0).withTags({'width': '3m'}).create()
dsutil.wayBuilder.withId(3).withNodes(dsutil.node(1), dsutil.node(2)).create()
dsutil.relationBuilder.withId(4).create()

const layer = josm.layers.addDataLayer({name: 'my data layer', ds: dsutil.ds})

// creates and applies three undoable/redoable commands
change(dsutil.node(1), {lat: 12.45}).applyTo(layer)
change(dsutil.relation(4), {tags: {name: 'a new name'}}).applyTo(layer)

// to remove a tag, set its value to null
change(dsutil.way(3), {tags: {width: null}}).applyTo(layer)
```

## Find primitives in a dataset

The easiest way to get a hold of a primitive in a dataset is to access it by its unique numeric id.

```js
const { DataSetUtil } = require('josm/ds')

// creates a data set util with empty new data set
const dsutil = new DataSetUtil()
// populate the data set with some objects
dsutil.nodeBuilder.withId(1).withPosition(1.0,2.0).create()
dsutil.nodeBuilder.withId(2).withPosition(1.0,2.0).create()
dsutil.wayBuilder.withId(3).withNodes(dsutil.node(1), dsutil.node(2)).create()
dsutil.relationBuilder.withId(4).create()

let node
node = dsutil.get(1, 'node');
// .. or
node = dsutil.node(1)

let way
way = dsutil.get(3, 'way')
// ... or
way = dsutil.way(3)

let relation
relation = dsutil.get(4, OsmPrimitiveType.RELATION)
// ... or
relation = dsutil.relation(4)
```

In addition, you can *search* in a dataset using the method `query()`.
`query()` accepts two types of search expressions:

1.  a search expression as you would enter it in the JOSM search field
2.  a predicate as JavaScript function, which replies either true or false for a primitive


```js
const { DataSetUtil } = require('josm/ds')
const console = require('josm/scriptingconsole')

// create a dsutil with an empty new dataset
let dsutil = new DataSetUtil()
dsutil.nodeBuilder.withId(1)
  .withPosition(1.0,2.0)
  .withTags({'amenity': 'restaurant'})
  .create()

dsutil.nodeBuilder.withId(2)
  .withPosition(1.0,2.0)
  .withTags({'amenity': 'hotel'})
  .create()

let restaurants

// query the dataset with a predicate
restaurants = dsutil.query(primitive => {
    return primitive.get('amenity') === "restaurant"
})
console.println(`num restaurants: ${restaurants.length}`)


// query the dataset with a JOSM search expression
restaurants = dsutil.query('amenity=restaurant')
console.println(`num restaurants: ${restaurants.length}`)
```

[Node]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Node.html
[Way]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Way.html
[Relation]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/Relation.html
[DataSet]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/DataSet.html
[OsmPrimitive]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/OsmPrimitive.html
[AbstractPrimitive]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/osm/AbstractPrimitive.html
[NodeBuilder]: ../../api/v2/module-josm_builder-NodeBuilder.html
[WayBuilder]: ../../api/v2/module-josm_builder-WayBuilder.html
[RelationBuilder]: ../../api/v2/module-josm_builder-RelationBuilder.html
[josm/builder]: ../../api/v2/module-josm_builder.html
[josm/command]: ../../api/v2/module-josm_command.html