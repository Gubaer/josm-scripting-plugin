
#
# Parameters for two JDKs used in testing
#
$JDK_PARAMS = @{
    jdk17 = @{
        uri = "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip"
        directory = "jdk17"
    }
    jdk20 = @{
        uri = "https://download.oracle.com/java/20/latest/jdk-20_windows-x64_bin.zip"
        directory = "jdk20"
    }
}

#
# Parameters for two GraalVM versions used in testing
#
$GRAALVM_PARAMS = @{

    jdk17 = @{
        uri = "https://download.oracle.com/graalvm/17/latest/graalvm-jdk-17_windows-x64_bin.zip"
        directory = "graalvm-jdk17"
    }

    jdk20 = @{
        uri = "https://download.oracle.com/graalvm/20/latest/graalvm-jdk-20_windows-x64_bin.zip"
        directory = "graalvm-jdk20"
    }
}

# 
# Parameters for a GraalJS distribution available for download in the Git repo
# https://github.com/Gubaer/josm-scripting-plugin-graaljs
#
$GRAALJS_PARAMS = @{
    latest = "23.0.0"
    "23.0.0" = @{
        uri = "https://github.com/Gubaer/josm-scripting-plugin-graaljs/releases/download/23.0.0/graaljs-23.0.0.zip"
        directory = "graaljs-23.0.0"
    }

    "22.1.3" = @{
        uri = "https://github.com/Gubaer/josm-scripting-plugin-graaljs/releases/download/22.3.0/graaljs-22.3.0.zip"
        directory = "graaljs-22.1.3"
    }

    "22.1.0" = @{
        uri = "https://github.com/Gubaer/josm-scripting-plugin-graaljs/releases/download/22.1.0/graaljs-22.1.0.zip"
        directory = "graaljs-22.1.0"
    }
}

#
# Parametes for downloading JOSM versions (latest, tested, or a specific version)
#
$JOSM_PARAMS = @{
    downloadUri = "https://josm.openstreetmap.de/download"
    archiveDownloadUri = "https://josm.openstreetmap.de/download/Archive"
}
