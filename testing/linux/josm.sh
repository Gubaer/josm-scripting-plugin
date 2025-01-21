#!/bin/bash
#
# josm.sh - launch a specific JOSM version with a stock JDK and optionally with a
#   a GraalJS distribution, or with a Graal VM.
#
# Run './josm.sh -h' for help
#
#
source ./lib.sh

function usage() {
    cat <<EOM;
usage: josm.sh <options>
    -h, --help, help
        show this information

    --josm latest | tested | <version>
        The JOSM version to lauch. Either 'latest', 'tested', or a JOSM version number.
        If missing, 'latest' is used.

    --jdk jdk17 | jdk21
        The JDK to use. Either 'jdk17' or 'jdk21'. If missing, 'jdk17' is used.

    --use-graal-vm
        If present, JOSM is started with the GraalVM. The GraalVM version is chosend depending on
        the parameter '--jdk'

    --graal-js latest | <version>
        The GraalJS version to be loaded. Either 'latest' or a GraalJS version configured in 'config.json'.
        If missing, no GraalJS version is loaded. GraalJS can't be used toghether with the GraalVM,
        only with a stock JDK.
EOM
}


function prepare_logging_properties() {
    local log_level
    log_level=$1 || "INFO"

    cat > logging.properties <<EOF
handlers=java.util.logging.ConsoleHandler
.level=WARNING
java.util.logging.ConsoleHandler.level=$log_elvel
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
org.openstreetmap.josm.plugins.scripting.level=$log_level
EOF

}

check_software || exit 1

josm="latest"
jdk="jdk17"
use_graal_vm=false
graal_js=""

while [ "$1" != "" ] ; do
    case "$1" in
        -h | --help | help)
            usage
            exit 0
            ;;

        --josm)
            shift
            if [ "$1" == "" ] ; then
                echo "error: missing argument for command line option '--jdk'"
                usage
                exit 1
            fi
            arg=`echo $1 | egrep '^(latest)|(tested)|([0-9]+)$'`
            if [ "$arg" != "" ] ; then
                josm=$1
            else
                echo "error: unsupported josm version '$1'"
                usage
                exit 1
            fi
            shift
            ;;


        --jdk)
            shift
            if [ "$1" == "" ] ; then
                echo "error: missing argument for command line option '--jdk'"
                usage
                exit 1
            fi
            arg=`echo $1 | egrep '^(jdk17)|(jdk21)$'`
            if [ "$arg" != "" ] ; then
                jdk=$1
            else
                echo "error: unsupported JDK '$1'"
                usage
                exit 1
            fi
            shift
            ;;

        --use-graal-vm)
            use_graal_vm=true
            shift
            ;;

        --graal-js)
            shift
            if [ "$1" == "" ] ; then
                echo "error: missing argument for command line option '--graal-js'"
                usage
                exit 1
            fi
            if [ "$1" == "latest" ] ; then
                graal_js=$1
            else
                graal_js=`cat config.json | jq -r ".\"graaljs-params\" | keys | join(\"\n\")" | egrep "^$1$"`
                if [ "$graal_js" == "" ] ; then
                    echo "error: unsupported GraalJS version '$1'"
                    usage
                    exit 1
                fi
            fi
            shift
            ;;

        *)
            echo "error: unsupported command line argument '$1'"
            usage
            exit 1
    esac
done

josm_jar=""
jdk_home=""
graal_js_home=""

case "$josm" in
    latest | tested)
        josm_jar=josm-$josm.jar
        ;;
    *)
        josm_jar=josm-snapshot-$josm.jar
        ;;
esac

if [ ! -f "$josm_jar" ] ; then
    echo "error: JOSM jar '$josm_jar' for JOSM version '$josm' isn't available locally. Download it first using './manage.sh download-josm <version>'."
    exit 1
fi

if [ $use_graal_vm == true ] ; then
    jdk_home=`cat config.json | jq -r ".\"graalvm-params\".\"$jdk\".directory"`
    if [ ! -d "$jdk_home" ] ; then
        echo "error: GraalVM home '$jdk_home' for JDK '$jdk' doesn't exist. Download it first using './manage.sh download-graalvm <version>'."
        exit 1
    fi
else
    jdk_home=`cat config.json | jq -r ".\"jdk-params\".\"$jdk\".directory"`
    if [ ! -d "$jdk_home" ] ; then
        echo "error: JDK home '$jdk_home' for JDK '$jdk' doesn't exist. Download it first using './manage.sh download-jdk <version>'."
        exit 1
    fi
fi

# GraalVM for JDK21 also requires a GraalJS distribution. We can't install the 'js' language
# into the local GraalVM JDK installation anymore. The tool 'bin/gu' isn't part of the 
# GraalVM JDK anymore.
if [ $use_graal_vm == true -a "$jdk" == "jdk21" ] ; then
    if [ "$graal_js" == "" ] ; then 
        graal_js="latest"
    fi 
fi

if [ "$graal_js" != "" ] ; then 
    if [ "$graal_js" == "latest" ] ; then
        graal_js=`cat config.json | jq -r ".\"graaljs-params\".latest"`
    fi

    graaljs_major=`echo $graal_js | awk -F"." '{print($1)}'`
    graaljs_minor=`echo $graal_js | awk -F"." '{print($2)}'`
fi

graal_js_home=`cat config.json | jq -r ".\"graaljs-params\".\"$graal_js\".directory"`

echo "JOSM:        $josm"
echo "JDK:         $jdk"
echo "Use GraalVM: $use_graal_vm"
echo "GraalJS:     $graal_js,  major: $graaljs_major, minor: $graaljs_minor"
echo "JDK Home:    $jdk_home"
echo "JOSM jar:    $josm_jar"

prepare_logging_properties "DEBUG"

cmd=""

if [ $use_graal_vm == false -a "$graal_js" == "" ] ; then
    cmd="$(pwd)/$jdk_home/bin/java \
        -Xms1g \
        -Xmx2g \
        -Djosm.home=`pwd`/josm-home \
        -Djava.util.logging.config.file=`pwd`/logging.properties \
        --add-opens java.prefs/java.util.prefs=ALL-UNNAMED \
        --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED \
        --add-exports=java.base/sun.security.action=ALL-UNNAMED \
        --add-exports=java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED \
        --add-exports=java.desktop/com.sun.imageio.spi=ALL-UNNAMED \
        -jar `pwd`/$josm_jar"
elif [ $use_graal_vm == true -a "$jdk" == "jdk17" ] ; then 
    cmd="$(pwd)/$jdk_home/bin/java \
        -Xms1g \
        -Xmx2g \
        -Djosm.home=`pwd`/josm-home \
        -Djava.util.logging.config.file=`pwd`/logging.properties \
        --add-opens java.prefs/java.util.prefs=ALL-UNNAMED \
        --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED \
        -jar `pwd`/$josm_jar"
else
    if [ $graljs_major -lt 23 ] || ( [ $graaljs_major -eq 23 ] && [ $graaljs_minor -eq 0 ] )  ;  then 
        modules="org.graalvm.sdk,org.graalvm.js,com.oracle.truffle.regex,org.graalvm.truffle"
    else
        modules="org.graalvm.polyglot,org.graalvm.word,org.graalvm.collections"
    fi 

    cmd="$(pwd)/$jdk_home/bin/java \
        -Xms1g \
        -Xmx2g \
        -Djosm.home=`pwd`/josm-home \
        -Djava.util.logging.config.file=`pwd`/logging.properties \
        -classpath `pwd`/$josm_jar \
        --module-path $graal_js_home/lib \
        --add-modules $modules \
        --add-opens java.prefs/java.util.prefs=ALL-UNNAMED \
        --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED \
        --add-exports=java.base/sun.security.action=ALL-UNNAMED \
        --add-exports=java.desktop/com.sun.imageio.plugins.jpeg=ALL-UNNAMED \
        --add-exports=java.desktop/com.sun.imageio.spi=ALL-UNNAMED \
        org.openstreetmap.josm.gui.MainApplication"
fi

echo "Launching JOSM with:"
echo "----"
echo $cmd
echo "----"
$cmd
