---
layout: page
title: Using GraalJS
nav_order: 2
---

# Using GraalJS in the scripting plugin

Starting with release **v0.2.0** the JOSM Scripting Plugin requires Java 11 (or higher). To use the plugin, you have to run JOSM with a Java 11 compatible JDK.

You have two options:

1. Run JOSM with the Java 11 (or Java 17) compatible JDK provided by [Oracles GraalVM][graalvm]

2. Run JOSM with another Java 11 (or higher) compatible JDK, for instance with the [OpenJDK][openjdk]

# Running JOSM with GraalJS

## Running JOSM with the GraalVM JDK

GraalVM ships with an engine for JavaScript (compatible with EMCAScript 2021) called [GraalJS][graaljs]. When you run JOSM with the JDK provided by the GraalVM, GraalJS
is available in the JOSM Scripting Plugin and you can execute scripts written in JavaScript using GraalJS.

1. Download [GraalVM 22.1][graalvm-22-download] (or higher) and install it.

    * Make sure the environment variable `JAVA_HOME` refers to your GraalVM installation.
    * Make sure the java binaries (in particular `java`) shipped with the GraalVM are
      on your path (the `PATH` environment variable has to be set).

    Refer to the [GraalVM quickstart guide][graalvm-quickstart] for additional help.

2. Check your installation with the command `java -version`. It should output information about the Java SDK shipped by the GraalVM, for instance:

    ```
    openjdk version "11.0.15" 2022-04-19
    OpenJDK Runtime Environment GraalVM CE 22.1.0 (build 11.0.15+10-jvmci-22.1-b06)
    OpenJDK 64-Bit Server VM GraalVM CE 22.1.0 (build 11.0.15+10-jvmci-22.1-b06, mixed mode, sharing)
    ```

3. Launch JOSM

    In the simplest case:

    ```bash
    # make sure 'java' is resolved to the java binary shipped with the GraalVM
    java -jar josm.jar
    ```

    Add additional command line options, if necessary, for instance:

    ```bash
    # make sure 'java' is resolved to the java binary shipped with the GraalVM
    java -Djosm.home=/your/josm/home -Xmx2G -jar josm.jar
    ```

## Running JOSM with another JDK and GraalJS as java library

JDKs other than GraalVM don't include GraalJS.


## Using the JavaScript [API V2](v2/v3)

The JOSM Scripting Plugin ships with a JavaScript API. [API V2](v2/v2) can only be used in the GraalJS scripting engine. Neither Mozilla Rhino nor Nashorn can execute scripts based on [API V2](v2/v2) (because the API V2 includes a `require()` function to load Common-JS modules, which is specific for GraalJS).

[API V2](v2/v2) has the following main features:

* loading and using CommonJS-compatible modules
* lookup, add, delete, and rename layers
* load data into a layer, save data from a layer
* download objects and changesets from the OSM server, upload objects and changesets to the OSM server
* create data sets
* create nodes, ways, and relations in a data set
* update nodes, ways, and relations
* query data sets for nodes, ways, or relations
* create undoable/redoable change objects to update nodes, ways, and relations
* write to the scripting console

Refer to the [API V2](v2/v2) documentation.



[graalvm]: https://www.graalvm.org/
[openjdk]: https://openjdk.java.net/
[graalvm-22-download]: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-22.1.0
[graalvm-quickstart]: https://www.graalvm.org/java/quickstart/
[graaljs]: https://github.com/oracle/graaljs
