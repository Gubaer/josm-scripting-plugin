---
layout: page
title: Data
parent: API V1
nav_order: 3
---

{% include v1-deprecated.md %}

# Manipulating data

JOSM is a powerful tool to create maps, although the structure of the map data is quite simple. There are only three basic types of objects or *OSM primitives*, as they are called in JOSM:

  1. **nodes** &ndash; individual points at a specific position
  2. **ways**  &ndash; sequences of nodes
  3. **relations** &ndash;  arbitrary ordered lists of nodes, ways, or other relations

Most of the scripts run by the Scripting Plugin will have to manipulate these primitives in one
way or the other. Basically, a scripts manipulates the same java objects representing data primitives, as JOSM does. The public methods and fields of the respective Java classes are available for scripting. In addition, there are JavaScript properties and functions mixed into the Java classes. They don't replace the native fields and methods, but they complete them with properties and and functions which are more "natural" to a script in a JavaScript-environment.

The following table lists the names of the basic Java classes for data primitives and the 
names of the corresponding JavaScript *mixins*.

<table id="table-primitives-and-mixins">
  <tr>
    <th>Kind of primitive</th>
    <th>Java class</th>
    <th>JavaScript mixin</th>
    <th>JavaScript builder class</th>
  </tr>
  <tr>
     <td>node</td>	
     <td><a data-josm-class="org.openstreetmap.josm.data.osm.Node">Node</a><br/>
     (extending <a data-josm-class="org.openstreetmap.josm.data.osm.OsmPrimitive">OsmPrimitive</a>)</td>
     <td><a data-js-object="mixin:NodeMixin">NodeMixin</a><br/>
      (extending <a data-js-object="mixin:NodeMixin">OsmPrimitiveMixin</a>)
     </td>
     <td><a data-js-object="class:NodeBuilder">NodeBuilder</a></td>
  </tr>
  <tr>
     <td>way</td>
     <td><a data-josm-class="org.openstreetmap.josm.data.osm.Way">Way</a><br/>
     (extending <a data-josm-class="org.openstreetmap.josm.data.osm.OsmPrimitive">OsmPrimitive</a>)</td>
     <td><a data-js-object="mixin:WayMixin">WayMixin</a><br/>
     (extending <a data-js-object="mixin:NodeMixin">OsmPrimitiveMixin</a>)</td>
     <td><a data-js-object="class:NodeBuilder">WayBuilder</a></td>
  </tr>
  <tr>
     <td>relation</td>
     <td><a data-josm-class="org.openstreetmap.josm.data.osm.Relation">Relation</a><br/>
     (extending <a data-josm-class="org.openstreetmap.josm.data.osm.OsmPrimitive">OsmPrimitive</a>)</td>
     <td><a data-js-object="mixin:RelationMixin">RelationMixin</a><br/>
     (extending <a data-js-object="mixin:NodeMixin">OsmPrimitiveMixin</a>)</td>
     <td><a data-js-object="class:NodeBuilder">RelationBuilder</a></td>
  </tr>
</table>
## Creating OSM primitives
Nodes, ways, and relations can be created by invoking one of the native constructors 
of the respective Java classes. Here are two basic examples: 

```js
var out = java.lang.System.out;

// Create a new node at position [12.45, 45.56]
var node = new org.openstreetmap.josm.data.osm.Node(
  new org.openstreetmap.josm.data.coor.LatLon(12.45, 45.56)
);
out.println("Created a node - id=" + node.getNumericId());

// Create a new empty relation with global id 12345 and global version 6
var Relation = org.openstreetmap.josm.data.osm.Relation;
var relation = new Relation(12345, 6);
out.println("Created a relation - id=" + relation.getNumericId());
```

Alternatively, the JOSM Scripting Plugin includes three <a data-js-object="module:josm/builder">builders</a> to create OSM primitives
in JavaScript, see overview table above. The primitives from the previous example
would be created as follows:

```js
var out = java.lang.System.out;
var builder= require("josm/builder");
var nb = builder.NodeBuilder;
var rb = builder.RelationBuilder;
    
// Create a new node at position [12.45, 45.56]
var node;
node = nb.withPosition(12.45,45.56).create();
// ... or ...
node = nb.create({lat: 12.45, lon: 45.56});
out.println("Created a node - id=" + node.getNumericId());

// Create a new  relation with global id 12345 and global version 6
var relation;
relation  = rb.withId(12345,6).create();
// .. or ..
relation = rb.create(12345, {version: 6});
out.println("Created a relation - id=" + relation.getNumericId());
```

## Setting and getting properties of OSM primitives

The native JOSM classes provide public setter and getter methods to set and get property values
on OSM primitives. You can either invoke them or set and get JavaScript
properties provided by the JavaScript mixins. As an example, the following table summarizes 
the various options for a nodes property <em>position</em>.</p>

<table>
  <tr>
    <th></th>
    <th>Native methods</th>
    <th>Mixin properties</th>
  </tr>
  <tr>
    <td style="vertical-align:top">set position</td>
    <td style="vertical-align:top">
    {% highlight javascript %}
node.setCoor(
  new LatLon(12.45, 45.56)
 );
{% endhighlight %}
   </td>
   <td style="vertical-align:top">
   {% highlight javascript %}
node.lat = 12.45;
node.lon = 45.56;
node.pos = {lat: 12.45, lon: 45.56};
node.pos = new LatLon(12.45, 45.56);
{% endhighlight %}
   </td>
  </tr>
  
    <tr>
    <td style="vertical-align:top">get position</td>
    <td style="vertical-align:top">
   {% highlight javascript %}
var lat = node.getCoor().$lat();
var lon = node.getCoor().$lon();
{% endhighlight %}
   </td>
   <td style="vertical-align:top">
   {% highlight javascript %}[
var lat, lon;
lat = node.lat; 
lat = node.pos.lat;
lon = node.lon;
lon = node.pos.lon;
{% endhighlight %}
   </td>
  </tr>
</table>

Similar options are availabe for other properties, check out the documentation for <a href="#table-primitives-and-mixins">the
native primitive classes and the JavaScript mixins</a>.

## Primitives and datasets
JOSM manages interconnected primitives in *datasets*, a kind of container for nodes, ways,
and relations. A primitive doesn't have to belong to a dataset, but if it does, it can belong to
at most one dataset. The dataset is said to be its *parent*.

The following table summarizes the name of the native Java class and the JavaScript mixin
representing data sets.

<table id="table-primitives-and-mixins">
  <tr>
    <th></th>
    <th>Java class</th>
    <th>JavaScript mixin</th>
  </tr>
  <tr>
     <td>data set</td>	
     <td><a data-josm-class="org.openstreetmap.josm.data.osm.DataSet">DataSet</a></td>
     <td><a data-js-object="mixin:DataSetMixin">DataSetMixin</a>
     </td>
  </tr>
</table>

There are two major differences between detached primitives and those attached to a dataset:


1.  on attached primitives more integrity constraints are checked.

    **Consequence:** what may work on detached primitive, may fail on an attached
  one

2. Data changes are notified to listeners listing on change events on the parent data set.

   **Consequence**: even simple property assignements on primitives may result in costly event
  propagation and UI refreshing.

  Consider to group batches of updates on attached primitives in a *batch* which 
  notifies listeners only once about data change events for the entire batch:
  ```js
  var ds = ... // assume ds is an already initialized data set
  // runs the updates on two primitives in a "batch"
  ds.batch(function() {
     ds.node(12345).lat = 12.34;
     ds.relation(67890).tags.name = "a new name";
  });
  ``` 

## Primitives and layers
JOSM provides an UI to display primitives and manipulate them interactively in *data layers*. 
If primitives are modified interactively, the respective changes can be *undone* and *redone*.

If you manipulate primitives attached to a dataset which is itself attached to a data layer, you
are better off to apply *data commands* to the primitives, instead of manipulating them directly.
For this purpose, the Scripting Plugin provides a <a data-js-object="module:josm/command">command API</a>.

```js
  var command = require("josm/command");
  var layer = josm.layers.get("my data layer");
  var ds = layer.data;
  
  // creates and applies two undoable/redoable commands 
  layer.apply(
	  command.change(ds.node(12345), {lat: 12.45}),
  	command.change(ds.relation(67890), {tags: {name: "a new name"}})

    // to remove a tag, set its value to null
    command.change(ds.way(87632), {tags: {width: null}})    
  );
```

## Find primitives in dataset

The easiest way to get hold on a primitive in a dataset is to access it by its unique numeric id.

```js
  var ds = .... // a dataset
  var node = ds.get("node", 12345); 
  // .. or
  node = ds.node(12345);
  
  var way = ds.get("way", 12345);
  // ... or
  way = ds.way(12345);
  
  var relation = ds.get("relation", -27222); // this is a local id
  // ... or
  relation = ds.relation(-27222);
```

In addition, you can *search in a dataset using the method `query()`. 
`query()` accepts two types of search expressions:

1.  a search expression as you would enter it in the JOSM search field
2.  a predicate as JavaScript function, which replies either true or false for a primitive


```js
  var ds = .... // a dataset
  // query the dataset with a predicate 
  var restaurants = ds.query(function(p) {
     p.tags.amenity == "restaurant";
  });
  
  // query the dataset with a JOSM search expression
  restaurants = ds.query("amenity=restaurant");
```
