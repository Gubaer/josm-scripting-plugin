---
layout: home
title: Home
nav_order: 1
---


# JOSM Scripting Plugin
## Run scripts in the Open Street Map editor JOSM

The JOSM Scripting Plugin is a plugin for the [Open Street Map]{:target="external"} editor [JOSM]{:target="external"}.

It includes a built-in scripting engine for Javascript based on [Mozilla Rhino]{:target="external"} and an 
<!-- TODO: fix link -->
[API for Javascript](apidoc/namespaces/josm.html)
to manipulate JOSMs internal application objects.

{% highlight javascript %}
let command = require("josm/command")
let nodeBuilder = require("josm/builder").NodeBuilder
josm.layers.activeLayer.apply(
    command.add(
      nodeBuilder
        .withTags({amenity: "restaurant"})
        .withPosition(12.34,45.67)
        .create()
  )
)
josm.alert("Added a node")
{% endhighlight %}

<img id="console-img" src="assets/img/scripting-console-sample.png"/>

It also executes Python scripts and can <a href="doc/python.html">load and execute plugins</a>
written in Python.

In addition, it can execute scripts written in [Groovy], [Ruby], or any other language for which a JSR-223 compatible script engine is available.	


[Open Street Map]: http://www.openstreetmap.org
[JOSM]: http://josm.openstreetmap.de
[Mozilla Rhino]: http://www.mozilla.org/rhino/
[Groovy]: http://groovy.codehaus.org/
[Ruby]: http://www.ruby-lang.org