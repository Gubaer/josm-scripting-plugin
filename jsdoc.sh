#!/bin/bash
# 
# Creates the jsdoc documentation
#

prepare_output_dir() {
  local output_dir=$1

  mkdir -p "${output_dir}/v1"
  mkdir -p "${output_dir}/v2"
}

print_help() {
  echo "./jsdoc.sh [args]"
  echo "  -o | --outputdir <output-directory>"
  echo "     optional. default is 'out'"
  echo "  -a | --api-version <version>"
  echo "     optional. either 'v1' or 'v2'. default is 'v1'"
  echo "  -h | --help"
  echo "     print help information"
}

OUTPUT_DIR="out"
API_VERSION="v1"

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

    -a | --api-version)
      shift
      if [ $# -eq 0 ] ; then
        echo "error: missing api version" 1>&2
        print_help
        exit 1
      fi
      API_VERSION=$1
      if [[ "$API_VERSION" != "v1" && "$API_VERSION" != "v2" ]] ; then
        echo "error: illegal api version '$API_VERSION', expected 'v1' or 'v2'" 1>&2
        print_help
        exit 1
      fi
      shift
      ;;
    -h | --help )
      print_help
      exit 0
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

echo "Generating documentation for API version '$API_VERSION' in '$OUTPUT_DIR/$API_VERSION' ..."

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

node $JSDOC_HOME/jsdoc.js \
  -c jsdoc.${API_VERSION}.conf \
  -t doc/templates \
  -d $OUTPUT_DIR/$API_VERSION
