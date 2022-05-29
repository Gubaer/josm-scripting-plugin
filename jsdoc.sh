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
  echo "     optional. either 'v1', 'v2', or 'all'. default is 'all'"
  echo "  -h | --help"
  echo "     print help information"
}

# default output directory
OUTPUT_DIR="out"

# default API version
API_VERSION="all"

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
      if [[ "$API_VERSION" != "v1" && "$API_VERSION" != "v2" && "$API_VERSION" != "all" ]] ; then
        echo "error: illegal api version '$API_VERSION', expected 'v1, 'v2', or 'all'" 1>&2
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


# the path to the jsdoc templates and project specific js modules
TEMPLATE_PATH=$(pwd)/docstrap/template

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

case "$API_VERSION" in
  v1 | v2)
    echo "Generating documentation for API version '$API_VERSION' in '$OUTPUT_DIR/$API_VERSION' ..."
    jsdoc \
        -c jsdoc.${API_VERSION}.conf \
        -t $TEMPLATE_PATH \
        -d $OUTPUT_DIR/$API_VERSION

    ;;

  all)
    echo "Generating documentation for API version 'v1' in '$OUTPUT_DIR/v1' ..."
    jsdoc \
        -c jsdoc.v1.conf \
        -t $TEMPLATE_PATH \
        -d $OUTPUT_DIR/v1

    echo "Generating documentation for API version 'v2' in '$OUTPUT_DIR/v2' ..."
    jsdoc \
        -c jsdoc.v2.conf \
        -t $TEMPLATE_PATH \
        -d $OUTPUT_DIR/v2

    ;;
esac