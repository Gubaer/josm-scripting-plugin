#!/bin/sh
#
# run rhino
#

BASEDIR=$(dirname $0)/..
if [ ! -e $BASEDIR/env.sh ];  then
  echo "FATAL: 'env.sh' doesn't exist. Copy 'env.sh.distrib' to 'env.sh' and configure local settings."
  exit 1
fi
. $BASEDIR/env.sh
java -cp "$RHINO_CP" -Djosm-scripting-plugin.home="$JOSM_SCRIPTING_PLUGIN_HOME" org.mozilla.javascript.tools.shell.Main $*
