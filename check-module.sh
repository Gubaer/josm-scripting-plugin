#!/bin/sh
#
# Loads a module given the module name. 
#

BASEDIR=$(dirname $0)
if [ ! -e $BASEDIR/env.sh ];  then
  echo "FATAL: 'env.sh' doesn't exist. Copy 'env.sh.distrib' to 'env.sh' and configure local settings."
  exit 1
fi
. $BASEDIR/env.sh

if [ $# != 1 ]; then
   echo "usage: check-module.sh module-name"
   exit 1 
fi

MODULE=$1

java -cp $RHINO_CP -Djosm-scripting-plugin.home="$JOSM_SCRIPTING_PLUGIN_HOME" org.mozilla.javascript.tools.shell.Main  -f - <<EOS
load("javascript/require.js");
require.addRepository("$JOSM_SCRIPTING_PLUGIN_HOME/javascript");
var module = require("$MODULE");
for (var p in module) {
  print("exports: " + p);
}
EOS


