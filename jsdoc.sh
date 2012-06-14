#!/bin/sh
# 
# Derived from the original 'jsdoc' shell script
#

BASEDIR=$(dirname $0)
if [ ! -e $BASEDIR/env.sh ];  then
  echo "FATAL: 'env.sh' doesn't exist. Copy 'env.sh.distrib' to 'env.sh' and configure local settings."
  exit 1
fi

. $BASEDIR/env.sh 

if [ "$JSDOC3_HOME" = "" ]; then
   echo "FATAL: environment variable JSDOC3_HOME not set. Edit the configuration file 'env.sh'"
   exit 1
elif ! [ -e "$JSDOC3_HOME" ]; then 
   echo "FATAL: jsdoc3 directory '$JSDOC3_HOME' doesn't exist. Check configuration in  'env.sh'"
   exit 1
fi
 
# rhino discards the path to the current script file, so we must add it back
SOURCE="$0"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
java -classpath ${JSDOC3_HOME}/lib/js.jar org.mozilla.javascript.tools.shell.Main -modules ${JSDOC3_HOME}/node_modules -modules ${JSDOC3_HOME}/rhino_modules -modules ${JSDOC3_HOME} -modules doc/template ${JSDOC3_HOME}/jsdoc.js --dirname=${JSDOC3_HOME} $@

