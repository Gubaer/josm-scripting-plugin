#!/bin/bash
#
# manage.sh - manage the testing environment for the JOSM scripting plugin
#
# Run './manage.sh help' for help
#
#

function check_software() {
    local wget
    local jq

    wget=`which wget`
    if [ "$wget" = "" ] ; then 
        echo "fatal: this script requires 'wget'. Install it with 'sudo apt install -y wget'."
        return 1
    fi 

    jq=`which jq`
    if [ "$jq" = "" ] ; then 
        echo "fatal: this script requires 'jq'. Install it with 'sudo apt install -y jq'."
        return 1
    fi
    return 0
}

function download_josm() {
    local version
    local josm_file
    local download_url

    version=$1

    case "$version" in 
        "latest")
            josm_file="josm-latest.jar"
            ;;
        "tested")
            josm_file="josm-tested.jar"
            ;;
        *)
            josm_file="josm-snapshot-$version.jar"
            ;;
    esac
    
    download_url=`cat config.json | jq -r '."josm-params"."download-uri"'`
    echo "Downloading JOSM '$version' from '$download_url' ..."
    wget -O $josm_file "$download_url/$josm_file" 
}

function download_jdk() {
    local version
    local download_url
    local jq_query 
    local directory

    version=$1
    jq_query=".\"jdk-params\".\"$version\".\"directory\""
    directory=`cat config.json | jq -r "$jq_query"`
    if [ -d "$directory" ] ; then 
        echo "warning: JDK '$version' already installed in '$directory'. Skipping download."
        return 0
    fi
    
    jq_query=".\"jdk-params\".\"$version\".\"uri\""
    download_url=`cat config.json | jq -r "$jq_query"`
    echo "Downloading JDK '$version' from '$download_url' ..."
    wget -O "$version.tar.gz" $download_url
    tar xvf "$version.tar.gz"
    rm "$version.tar.gz"
    return 0
}

function download_graalvm() {
    local version
    local jq_query
    local directory
    local download_url

    version=$1

    jq_query=".\"graalvm-params\".\"$version\".\"directory\""
    directory=`cat config.json | jq -r "$jq_query"`
    if [ -d "$directory" ] ; then 
        echo "warning: GraalVM for JDK '$version' already installed in '$directory'. Skipping download."
        return 0
    fi

    jq_query=".\"graalvm-params\".\"$version\".\"uri\""
    download_url=`cat config.json | jq -r "$jq_query"`
    if [ "$download_url" = "" ] ; then 
        echo "error: no download URI found in ./config.json for GraalVM for JDK '$version'. Aborting download."
        return 1
    fi
    echo "Downloading GraalVM for JDK '$version' from '$download_url' ..."
    wget -O "graalvm-$version.tar.gz" $download_url
    tar xvf "graalvm-$version.tar.gz"
    rm "graalvm-$version.tar.gz"

    # install GraalJS
    $directory/bin/gu install js

    return 0
}

function download_graaljs() {
    local version 
    local is_valid_version
    local download_url
    local jq_query
    
    version=$1
    jq_query=".\"graaljs-params\" | has(\"$version\")"
    is_valid_version=`cat config.json | jq -r "$jq_query"`
    if [ "$is_valid_version" != "true" ] ; then 
        echo "error: unsupported GraalJS version '$version'. Aborting download"
        return 1
    fi
    if [ "$version" == "latest" ] ; then 
        jq_query=".\"graaljs-params\".latest"
        version=`cat config.json | jq -r "$jq_query"`
    fi
    jq_query=".\"graaljs-params\".\"$version\".uri"
    download_url=`cat config.json | jq -r "$jq_query"`
    if [ "$download_url" = "" ] ; then 
        echo "error: no download URI found in ./config.json for GraalJS '$version'. Aborting download."
        return 1
    fi
    jq_query=".\"graaljs-params\".\"$version\".directory"
    directory=`cat config.json | jq -r "$jq_query"`
    if [ -d "$directory" ] ; then 
        echo "warning: GraalJS '$version' already installed in '$directory'. Skipping download."
        return 0
    fi 
    mkdir -p $directory
    echo "Downloading GraalJS '$version' from '$download_url' ..."
    wget -O "graaljs-$version.zip" $download_url
    unzip "graaljs-$version.zip" -d $directory
    rm "graaljs-$version.zip"
    return 0
}

function create_josm_home() {
    local josm_home

    josm_home=`pwd`/josm-home
    if [ -d "$josm_home" ] ; then 
        echo "warning: JOSM home '$josm_home' already exists. Skipping."
        return 0
    fi
    mkdir -p $josm_home 
    echo "info: created JOSM home directory '$josm_home'"
}

function delete_josm_home() {
    local josm_home

    josm_home=`pwd`/josm-home
    if [ ! -d "$josm_home" ] ; then 
        echo "warning: JOSM home '$josm_home' doesn't exist. Skipping deleting it."
        return 0
    fi 
    rm -rf $josm_home
    echo "info: deleted JOSM home directory '$josm_home'"
}

function usage() {
    cat <<EOM
usage: manage.sh <action> <args>
    actions:
        help, -h
            display usage information

        prepare
            fully prepares the testing environment, by running download-josm for jdk11 and jdk17,
            running download-graalvm for jdk11 and jdk17, running download graaljs, 
            download-josm for latest and tested, and creating the josm home directory with
            create-josm-home

        download-josm latest|tested|<version>  
            download a JOSM version

        download-jdk jdk11|jdk17
            downloads a portable OpenJDK and installs it in the current directory

        download-graalvm jdk11|jdk17
            downloads a GraalVM for Windows and installs it in the current directory

        download-graaljs 
            downloads a GraalJS for Windows and installs it in the current directory

        clean-jars
            delete the downloaded JOSM versions

        clean
            clean all locally installed JOSM jars, JDKs, Graal VMs, and GraalJS
            distributions

        create-josm-home
            creates the JOSM home directory 

        delete-josm-home
            deletes the JOSM home directory

        update-scripting-jar
            updates the scripting jar file with the latest scripting jar
            in the build/dist directory
EOM
}

function prepare() {
    download_jdk "jdk11"
    download_jdk "jdk17"
    download_graalvm "jdk11"
    download_graalvm "jdk17"
    download_graaljs "latest"
    download_josm "latest"
    download_josm "tested"
    create_josm_home
}


function clean() {
    local jdks
    local jdk
    local jq_query
    local directory
    local num_jars
    local graal_js_versions
    local version

    # remove local JOSM jars
    num_jars=`ls *.jar 2>/dev/null | wc -l`
    if [ $num_jars -ne 0 ] ; then 
        rm *.jar
        echo "info: removed locally installed JOSM jars"
    else 
        echo "warning: no locally installed JOSM jars. Skipping them."
    fi

    # remove locally installed JDKs
    jdks=`cat config.json | jq -r ".\"jdk-params\" | keys | join(\" \")"`
    for jdk in $jdks 
    do
        jq_query=".\"jdk-params\".\"$jdk\".directory"
        directory=`cat config.json | jq -r "$jq_query"`
        if [ -d "$directory" ] ; then 
            rm -rf $directory 
            echo "info: deleted JDK '$jdk' in directory '$directory'"
        else 
            echo "warning: JDK '$jdk' not installed locally. Skipping it."
        fi
    done

    # remove locally installed Graal VMS
    jdks=`cat config.json | jq -r ".\"graalvm-params\" | keys | join(\" \")"`
    for jdk in $jdks 
    do
        jq_query=".\"graalvm-params\".\"$jdk\".directory"
        directory=`cat config.json | jq -r "$jq_query"`
        if [ -d "$directory" ] ; then 
            rm -rf $directory 
            echo "info: deleted GraalVM for JDK '$jdk' in directory '$directory'"
        else 
            echo "warning: GraalVM for JDK '$jdk' not locally installed. Skipping it."
        fi
    done

    # remove locally installed GraalJS distributions
    graal_js_versions=`cat config.json | jq -r ".\"graaljs-params\" | keys | join(\" \")"`
    for version in $graal_js_versions 
    do
        if [ "$version" != "latest" ] ; then 
            jq_query=".\"graaljs-params\".\"$version\".directory"
            directory=`cat config.json | jq -r "$jq_query"`
            if [ -d "$directory" ] ; then 
                rm -rf $directory 
                echo "info: deleted GraalJS version '$version' in directory '$directory'"
            else 
                echo "warning: GraalJS version '$version' not installed locally. Skipping it."
            fi
        fi
    done

    # delete JOSM home
    delete_josm_home
}

function update_scripting_jar() {
    jar_file=`pwd`/../../build/dist/scripting.jar
    if [ ! -f "$jar_file" ] ; then 
        echo "error: plugin jar file '$jar_file' doesn't exists. Build it first using '.\gradlew assemble' in the project directory"
        return 1
    fi 
    josm_home=`pwd`/josm-home
    if [ ! -d "$josm_home" ] ;  then 
        create_josm_home
    fi 
    mkdir -p "$josm_home/plugins"
    cp $jar_file "$josm_home/plugins" 
    echo "info: copied 'scripting.jar' from '$jar_file' into JOSM home at '$josm_home'"
}

check_software || exit 1

case "$1" in 
    "help" | "-h")
        usage
        exit 1
        ;;

    "download-josm")
        if [ "$2" == "" ] ; then
            echo "error: missing version"
            usage
            exit 1
        fi
        if [ "$2" == "latest" -o "$2" == "tested"  ] ; then 
            download_osm $2
        elif [ "$2" == "$(echo $2 | egrep '^[0-9]+$')" ] ; then
            download_josm $2
        else 
            echo "error: unsupported version '$2'"
            usage
            exit 1
        fi
        ;;

    "download-jdk")
        if [ "$2" == "" ] ; then 
            echo "error: missing version"
            usage
            exit 1
        fi 
        if [ "$2" == "jdk11" -o "$2" == "jdk17" ] ; then 
            download_jdk $2
        else 
            echo "error: unsupported jdk version '$2'"
            usage
            exit 1
        fi
        ;;

    "download-graalvm")
        if [ "$2" == "" ] ; then 
            echo "error: missing version"
            usage
            exit 1
        fi 
        if [ "$2" == "jdk11" -o "$2" == "jdk17" ] ; then 
            download_graalvm $2
        else 
            echo "error: unsupported jdk version '$2' for GraalVM"
            usage
            exit 1
        fi
        ;;

    "download-graaljs")
        if [ "$2" == "" ] ; then 
            echo "error: missing version"
            usage
            exit 1
        fi 
        download_graaljs $2
        ;;

    "prepare")
        prepare 
        ;;

    "clean")
        clean 
        ;;

    "create-josm-home") 
        create_josm_home
        ;;

    "delete-josm-home")
        delete_josm_home
        ;;

    "update-scripting-jar")
        update_scripting_jar
        ;;

    *)
        echo "error: unsupported command line argument" 
        usage
        exit 1
        ;;
esac

    