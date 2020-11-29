---
layout: page
title: Using native Java
parent: API V2
nav_order: 3
---

# Scripting native JOSM classes and objects

The embedded `Graal.JS`scripting engine is tightly integrated with the Java type system. Native java classes, both from the Java SDK and from the [JOSM code base], are available for scripting.

You have to declare the Java class with `Java.type(...)`.


```js
// declare a Java type before using it
const LatLon = Java.type('org.openstreetmap.josm.data.coor.LatLon')

// invoke the constructor of a Java class as you would in a Java programm
const pos = new LatLon(12.34, 45.67)

// static methods provided by a Java class are available for scripting
const ok = LatLon.isValidLat(12.34)    // this is a native Java method

// instance methods of a java class are available for scripting, too
const displayName = pos.toDisplayString()        // this is a native Java method
```


[JOSM code base]: http://josm.openstreetmap.de/browser/josm/trunk/src"
[LatLon]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/coor/LatLon.html
[LatLonMixin]: /api/v1/josm_mixin_LatLonMixin.LatLonMixin.html
