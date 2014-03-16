# JOSM Scripting Plugin

The JOSM scripting plugin executes scripts in the [Open Street Map](http://www.openstreetmap.org) editor
[JOSM](http://josm.openstreetmap.de/). 

Scripts can be defined in any scripting language for which a 
[JSR-223](http://www.jcp.org/aboutJava/communityprocess/pr/jsr223/) compatible script engine is available, in 
particular in 
* [JavaScript](http://en.wikipedia.org/wiki/JavaScript)
* [Groovy](http://groovy.codehaus.org/)
* [Ruby](http://www.ruby-lang.org/en/)
* [Python](http://www.python.org/)

## For JOSM users
The scripting plugin can be installed and kept up to date using JOSMs plugin manager:

1. Select Preferences -> Plugins
2. Search for the plugin "Scripting" and install ist 

## For developers
The scripting plugin includes an embedded scripting engine for JavaScript based on 
[Mozilla Rhino](http://www.mozilla.org/rhino/).
It provides a custom JavaScript API to write scripts for the JOSM editor, please refer to 
the [API documentation](http://gubaer.github.com/josm-scripting-plugin/).

Furthermore, it can load and execute [plugins written in Python](http://gubaer.github.com/josm-scripting-plugin/doc/python.html).

In addition, it can execute scripts written in Ruby, Groovy, and other languages, refer to 
[these examples](https://github.com/Gubaer/josm-scripting-plugin/tree/master/scripts). 

If you want to contribute to the scripting plugin itself, please fork this repository and
submit your pull requests.

## How to build

```bash
% git checkout deploy          # switch to deploy branch

# edit build.gradle and add a new pair with a plugin build number and a
# JOSM build number

% git clean build              # build the plugin
% git deploy                   # deploys the plugin jar to github,
                               # where it is picked up by the JOSM
                               # plugin installer
```

## Credits
The JOSM scripting plugin uses: 

* jsyntaxpane by Ayman Al-Sairafi
* [Rhino](http://www.mozilla.org/rhino/) scripting engine by Mozilla Foundation

## License
Published under GPL Version 3 and higher. See included LICENSE file.
