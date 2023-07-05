This directory provides an environment to interactively test JOSM and the scripting plugin with different JOSM, JDK, GraalVM, and GraalJS versions.

Use [`manage.ps1](manage.ps1) to prepare and manage the test environment, in particular to

* download and install JOSM versions
* download and install JDKs (jdk11 and jdk17)
* download and install GraalVMs for jdk11 or jdk17
* download and install GraalJS versions
* manage a JOSM_HOME for the testing environment (create and init, delete)
* deploy the current scripting plugin (the locally built `scripting.jar`) to the testing environment

Run `manage.ps1 -help` for additional information.

Use [`josm.ps`](josm.ps1) to launch JOSM and the scripting plugin either with a stock JDK (with or without GraalJS) or with a GraalVM version.

Run `josm.ps1 -help` for additional information.

If you get an error *execution of scripts is disabled on this system* run the following command in the PowerShell console:
```powershell
# enable execution of the scripts on the local machine
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope LocalMachine
```