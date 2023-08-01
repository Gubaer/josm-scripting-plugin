This directory provides an environment to interactively test JOSM and the scripting plugin with different JOSM, JDK, GraalVM, and GraalJS versions.

Use [`manage.sh](manage.sh) to prepare and manage the test environment, in particular to

* download and install JOSM versions
* download and install JDKs (jdk11 and jdk17)
* download and install GraalVMs for jdk11 or jdk17
* download and install GraalJS versions
* manage a JOSM_HOME for the testing environment (create and init, delete)
* deploy the current scripting plugin (the locally built `scripting.jar`) to the testing environment

Run `manage.sh help` for additional information.

Typical usage:
```bash
# prepare the testing environment, download JOSM jars, JDKs, GraalVMs, and
# a GraalJS distribution. Install them locally in this testing directory
$ ./manage.sh prepare

# clean up the testing environment. Remove all downloaded JOSM jars, JDKs,
# GraalVMs, and GraalJS distributions.
$ ./manage.sh clean
```

Use [`josm.sh`](josm.sh) to launch JOSM and the scripting plugin either with a stock JDK (with or without GraalJS) or with a GraalVM version.

Run `josm.sh help` for additional information.
```