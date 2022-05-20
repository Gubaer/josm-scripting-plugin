---
layout: page
title: Using native Java
parent: API V1
nav_order: 3
---

{% include v1-deprecated.md %}

# Scripting native JOSM classes and objects
{: .no_toc }

1. TOC
{: toc }

## Using native Java classes and methods

The embedded scripting engine is tightly integrated with the Java type system. Native java classes, both from the Java SDK and the [JOSM code base], are available for scripting.

You can use a Java class's qualified class name in a script. JavaScript doesn't include the keyword `import`. You can't and don't have to import Java classes in a script. Declare a local variable referring to the Java class to avoid the fully qualified Java class names.

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

The JOSM Scripting Plugin ships with JavaScript **mixins** for some Java classes, particularly those used in scripts often.

A JavaScript mixin decorates a Java class with additional properties and methods implemented in JavaScript.

The following examples show how [LatLonMixin]{:target="apidoc"} decorates the native JOSM class [LatLon]{:target="apidoc"}.

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


[JOSM code base]: http://josm.openstreetmap.de/browser/josm/trunk/src"
[LatLon]: https://josm.openstreetmap.de/doc/org/openstreetmap/josm/data/coor/LatLon.html
[LatLonMixin]: /api/v1/josm_mixin_LatLonMixin.LatLonMixin.html
