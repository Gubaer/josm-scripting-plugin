#!/bin/bash
# 
# Creates the jsdoc documentation
#
# Usage:
#   jsdoc.sh  [outputdir]
#

OUTDIR=$1
: ${OUTDIR:="out"}

# the path where jsdoc is installed 
# If not installed, clone it from github: 
#   $ git clone https://github.com/jsdoc3/jsdoc
JSDOC_HOME=../jsdoc

# the path to the jsdoc templates and project specific js modules
TEMPLATE_PATH=$(pwd)/doc/templates

# add the template path to the node path if not yet present
if [ "$(echo $NODE_PATH | tr ":" "\n" | grep $TEMPLATE_PATH)" == "" ] ; then
	export NODE_PATH=$NODE_PATH:$TEMPLATE_PATH
fi

if [ ! -f $JSDOC_HOME/jsdoc.js ] ;  then
	echo "error: $JSDOC_HOME/jsdoc.js not found"
	echo "Make sure jsdoc is installed and configure JSDOC_HOME in this script."
	echo "Clone jsdoc from github to install it."
	echo "==>  $ git clone https://github.com/jsdoc3/jsdoc"
	exit 1
fi

echo "Generating documentation to '$OUTDIR' ..."
node $JSDOC_HOME/jsdoc.js -c jsdoc.conf -t doc/templates -d $OUTDIR
