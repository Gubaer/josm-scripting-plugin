#!/bin/sh
#
# run rhino
#

if [ ! -e ./env.sh ];  then
  echo "FATAL: 'env.sh' doesn't exist. Copy 'env.sh.distrib' to 'env.sh' and configure local settings."
  exit 1
fi
. ./env.sh
java -cp $RHINO_JAR -Djosm-scripting-plugin.home="$JOSM_SCRIPTING_PLUGIN_HOME" org.mozilla.javascript.tools.shell.Main $*
