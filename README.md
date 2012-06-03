# JOSM Scripting Plugin

The JOSM scripting plugin executes scripts in in the [Open Street Map](http://www.openstreetmap.org) editor
[JOSM](http://josm.openstreetmap.de/). 

Scripts can be defined in any scripting language, for which a 
[JSR-223](http://www.jcp.org/aboutJava/communityprocess/pr/jsr223/) compatible scripting engine is available, in 
particular in 
* [JavaScript](http://en.wikipedia.org/wiki/JavaScript)
* [Groovy](http://groovy.codehaus.org/)
* [Ruby](http://www.ruby-lang.org/en/)
* [Python](http://www.python.org/)

## For JOSM users
The scripting plugin can be installed and kept up to date using JOSMs native plugin manager, 
see [installation instructions](http://gubaer.github.com/josm-scripting-plugin/index.html#install).

## For developers
The scripting plugin includes an embedded scripting engine for JavaScript based on 
[Mozilla Rhino](http://www.mozilla.org/rhino/).
It provides a custom JavaScript API to write scripts for the JOSM editor, please refer to 
the [API documentation](http://gubaer.github.com/josm-scripting-plugin/).

In addition, scripts in a number of other scripting languages can be run within JOSM, see 
[these examples](https://github.com/Gubaer/josm-scripting-plugin/tree/master/scripts). 

If you want to contribute to the scripting plugin itself, please fork this repository and
submit your pull requests.


## Credits
The JOSM scripting plugin uses: 

* jsyntaxpane by Ayman Al-Sairafi. See [README.jsyntaxpane](master/README.jsyntaxpane) and [LICENSE.jsyntaxpane](master/LICENSE.jsyntaxpane)
* [Rhino](http://www.mozilla.org/rhino/) scripting engine by Mozilla Foundation. See [README.rhino](master/README.rhino) and [LICENSE.rhino](master/LICENSE.rhino)











 