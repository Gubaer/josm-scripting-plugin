
#
# Parameters for two JDKs used in testing
#
$JDK_PARAMS = @{
    jdk11 = @{
        uri = "https://aka.ms/download-jdk/microsoft-jdk-11.0.19-windows-x64.zip"
        directory = "jdk-11.0.19+7"
    }
    jdk17 = @{
        uri = "https://aka.ms/download-jdk/microsoft-jdk-17.0.7-windows-x64.zip"
        directory = "jdk-17.0.7+7"
    }
}

#
# Parameters for two GraalVM versions used in testing
#
$GRAALVM_PARAMS = @{
    jdk11 = @{
        uri = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.2/graalvm-ce-java11-windows-amd64-22.3.2.zip"
        directory = "graalvm-ce-java11-22.3.2"
    }

    jdk17 = @{
        uri = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.2/graalvm-ce-java17-windows-amd64-22.3.2.zip"
        directory = "graalvm-ce-java17-22.3.2"
    }
}

# 
# Parameters for a GraalJS distribution available for download in the Git repo
# https://github.com/Gubaer/josm-scripting-plugin-graaljs
#
$GRAALJS_PARAMS = @{
    latest = "22.1.3"
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
