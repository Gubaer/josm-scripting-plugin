package org.openstreetmap.josm.plugins.scripting.build

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

enum GraalVMPlattform {

    LINUX_AMD64("linux-amd64"),
    LINUX_AARCH64("linux-aarch64"),
    MACOS_AMD64("darwin-amd64"),
    MACOS_AARCH64("darwin-aarch64"),
    WINDOWS_AMD64("windows-amd64")

    final String name
    GraalVMPlattform(final name) {
        this.name = name
    }

    static def fromString(final value) {
        def name = value.trim()
        return values().find {
            it.name == name
        }
    }

    def isWindows() {
        return this == WINDOWS_AMD64
    }
}

@SuppressWarnings('unused')
abstract class GraalVMDownloadTask extends DefaultTask {

    static final DEFAULT_DOWNLOAD_BASE_URL = "https://github.com/graalvm/graalvm-ce-builds/releases/download"
    static final DEFAULT_GRAALVM_VERSION = "22.1.0"
    static final DEFAULT_GRAALVM_PLATTFORM = GraalVMPlattform.LINUX_AMD64


    @Input
    @Optional
    abstract Property<String> getDownloadBaseUrl()

    @Input
    @Optional
    abstract Property<String> getGraalVMVersion()

    @Input
    @Optional
    abstract Property<String> getGraalVMPlattform()

    def buildDistributionFileName() {
        def plattform = GraalVMPlattform.fromString(configuredGraalVMPlattform)
        if (plattform == null) {
            throw new InvalidUserDataException("unsupported GraalVM plattform '${configuredGraalVMPlattform}'")
        }
        def fileName = "graalvm-ce-java11-$configuredGraalVMPlattform-${configuredGraalVMVersion}"

        if (plattform.isWindows()) {
            fileName += ".zip"
        } else {
            fileName += ".tar.gz"
        }
        return fileName
    }

    @Internal
    def getConfiguredGraalVMVersion() {
        if (graalVMVersion.isPresent()) {
            return graalVMVersion.get()
        }
        def version = project.property("graalvm.version")
        if (version != null) {
            return version
        }
        return DEFAULT_GRAALVM_VERSION
    }

    @Internal
    def getConfiguredGraalVMPlattform() {
        if (graalVMPlattform.isPresent()) {
            return graalVMPlattform.get()
        }
        def plattform = project.property("graalvm.plattform")
        if (plattform != null) {
            return plattform
        }
        return DEFAULT_GRAALVM_PLATTFORM
    }

    @Internal
    def getConfiguredGraalDownloadUrl() {
        if (downloadBaseUrl.isPresent()) {
            return downloadBaseUrl.get()
        }
        def url = project.property("graalvm.download-base-url")
        if (url != null) {
            return url
        }
        return DEFAULT_DOWNLOAD_BASE_URL
    }

    def buildDownloadUrl() {
        def plattform = GraalVMPlattform.fromString(configuredGraalVMPlattform)
        if (plattform == null) {
            throw new InvalidUserDataException("unsupported GraalVM plattform '$configuredGraalVMPlattform'")
        }
        def url = "$configuredGraalDownloadUrl/vm-${configuredGraalVMVersion}/${buildDistributionFileName()}"
        return new URL(url)
    }

    def downloadGraalVM(final URL url) {
        def fileName = url.getPath().split("/").last()
        def tempDir = File.createTempDir("graalvm-download")
        def tempFile = new File(tempDir, fileName)
        logger.info("Downloading GraalVM from '$url' ...")
        logger.debug("Writing GraalVM distribution to temporary file '$tempFile' ...")
        url.openConnection().with {conn ->
            tempFile.withOutputStream {out ->
                conn.inputStream.with {inp ->
                    out << inp
                }
            }
        }
        return tempFile
    }

    def buildInstallationDirName() {
        def installBaseDir = new File(project.projectDir, "software")
        return new File(installBaseDir, "graalvm-ce-java11-$configuredGraalVMVersion")
            .absolutePath
    }

    def createInstallationDir() {
        def installDir = new File(project.projectDir, "software")
        if (!installDir.exists()) {
            installDir.mkdirs()
        }
        return installDir
    }

    def installGraalVM(final File tempFile) {
        def installDir = new File(project.projectDir, "software")
        def command = "tar xvf ${tempFile.absolutePath} --directory ${installDir.absolutePath}"
        def process = command.execute()
        process.out.close()
        process.inputStream.withReader {reader ->
            reader.readLines()
        }
        process.waitFor()
        if (process.exitValue()) {
            logger.error("Failed to install GraalVM: ${process.getErrorStream()}")
            throw new StopActionException("Failed to install GraalVM")
        }
    }

    @TaskAction
    def downloadAndInstall() {
        def installDir = new File(buildInstallationDirName())
        logger.info("installdir: ${installDir.absolutePath}")
        if (installDir.exists() && installDir.isDirectory()) {
            logger.info("GraalVM '$configuredGraalVMVersion' already installed in '$installDir.absolutePath. Skipping download.'")
            return
        }

        def plattform = GraalVMPlattform.fromString(configuredGraalVMPlattform)
        if (plattform == null) {
            throw new InvalidUserDataException("unsupported GraalVM plattform '$configuredGraalVMPlattform'")
        }
        if (plattform.isWindows()) {
            throw new StopActionException("GraalVM download is not supported yet on the Windows plattform")
        }

        def url = buildDownloadUrl()
        def distribFile = downloadGraalVM(url)
        def installBaseDir = createInstallationDir()
        installGraalVM(distribFile)
        installDir = new File(installBaseDir, distribFile.name)
        logger.info("Successfully downloaded GraalVM '$configuredGraalVMVersion'")
        logger.info("Successfully installed GraalVM '$configuredGraalVMVersion' in '${installDir.absolutePath}' ...")
        distribFile.delete()
    }
}
