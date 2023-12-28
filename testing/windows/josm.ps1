<#
.DESCRIPTION
Run JOSM with different versions of JDKs, GraalVMs, and GraalJS libraries 

.PARAMETER josm
The JOSM version to lauch. Either 'latest', 'tested', or a JOSM version number. If missing, 'latest' is used.

.PARAMETER jdk
The JDK to use. Either 'jdk17' or 'jdk21'. If missing, 'jdk17' is used.

.PARAMETER useGraalVM
If present, JOSM is started with the GraalVM. The GraalVM version is chosend depending on the parameter '-jdk'

.PARAMETER graalJs
The GraalJS version to be loaded. Either 'latest' or a GraalJS version configured in 'config.ps1'. 
If missing, no GraalJS version is loaded. GraalJS can't be used toghether with the GraalVM, only with a
stock JDK.

.PARAMETER help
Display the help information

#>
param(
    [AllowEmptyString()]
    [string]$josm,

    [AllowEmptyString()]
    [string]$jdk,
    
    [switch]$useGraalVM,
    
    [AllowEmptyString()]
    [string]$graalJs,
    
    [AllowEmptyString()]
    [switch]$help
)

# display messages emitted by Write-Information
$InformationPreference = 'Continue'

# load configuration settings
. .\config.ps1

if ($help) {
    Get-Help .\josm.ps1 -Detailed
    Exit 0
}

if (!$josm) {
    $josm = "latest"
}

if ($jdk) {
    if (! ($jdk -eq "jdk17" -or $jdk -eq "jdk21")) {
        Write-Error -Message "Unsupported JDK version '$jdk'. Use 'jdk17' or 'jdk21'." -Category InvalidArgument
        Exit 1
    }
} else {
    $jdk = "jdk17"
}

if ($graalJs) {
    if (!$GRAALJS_PARAMS.ContainsKey($graalJs)) {
        Write-Error -Message "Unsupported GraalJS version '$graalJs'."  -Category InvalidArgument
        Exit 1
    }
    if ($graalJs -eq "latest") {
        $graalJs = $GRAALJS_PARAMS["latest"]
    }
}

if ($useGraalVM -and $graalJs) {
    Write-Warning "Cannot launch GraalVM toghether with GraalJS. Ignoring parameter -graalJs"
    $graalJs = $null
}

if ($jdk -and $useGraalVM) {
    Write-Information "Launching JOSM '$josm' with GraalVM for JDK '$jdk'"
} elseif ($jdk -and $graalJs) {
    Write-Information "Launching JOSM '$josm' with Open JDK for '$jdk' and GraalJS '$graalJs'"
} else {
    Write-Information "Launching JOSM '$josm' with Open JDK for '$jdk' (without GraalJS)"
}

$josmJar = ""
if ($josm -eq "latest" -or $josm -eq "tested") {
    $josmJar = Join-Path $(Get-Location) -ChildPath "josm-$josm.jar"
} else {
    $josmJar = Join-Path $(Get-Location) -ChildPath "josm-snapshot-$josm.jar"
}
if (!(Test-Path $josmJar)) {
    Write-Error "JOSM jar '$josmJar' doesn't exist. Download it first using '.\manage.ps1 download-josm'. Aborting."
    Exit 1
}

$jdkHome = ""
if ($jdk -and !$useGraalVM) {
    $jdkHome = Join-Path $(Get-Location) -ChildPath $JDK_PARAMS[$jdk]["directory"]    
    if (!(Test-Path $jdkHome)) {
        Write-Error "JDK home '$jdkHome' for JDK '$jdk' doesn't exist. Download it first using '.\manage.ps1 download-jdk'. Aborting."
        Exit 1
    }
} else {
    $jdkHome = Join-Path $(Get-Location) -ChildPath $GRAALVM_PARAMS[$jdk]["directory"]    
    if (!(Test-Path $jdkHome)) {
        Write-Error "Install dir '$jdkHome' for GraalVM '$jdk' doesn't exist. Download it first using '.\manage.ps1 download-graalvm'. Aborting."
        Exit 1
    }
}

$graalJsHome = ""
if ($graalJs) {
    $graalJsHome = Join-Path $(Get-Location) -ChildPath $GRAALJS_PARAMS[$graalJs]["directory"]
    if (!(Test-Path $graalJsHome)) {
        Write-Error "Install dir '$graalJsHome' for GraalJS with version '$graalJs' doesn't exist. Download it first using '.\manage.ps1 download-graaljs'. Aborting."
        Exit 1
    }
}

Write-Information "Launching JOSM '$josm' in jar '$josmJar'"
if ($useGraalVM) {
    Write-Information "Using GraalVM for JDK '$jdk' in directory '$jdkHome'"
} else {
    Write-Information "Using OpenJDK for JDK '$jdk' in directory '$jdkHome'"
    if ($graalJsHome) {
        Write-Information "Loading GraalJS '$graalJS' in directory '$graalJsHome'"
    }
}

$javaBinPath = Join-Path -Path $jdkHome -ChildPath "\bin\java.exe"
if (!(Test-Path $javaBinPath)) {
    Write-Error "java binary '$javaBinPath' doesn't exist. Aborting."
    Exit 1
}

$josmHome = Join-Path -Path $(Get-Location) -ChildPath "josm-home"
if (!(Test-Path $josmHome)) {
    Write-Error "JOSM home '$josmHome' doesn't exist. Create it first using '.\manage.ps1 create-josm-home'. Aborting."
    Exit 1
}

#
# prepare configuration for logging 
#
$logLevel = "INFO"
$loggingProperties = @"
handlers=java.util.logging.ConsoleHandler
.level=WARNING
java.util.logging.ConsoleHandler.level=$logLevel
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
org.openstreetmap.josm.plugins.scripting.level=$logLevel
"@
$loggingPropertiesFile = Join-Path $(Get-Location) -ChildPath "logging.properties"
$loggingProperties | Out-File -FilePath $loggingPropertiesFile


#
# launch JOSM
#
$Env:JAVA_HOME = $jdkHome
if (!$graalJs) {
    Start-Process `
    -FilePath "$javaBinPath" `
    -ArgumentList `
        '-Xms1g', `
        '-Xmx2g', `
        "-Djosm.home=$josmHome", `
        "-Djava.util.logging.config.file=$loggingPropertiesFile", `
        -jar, $josmJar
} else {
    Start-Process `
    -FilePath "$javaBinPath" `
    -ArgumentList `
        '-Xms1g', `
        '-Xmx2g', `
        "-Djosm.home=$josmHome", `
        "-Djava.util.logging.config.file=$loggingPropertiesFile", `
        "-classpath", $josmJar, `
        "--module-path", "$graalJsHome\lib", `
        "--add-modules", "org.graalvm.sdk,org.graalvm.js,com.oracle.truffle.regex,org.graalvm.truffle", `
        "--add-opens", "java.prefs/java.util.prefs=ALL-UNNAMED", `
        "org.openstreetmap.josm.gui.MainApplication" 
}
