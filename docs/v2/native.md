---
layout: page
title: Using native Java
parent: API V2
nav_order: 3
---

# Scripting native JOSM classes and objects

## Using native Java classes and methods

The embedded `Graal.JS`scripting engine is tightly integrated with the Java type system. Native java classes, both from the Java SDK and from the [JOSM code base], are available for scripting.

You have to declare 
You can use the fully qualified class names of a Java class in a script. JavaScript doesn't have a keyword `import`. You can't and you don't have to import Java classes in a script. Declare a local variable referring to the Java class, if you wan't to avoid the fully qualified
Java class names.

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
