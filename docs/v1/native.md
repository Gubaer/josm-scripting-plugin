---
layout: page
title: NativeJava 
parent: API V1
nav_order: 3
---

{% include v1-deprecated.md %}

# Scripting native JOSM classes and objects

## Using native Java classes and methods

The embedded scripting engine is tightly integrated with the Java type system. Native java classes, both from the Java SDK and from the <a href="http://josm.openstreetmap.de/browser/josm/trunk/src">JOSM code base</a>, are available for scripting.

You can use the fully qualified class names of a Java class in a script. JavaScript doesn't have a keyword `import`. You can't and you don't have to import Java classes in a script. 
Declare a local variable referring to the Java class, if you wan't to avoid the fully qualified
Java class names.

```js
// invoke the constructor of a Java class as you would in a Java programm
var pos = new org.openstreetmap.josm.data.coor.LatLon(12.34, 45.67);

// instead of "importing" a class, you can declare a local variable
// referring to it
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

// static methods provided by a Java class are available for scripting
var ok = LatLon.isValidLat(12.34);    // this is a native Java method

// instance methods of a java class are available for scripting, too
var pos2 = new LatLon(12.34, 45.67);
var s = pos.toDisplayString();        // this is a native Java method
```

## Enriched Java classes &ndash; Mixins

The JOSM Scripting Plugin ships with JavaScript **mixins** for a selection of
Java classes, in particular for those, which are often used in scripts.
A JavaScript mixin decorates a Java class with additional properties and methods, 
which are implemented in JavaScript.

The following examples show how <a data-js-object="mixin:LatLonMixin">LatLonMixin</a> decorates the native JOSM class <a data-josm-class="org.openstreetmap.josm.data.coor.LatLon">LatLon</a>.</p>

```js
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

//This invokes the native constructor of the native Java class LatLon 
var pos1 = new LatLon(12.34, 45.67);

// LatLonMixin implements an additional static method 'make'
// which isn't available in the native Java class, but which can
// nevertheless be invoked like a static method
var pos2 = LatLon.make({lat: 12.34, lon: 45.67});

// LatLonMixin implements a (read-only) property 'lat', which
// is implemted by the mixin. There is neither a native getter method
// 'getLat()' nor a native public fiedl 'lat'.
var lat = pos1.lat;

// In this special case, there is a native public method 'lat()'.
// If you wan't to invoke it instead of accessing
// the property 'lat', add the prefix '$'.  
lat = pos1.$lat();

// In fact, you can invoke *any* native public method with a '$' as prefix.
var name = pos1.toDisplayString();     
name = pos1.$toDisplayString();
```
