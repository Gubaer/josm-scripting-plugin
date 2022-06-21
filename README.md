# JOSM Scripting Plugin

The JOSM scripting plugin executes scripts in the [Open Street Map][osm] editor [JOSM][josm].

Scripts can be written in any scripting language for which a [JSR-223][jsr223] compatible script engine is available, in  particular in
* [JavaScript][javascript]
* [Groovy][groovy]
* [Ruby][ruby]
* [Python][python]

If the [GraalJS][graal-js] engine is on the classpath, you can use it to execute scripts in JavaScript. The plugin includes a JavaScript library to manage layers, edit OSM primitives, load and save data, upload data to the OSM server, and download primitives from the OSM server. Refer to the [API V3 documentation][api-v3].

The plugin includes [Mozilla Rhino][rhino] as a scripting engine for historical reasons. Mozilla Rhino is deprecated in the scripting plugin. The plugin authors will remove it at the end of 2022. The plugin also includes a JavaScript library [API V1][api-v1] to be used with Mozilla Rhino. It is deprecated too. The plugin authors will remove it at the end of 2022 with Mozilla Rhino. If your current scripts use [API V1][api-v1], migrate them to [API V2][api-v2], and change to [GraalJS][doc-graaljs].

## Install
Use JOSMs plugin manager to install the scripting plugin and keep it up to date.

1. Select Preferences -> Plugins
2. Search for the plugin **scripting** and install it

The scripting plugin requires Java 11 or higher.

## Documentation

* [JOSM Scripting Plugin documentation][doc-home]
* [API V3 documentation][api-v3] with [API V3 library doc][libdoc-api-v3]
* [API V1 documentation (deprecated)][api-v1] with [API V1 library doc][libdoc-api-v1]


## How to build

Add a new entry to [releases.yml](releases.yml) then run:

```bash
#
# Assumes that a release 'v9.9.9' is to be built
#

# build the plugin
$ ./gradlew clean build

# tag the release
$ git tag v9.9.9
$ git push origin v9.9.9

# create a GitHub release for the current release (the
# most recent release in releases.yml)
$ ./gradlew createGithubRelease

# publish the scripting.jar to the current GitHub release (the
# most recent release in releases.yml)
#
# The new GitHub release becomes the 'latest' GitHub release
# for the scripting plugin. The JOSM plugin registry automatically
# picks up the new version from this 'latest' release.
$ ./gradlew publishToGithubRelease
```

## How to test

There are two suites of unit tests:
1. a suite of unit tests implemented in Groovy
2. a suite of unit tests implemented in JavaScript which provide test cases for the JavaScript API

How to run:
```bash
# build the plugin and run the tests
$ ./gradlew build
# ... or run the checks only, without building
$ ./gradlew cleanTest cleanTestScriptApi check
```

## How to update the i18n resources
Localized strings are uploaded to [Transifex][transifex].

Translated resources can be downloaded periodically from Transifex and then commited to the github repository.

```bash
# install the transifex client
# see transifex documentation: https://docs.transifex.com/client/installing-the-client
$ sudo apt install python3-pip
$ sudo pip3 install transifex-client

# createa an API key for transifex, see https://docs.transifex.com/api/introduction
# create a file with the transifex api key
$ touch $HOME/.transifexrc

# edit $HOME/.transifexrc and add the following content
[https://www.transifex.com]
api_hostname = https://api.transifex.com
hostname = https://www.transifex.com
password = <the transifex api key>
username = api

# For new languages, or when updating only certain languages:
# Download the german translations into src/main/po/de.po
# Shorten the de.po file, removes unnecessary parts
# Then commit it to git
$ tx pull -l de
$ ./gradlew shortenPoFiles
$ git stage src/main/po
$ git commit

# For existing languages:
# Downloads translations for all existing languages into src/main/po/
$ ./gradlew transifexDownload
$ git stage src/main/po
$ git commit

# build the plugin
$ ./gradlew build
```

## How to generate the API documentation

### Required software
```bash
# install nvm
$ wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.1/install.sh | bash
# install npm and node
$ nvm install --lts
# install the fs-extra module
$ npm install fs-extra
# install jsdoc
$ npm install -g jsdoc
# install dependencies for docstrap
$ cd docstrap && npm install
```

### Generate the API documentation
```bash
# generate the API documentation for the v1 API. Documentation is written
# to out/v1
$ ./jsdoc.sh --api-version v1

# generate the API documentation for the v2 API in a temporary directory
$ ./jsdoc.sh --api-version v2 --output-dir /tmp/api-v2

# generate the complete API documentation into the 'gh-pages' branch
# and publish it
# First checkout the 'gh-pages' branch into ../josm-scripting-plugin.gh-pages
$ ./jsdoc.sh \
    --api-version all \
    --output-dir ../josm-scripting-plugin.gh-pages/api
$ cd ../josm-scripting-plugin.gh-pages
$ git commit -a -m "Regenerate API doc"
```

## Build status
[![Josm Scripting Plugin - Build][build-batch]][build-status]

## Credits
The JOSM scripting plugin uses:

* [Mozilla Rhino][rhino] scripting engine by Mozilla Foundation
* [RSyntaxTextArea][rsyntaxtextarea]
* [Graal.JS][graal-js]

* supported by [![JetBrains logo](jetbrains.png)](https://www.jetbrains.com/?from=JOSM%20Scripting%20Plugin) with a free JetBrains Open Source license


## License
Published under GPL Version 3 and higher. See included [LICENSE](LICENSE) file.


[transifex]:https://www.transifex.com
[osm]:http://www.openstreetmap.org
[josm]:http://josm.openstreetmap.de/
[jsr223]:http://www.jcp.org/aboutJava/communityprocess/pr/jsr223/
[javascript]:http://en.wikipedia.org/wiki/JavaScript
[groovy]:http://groovy.codehaus.org/
[ruby]:http://www.ruby-lang.org/en/
[python]:http://www.python.org/
[rhino]:http://www.mozilla.org/rhino/
[script-examples]:https://github.com/Gubaer/josm-scripting-plugin/tree/master/src/main/resources/scripts
[graal-js]:https://github.com/oracle/graaljs
[api-v2]:http://gubaer.github.io/josm-scripting-plugin/docs/v2/v2.html
[api-v3]:http://gubaer.github.io/josm-scripting-plugin/docs/v3/v3.html
[libdoc-api-v2]:http://gubaer.github.io/josm-scripting-plugin/api/v2/module-josm.html
[libdoc-api-v3]:http://gubaer.github.io/josm-scripting-plugin/api/v3/module-josm.html
[api-v1]:http://gubaer.github.io/josm-scripting-plugin/docs/v1/v1.html
[libdoc-api-v1]:http://gubaer.github.io/josm-scripting-plugin/api/v1/module-josm.html
[doc-graaljs]:http://gubaer.github.io/josm-scripting-plugin/docs/graaljs.html
[jsyntaxarea]:https://bobbylight.github.io/RSyntaxTextArea/
[rsyntaxtextarea]:https://bobbylight.github.io/RSyntaxTextArea/
[doc-home]:http://gubaer.github.io/josm-scripting-plugin/
[build-batch]:https://github.com/Gubaer/josm-scripting-plugin/actions/workflows/gradle.yml/badge.svg
[build-status]:https://github.com/Gubaer/josm-scripting-plugin/actions/workflows/gradle.yml