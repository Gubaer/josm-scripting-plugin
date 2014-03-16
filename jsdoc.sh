#!/bin/bash
# 
# Creates the jsdoc documentation
#
# Usage:
#   jsdoc.sh  [outputdir]
#

OUTDIR=$1
: ${OUTDIR:="out"}

echo "Generating documentation to <$OUTDIR> ..."
../jsdoc/jsdoc -c jsdoc.conf -t doc/templates -d $OUTDIR
