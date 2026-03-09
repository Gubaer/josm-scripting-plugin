# JOSM Scripting Plugin

The JOSM scripting plugin executes scripts in the [Open Street Map][osm] editor [JOSM][josm].

Scripts can be written in any scripting language for which a [JSR-223][jsr223] compatible script engine is available, in  particular in
* [JavaScript][javascript]
* [Groovy][groovy]
* [Ruby][ruby]
* [Python][python]

If the [GraalJS][graal-js] engine is on the classpath, you can use it to execute scripts in JavaScript. The plugin includes a JavaScript library to manage layers, edit OSM primitives, load and save data, upload data to the OSM server, and download primitives from the OSM server. Refer to the [API V3 documentation][api-v3].

Until release v0.2.10 the plugin included [Mozilla Rhino][rhino] as a scripting engine and a JavaScript library [API V1][api-v1] to be used with Mozilla Rhino. Starting from release v0.3.0  [Mozilla Rhino][rhino] **is not included anymore**. If your current scripts use [API V1][api-v1], migrate them to [API V3][api-v3], and change to [GraalJS][doc-graaljs].

## Install
Use JOSMs plugin manager to install the scripting plugin and keep it up to date.

1. Select Preferences -> Plugins
2. Search for the plugin **scripting** and install it

The scripting plugin requires Java 17 or higher.

## Documentation

* [JOSM Scripting Plugin documentation][doc-home]
* [API V3 documentation][api-v3] with [API V3 library doc][libdoc-api-v3]

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

Translatable strings are uploaded to [Transifex][transifex] (project `josm`, resource
`josm-plugin_scripting`). Translations are downloaded from Transifex and committed to
the repository under `src/main/po/<lang>.po`.

### One-time setup

**1. Install the Transifex CLI**

Download and install the official Transifex CLI binary:

```bash
curl -o- https://raw.githubusercontent.com/transifex/cli/master/install.sh | bash
```

This installs the `tx` binary into `~/bin`. Make sure `~/bin` is on your `PATH`.

**2. Install gettext tools**

The `xgettext` tool (part of the `gettext` package) is required to extract translatable
strings from the Java source files:

```bash
sudo apt install gettext
```

**3. Configure Transifex credentials**

Create an API token on Transifex: https://app.transifex.com/user/settings/api/

Then add the `TX_TOKEN` to the `.env` file.

### Extracting translatable strings

Run `xgettext` to scan the Java source files and write the PO template to
`build/i18n/josm-plugin_scripting.pot`:

```bash
mkdir -p build/i18n
rm -f build/i18n/josm-plugin_scripting.pot
find src/main/java -name "*.java" | xgettext \
  --language=Java \
  --from-code=UTF-8 \
  --keyword=tr:1 \
  --keyword=trn:1,2 \
  --keyword=trc:2c,1 \
  --add-comments \
  --sort-by-file \
  --package-name=josm-plugin_scripting \
  --files-from=- \
  --output=build/i18n/josm-plugin_scripting.pot
```

### Uploading strings to Transifex

Push the generated template so translators can work on it:

```bash
tx push --source
```

### Downloading translations

Transifex hosts translations for many languages, most of which may be only partially
translated. Use `--minimum-perc` to skip languages below a translation coverage
threshold (e.g. 10%):

```bash
tx pull --all --force --minimum-perc=20
```

To download a single language (e.g. German):

```bash
tx pull -l de
```

Translations are written to `src/main/po/<lang>.po`. Commit the updated files:

```bash
git add src/main/po
git commit
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

# generate the API documentation into the 'gh-pages' branch and publish it
# First checkout the 'gh-pages' branch into ../josm-scripting-plugin.gh-pages
$ ./jsdoc.sh \
    --output-dir ../josm-scripting-plugin.gh-pages/api
$ cd ../josm-scripting-plugin.gh-pages
$ git commit -a -m "Regenerate API doc"
```

## Build status
[![Josm Scripting Plugin - Build][build-batch]][build-status]

## Credits
The JOSM scripting plugin uses:

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
[direnv]:https://direnv.net/