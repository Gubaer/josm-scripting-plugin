
#
# Parameters for two JDKs used in testing
#
$JDK_PARAMS = @{
    jdk17 = @{
        uri = "https://aka.ms/download-jdk/microsoft-jdk-17.0.13-windows-x64.zip"
        directory = "jdk-17"
    }
    jdk21 = @{
        uri = "https://aka.ms/download-jdk/microsoft-jdk-21.0.5-windows-x64.zip"
        directory = "jdk-21"
    }
 }

#
# Parameters for two GraalVM versions used in testing
#
$GRAALVM_PARAMS = @{
    jdk17 = @{
        uri = "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-17.0.9/graalvm-community-jdk-17.0.9_windows-x64_bin.zip"
        directory = "graalvm-jdk-17"
    }
    jdk21 = @{
        uri = "https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_windows-x64_bin.zip"
        directory = "graalvm-jdk-21"
    }
}

# 
# Parameters for a GraalJS distribution available for download in the Git repo
# https://github.com/Gubaer/josm-scripting-plugin-graaljs
#
$GRAALJS_PARAMS = @{
    latest = "24.1.1"

    "24.1.1" = @{
        uri = "https://github.com/Gubaer/josm-scripting-plugin-graaljs/releases/download/24.1.1/graaljs-24.1.1.zip"
        directory = "graaljs-24.1.1"
    }

    "23.1.1" = @{
        uri = "https://github.com/Gubaer/josm-scripting-plugin-graaljs/releases/download/23.1.1/graaljs-23.1.1.zip"
        directory = "graaljs-23.1.1"
    }

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
