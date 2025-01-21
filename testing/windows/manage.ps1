#
# manage.ps1  - manage the environment for interactively testing JOSM and the 
#   JOSM scripting plugin on Windows 
# 
# For usage information, run 
#   .\manage.ps1 help
# 


# load configuration settings
. .\config.ps1

# display messages emitted by Write-Information
$InformationPreference = 'Continue'
# Don't display a progress bar during Invoke-WebRequest => tremendously faster downloads
$ProgressPreference = 'SilentlyContinue'

function Usage() {
    Write-Host @"
Usage: manage.ps1 action <args>
    Actions:
        help
            display usage information
        prepare
            fully prepares the testing environment, by running download-josm for jdk11 and jdk17,
            running download-graalvm for jdk17 and jdk21, running download graaljs, 
            download-josm for latest and tested, and creating the josm home directory with
            create-josm-home
        download-josm latest|tested|<version>  
            download a JOSM version
        download-jdk jdk17|jdk21
            downloads a portable OpenJDK and installs it in the current directory
        download-graalvm jdk17|jdk21
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
"@
}

class JosmHome {
    static [string] $DEFAULT_JOSM_HOME_DIR = "josm-home"

    static [string]path() {
        return $(Join-Path $(Get-Location) -ChildPath $([JosmHome]::DEFAULT_JOSM_HOME_DIR))
    }

    static [bool]exists() {
        return $(Test-Path $([JosmHome]::path()))
    }

    static [void] create() {
        if ([JosmHome]::exists()) {
            Write-Warning "JOSM home directory '$([JosmHome]::path())' already exists."
            return
        } else {
            New-Item $([JosmHome]::path()) -Type Directory
            Write-Information "Created JOSM home directory '$([JosmHome]::path())"
        }
    }

    static [void] delete() {
        if (![JosmHome]::exists()) {
            Write-Warning "JOSM home '$([JosmHome]::path())' doesn't exist. Skipping delete operation."
        } else {
            Remove-Item $([JosmHome]::path()) -Recurse -Force  
            Write-Information "Deleted JOSM home directory '$([JosmHome]::path())" 
        }
    }

    static [void] enableScriptingPluginInPreferences() {
        $preferencesFile = Join-Path $(Get-Location) -ChildPath "josm-home\preferences.xml"
        if (!(Test-Path $preferencesFile)) {
            # create a default preferences file 
            Write-Information "Creating a default preferences file for JOSM in '$preferencesFile' ..."
            $defaultPreferencesFile = @"
<?xml version="1.0" encoding="UTF-8"?>
<preferences xmlns='http://josm.openstreetmap.de/preferences-1.0' version='18770'>
    <!-- don't prompt for interactively updating JOSM -->
    <tag key='pluginmanager.version-based-update.policy' value='never'/>
    <!-- don't display scripting plugin release notes -->
    <tag key='org.openstreetmap.josm.plugins.scripting.ui.release.ReleaseNotes.last-seen-release-note' value='v0.2.6'/>
    <list key='plugins'>
        <entry value='scripting'/>
    </list>  
</preferences>            
"@
            # Important: -Encoding Default is important. If -Encoding is missing or -Enoding UTF8 is used
            # JOSM will fail to parse the preferences file
            $defaultPreferencesFile | Out-File $preferencesFile -Encoding Default
        }

        $preferences = [xml](Get-Content -Path $preferencesFile)
        $plugins = $preferences.SelectNodes("//*[local-name()='list'][@key = 'plugins']")
        $pluginElement = $null
        if (!$plugins -or $plugins.Count -eq 0) {
            # no element for plugins yet, create one
            $pluginElement = $preferences.CreateElement("list", $preferences.DocumentElement.NamespaceURI)
            $pluginElement.SetAttribute("key", "plugins")
            $preferences.DocumentElement.AppendChild($pluginElement)
        } else {
            $pluginElement = $plugins[0]
        }
        $scriptingEntry = $pluginElement.SelectNodes("./*[local-name() ='entry'][@value = 'scripting']")
        if (!$scriptingEntry -or $scriptingEntry.Count -eq 0) {
            $scriptingEntryElement = $preferences.CreateElement("entry", $preferences.DocumentElement.NamespaceURI)
            $scriptingEntryElement.SetAttribute("value", "scripting")
            $pluginElement.AppendChild($scriptingEntryElement)
            Write-Information "Enabled plugin scripting in preferences file '$preferencesFile'"    
            $xmlWritterSettings = New-Object System.Xml.XmlWriterSettings
            # Important: 'Default' is important. If the encoding is missing or UTF8 is used
            # JOSM will fail to parse the preferences file
            $xmlWritterSettings.encoding = [System.Text.Encoding]::Default
            $xmlWritterSettings.Indent = $true
            $xmlWriter =  [System.XML.XmlWriter]::Create($preferencesFile, $xmlWritterSettings)
            $preferences.Save($xmlWriter)       
        } else {
            Write-Information "Scripting plugin already configured in '$preferencesFile'"
        }

      
    }

    static [void] installLocalScriptingJar() {
        # The directory where the scripting.jar is locally built
        $distDir = $(Join-Path $(Get-Location) -ChildPath "..\..\build\dist")
        $jarFile = $(Join-Path $distDir -ChildPath "scripting.jar")
        if (!(Test-Path $jarFile)) {
            Write-Warning "Scripting jar file '$jarFile' doesn't exist. Build it first using 'gradlew.bat build' in the repo root"
            return
        }
        if (![JosmHome]::exists()) {
            [JosmHome]::create()
        }
        $pluginsDir = (Join-Path $([JosmHome]::path()) -ChildPath "plugins")
        if (!(Test-Path $pluginsDir)) {
            New-Item $pluginsDir -Type Directory
        }
        Copy-Item -Path $jarFile -Destination $pluginsDir
        Write-Information "Copied local scripting jar '$jarFile' file into '$pluginsDir\scripting.jar'"

        [JosmHome]::enableScriptingPluginInPreferences()
    }
}

function downloadJosm([string]$version) {
    $josmFile = ""
    switch($version) {
        "latest" {
            $josmFile = "josm-latest.jar"
        }
        "tested" {
            $josmFile = "josm-tested.jar"
        }
        default {
            $josmFile = "josm-snapshot-$version.jar"            
        }
    }
    $downloadUrl = "$($JOSM_PARAMS["downloadUri"])/$josmFile"
    $targetFile = Join-Path -Path $(Get-Location) -ChildPath $josmFile
    Write-Host "Downloading $josmFile from $downloadUrl ..."
    Invoke-WebRequest -Uri $downloadUrl -OutFile $targetFile
}

function downloadJDK([string]$version) {
    $downloadUrl = $JDK_PARAMS[$version]["uri"]
    Write-Information "Downloading jdk version '$version' from $downloadUrl' ..."
    $jdkDirectory = $JDK_PARAMS[$version]["directory"]
    if (Test-Path $jdkDirectory) {
        Write-Warning "JDK with version '$version' already available in '$jdkDirectory'. Skipping download."
        return
    }
    Invoke-WebRequest -Uri $downloadUrl -OutFile "$version.zip"
    Expand-Archive -Path "$version.zip" -DestinationPath "."
    $candidates = Get-ChildItem "." -Filter "$jdkDirectory*"
    if ($candidates.Length -eq 0) {
        Write-Warning "Local download directory '$jdkDirectory*' doesn't exist. Aborting installation of JDK with version '$verison'."
        return 1
    } elseif ( $candidates.Length -gt 1) {
        Write-Warning "Multiple download directory '$jdkDirectory*' exist. Aborting installation of JDK with version '$verison'."
    }
    Rename-Item -Path $candidates[0] -NewName $jdkDirectory
    Remove-Item -Path "$version.zip"
}

function downloadGraalVM([string]$version) {
    $downloadUrl = $GRAALVM_PARAMS[$version]["uri"]
    $graalVMDirectory = Join-Path -Path $(Get-Location) -ChildPath $GRAALVM_PARAMS[$version]["directory"]
    if (Test-Path $graalVMDirectory) {
        Write-Warning "GraalVM for JDK '$version' already available in '$graalVMDirectory'. Skipping download."
        return
    }

    Write-Information "Downloading GraalVM for JDK '$version' from $downloadUrl' ..."    
    $localFile = "graalvm-for-$version.zip"
    Invoke-WebRequest -Uri $downloadUrl -OutFile $localFile
    Expand-Archive `
        -Path $localFile `
        -DestinationPath $(Get-Location)
    

    if ($version -eq "jdk17") {
        $expandedPath = Get-ChildItem . -Filter "graalvm-community-openjdk-17*" `
            | Select-Object -First 1 -ExpandProperty Name             
        if (!$expandedPath) {
            Write-Warning "Directory starting with 'graalvm-community-openjdk-17*' doesn't exist. Skipping installation of GraalVM"
            return
        }
        Rename-Item -Path $expandedPath -NewName $graalVMDirectory
        Write-Information "Downloaded GraalVM for JDK17 into '$graalVMDirectory'"
        # remove the downloaded zip file
        Remove-Item -Path $localFile

        # initiallize the JavaScript language. Not possible with JDK 21 anymore because 
        # gu.cmd command was removed
        . $graalVMDirectory\bin\gu.cmd install js

    } elseif ($version -eq "jdk21") {
        $expandedPath = Get-ChildItem . -Filter "graalvm-jdk-21*" `
            | Select-Object -First 1 -ExpandProperty Name             
        if (!$expandedPath) {
            Write-Warning "Directory starting with 'graalvm-jdk-21*' doesn't exist. Skipping installation of GraalVM"
            return
        }
        Rename-Item -Path $expandedPath -NewName $graalVMDirectory
        Write-Information "Downloaded GraalVM for JDK21 into '$graalVMDirectory'"
        # remove the downloaded zip file
        Remove-Item -Path $localFile

        Write-Warning "GraalVM for JDK 21 doesn't include the update tool 'gu.cmd' anymore."
        Write-Warning "Downloading and installing the latest GraalJS Java modules instead."
        downloadGraalJS($GRAALJS_PARAMS["latest"])
    }
}

function downloadGraalJS([string] $version) {
    if (!$GRAALJS_PARAMS.ContainsKey($version)) {
        $availableVersions = $GRAALJS_PARAMS.keys -join ", " 
        Write-Error -Message "Unsupported GraalJS version '$version'. Available versions: $availableVersions" -Category InvalidArgument
        Exit 1
    }
    $graalJSDirectory = Join-Path $(Get-Location) -ChildPath "graaljs-$version"
    if (Test-Path $graalJSDirectory) {
        Write-Warning "GraalJS version '$version' already available in '$graalJSDirectory'. Skipping download."
        return
    }
    New-Item $graalJSDirectory -ItemType Directory

    $downloadUrl = $GRAALJS_PARAMS[$version]["uri"]
    Write-Information "Downloading GraalJS version '$version' from $downloadUrl' ..."    
    $localFile = "graaljs-$version.zip"
    Invoke-WebRequest -Uri $downloadUrl -OutFile $localFile
    Expand-Archive -Path $(Join-Path $(Get-Location) -ChildPath $localFile) -DestinationPath $graalJSDirectory
    Remove-Item -Path $(Join-Path $(Get-Location) -ChildPath $localFile)
}

function clean() {
    # remove all JOSM jars 
    Remove-Item *.jar
    Write-Information "Removed all locally installed JOSM jars"

    # remove all locally installed JDKs
    foreach($jdk in $JDK_PARAMS.Keys) {
        $directory = $JDK_PARAMS[$jdk].directory 
        if (Test-Path $directory) {
            Remove-Item -Path $directory -Force -Recurse
            Write-Information "Removed JDK '$jdk' in directory '$directory'"
        }
    }

    # remove all locally installed Graal VMS
    foreach ($vm in $GRAALVM_PARAMS.Keys) {
        $directory = $GRAALVM_PARAMS[$vm].directory 
        if (Test-Path $directory) {
            Remove-Item -Path $directory -Force -Recurse
            Write-Information "Removed GraalVM for JDK '$jdk' in directory '$directory'"
        }
    }

    # remove all locally installed GraalJS distributions
    foreach ($graalJs in $GRAALJS_PARAMS.Keys) {
        if ($graalJs -eq "latest") {
            continue
        }
        $directory = $GRAALJS_PARAMS[$graalJs].directory 
        if (Test-Path $directory) {
            Remove-Item -Path $directory -Force -Recurse
            Write-Information "Removed GraalJS '$graalJs' in directory '$directory'"
        }
    }

    # remove JOSM home
    [JosmHome]::delete()
}

$action=$args[0]

# Default: display usage information
if (!$action) {
    Usage
    Exit 0
}

switch($action) {
    "help" {
        Usage
        Exit 0
    }

    "prepare" {
        downloadJDK("jdk17")
        downloadJDK("jdk21")
        downloadGraalVM("jdk17")
        downloadGraalVM("jdk21")
        downloadGraalJS($GRAALJS_PARAMS["latest"])
        downloadJosm("latest")
        downloadJosm("tested")
        [JosmHome]::create()
    }

    "download-josm" {
        $version = $args[1]
        if (!$version) {
            Write-Error -Message "Missing command line argument for version" -Category InvalidArgument
            Usage
        } else {
            downloadJosm($version)
        }
    }

    "download-jdk" {
        $version = $args[1]
        if (!$version) {
            Write-Error -Message "Missing command line argument for version" -Category InvalidArgument
            Usage
            Exit 1
        }
        if (! ($version -eq "jdk17" -or $version -eq "jdk21")) {
            Write-Error -Message "Unsupported JDK version '$version'" -Category InvalidArgument
            Usage
            Exit 1
        }
        downloadJDK($version)
    }

    "download-graalvm" {
        $version = $args[1]
        if (!$version) {
            Write-Information "Using default version 'jdk17'"
            $version = "jdk17"
        }
        if (!($version -eq "jdk17"-or $version -eq "jdk21")) {
            Write-Error -Message "Unsupported JDK version '$version'" -Category InvalidArgument
            Usage
            Exit 1
        }
        downloadGraalVM($version)
    }

    "download-graaljs" {
        $version = $args[1]
    
        if (!$version) {
            Write-Information "Downloading latest GraalJS release ..."
            $version = "latest"
        }
        if ($version -eq "latest") {
            $version = $GRAALJS_PARAMS["latest"]
            Write-Information "Resolving latest GraalJS release to release '$version' ..."
        }
        Write-Information "Downloading GraalJS release '$version' ..."
        downloadGraalJS($version)
    }

    "clean-jars" {
        Write-Host "Deleting downloaded josm jar files ..."
        if (Test-Path -Path "josm-latest.jar") {
            Remove-Item "josm-latest.jar"
        }
        if (Test-Path -Path "josm-tested.jar") {
            Remove-Item "josm-tested.jar"
        }
        Remove-Item "josm-snapshot-*.jar"
    }

    "clean" {
        clean
    }

    "create-josm-home" {
        [JosmHome]::create()
    }

    "delete-josm-home" {
        [JosmHome]::delete()
    }

    "update-scripting-jar" {
        [JosmHome]::installLocalScriptingJar()
    }

    default {
        Write-Error -Message "Unsupported action '$action'" -Category InvalidArgument
        Usage
        Exit 1
    }
}
