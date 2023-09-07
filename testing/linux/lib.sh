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