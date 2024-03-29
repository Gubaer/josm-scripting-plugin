#!/bin/bash
#
# Creates the jsdoc documentation
#

prepare_output_dir() {
  local output_dir=$1

  mkdir -p "${output_dir}/v3"
}

print_help() {
  echo "./jsdoc.sh [args]"
  echo "  -o | --outputdir <output-directory>"
  echo "     optional. default is 'out'"
  echo "  -h | --help"
  echo "     print help information"
}

# default output directory
OUTPUT_DIR="out"

while [ $# -gt 0 ] ;
do
  arg=$1
  case $arg in
    -o | --output-dir)
      shift
      if [ $# -eq 0 ] ; then
        echo "error: missing outputdir" 1>&2
        print_help
        exit 1
      fi
      OUTPUT_DIR=$1
      shift
      ;;

    *)
      echo "error: unsupported command line argument '$arg'" 1>&2
      print_help
      exit 1
      ;;
  esac
done

prepare_output_dir $OUTPUT_DIR

# the path to the jsdoc templates and project specific js modules
TEMPLATE_PATH=$(pwd)/docstrap/template
#TEMPLATE_PATH=$HOME/repositories/docstrap/template

# add the template path to the node path if not yet present
if [ "$(echo $NODE_PATH | tr ":" "\n" | grep $TEMPLATE_PATH)" == "" ] ; then
	export NODE_PATH=$NODE_PATH:$TEMPLATE_PATH
fi

JSDOC=`which jsdoc`
if [ "$JSDOC" == "" ] ; then
	echo "error: jsdoc not found"
	echo "Make sure jsdoc is installed. See README.md"
	exit 1
fi

echo "Generating documentation for API version 'v3' in '$OUTPUT_DIR/v3' ..."
jsdoc \
    -c jsdoc.v3.conf \
    -t $TEMPLATE_PATH \
    -d $OUTPUT_DIR/v3
