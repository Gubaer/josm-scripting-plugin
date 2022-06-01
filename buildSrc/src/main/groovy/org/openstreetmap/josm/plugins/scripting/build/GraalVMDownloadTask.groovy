package org.openstreetmap.josm.plugins.scripting.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * The platforms for which distributions of the GraalVM are available
 * for download.
 */
enum GraalVMPlatform {

    LINUX_AMD64("linux-amd64"),
    LINUX_AARCH64("linux-aarch64"),
    MACOS_AMD64("darwin-amd64"),
    MACOS_AARCH64("darwin-aarch64"),
    WINDOWS_AMD64("windows-amd64")

    final String name
    GraalVMPlatform(final name) {
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

/**
 * The JDKs for which distributions of the GraalVM are available
 * for download.
 */
enum GraalVMJDK {
    JDK11("java11"),
    JDK17("java17")
    final String name

    GraalVMJDK(String name) {
        this.name = name
    }

    static def fromString(final value) {
        def name = value.trim()
        return values().find {
            it.name == name
        }
    }
}

/**
 * Downloads and locally (within the project directory) installs
 * GraalVM.
 */
@SuppressWarnings('unused')
abstract class GraalVMDownloadTask extends DefaultTask {

    static final DEFAULT_DOWNLOAD_BASE_URL = "https://github.com/graalvm/graalvm-ce-builds/releases/download"
    static final DEFAULT_GRAALVM_VERSION = "22.1.0"
    static final DEFAULT_GRAALVM_PLATFORM = GraalVMPlatform.LINUX_AMD64
    static final DEFAULT_GRAALVM_JDK = GraalVMJDK.JDK11

    static final PROP_VERSION = "graalvm.version"
    static final PROP_PLATFORM = "graalvm.platform"
    static final PROP_JDK = "graalvm.jdk"
    static final PROP_DOWNLOAD_BASE_URL = "graalvm.download-base-url"

    /**
     * The base URL for downloading GraalVM releases. Use it to override
     * {@link #DEFAULT_DOWNLOAD_BASE_URL}, if necessary.
     */
    @Input
    @Optional
    abstract Property<String> getDownloadBaseUrl()

    /**
     * The GraalVM version
     */
    @Input
    @Optional
    abstract Property<String> getGraalVMVersion()

    /**
     * The GraalVM platform. Set it to either
     * <ul>
     *    <li>an enumeration value of {@link GraalVMPlatform}</li>
     *    <li>a string value, i.e. the name of a GraalVM platform</li>
     * </ul>
     * @return
     */
    @Input
    @Optional
    abstract Property<Object> getGraalVMPlatform()


    @Input
    @Optional
    abstract Property<Object> getGraalVMJDK()

    private def buildDistributionFileName() {
        final platform = this.configuredGraalVMPlatform
        final jdk = this.configuredGraalVMJDK
        final version = this.configuredGraalVMVersion
        final suffix = platform.isWindows() ? "zip" : "tar.gz"
        def fileName = "graalvm-ce-$jdk.name-$platform.name-$version.$suffix"
        return fileName
    }

    @Internal
    String getConfiguredGraalVMVersion() {
        if (graalVMVersion.isPresent()) {
            return graalVMVersion.get()
        }
        def version = project.property(PROP_VERSION)
        if (version != null) {
            return version
        }
        return DEFAULT_GRAALVM_VERSION
    }

    @Internal
    GraalVMPlatform getConfiguredGraalVMPlatform() {
        if (graalVMPlatform.isPresent()) {
            final value = graalVMPlatform.get()
            if (value == null) {
                throw new GradleException("Property 'graalVMPlatform' in task '${this.getName()}' must not be null")
            } else if (value instanceof  GraalVMPlatform) {
                return value
            } else {
                final platform = GraalVMPlatform.fromString(value.toString())
                if (platform != null) {
                    return platform
                } else {
                    throw new GradleException("Illegal value for property 'graalVMPlatform' in task '${this.getName()}'. Got '$value'")
                }
            }
        }
        if (project.hasProperty(PROP_PLATFORM)) {
            def platform = project.property(PROP_PLATFORM)
            platform = GraalVMPlatform.fromString(platform.toString())
            if (platform == null) {
                throw new GradleException(
                    "Illegal value for project property '$PROP_PLATFORM'. Got '$platform'")
            }
            return platform
        }
        return DEFAULT_GRAALVM_PLATFORM
    }

    @Internal
    GraalVMJDK getConfiguredGraalVMJDK() {
        if (graalVMJDK.isPresent()) {
            final value = graalVMJDK.get()
            if (value == null) {
                throw new GradleException("Property 'graalVMJDK' in task '${this.name}' must not be null")
            } else if (value instanceof GraalVMJDK) {
                return value
            } else {
                final jdk = GraalVMJDK.fromString(value.toString())
                if (jdk == null) {
                    throw new GradleException(
                        "Illegal value for property 'graalVMJDK' in task '$this.name. Got value '$value.toString()'")
                }
                return jdk
            }
        }
        if (project.hasProperty(PROP_JDK)) {
            final value = project.property(PROP_JDK)
            final jdk = GraalVMJDK.fromString(value.trim())
            if (jdk == null) {
                throw new GradleException("Illegal value for project property '$PROP_JDK'. Got value '$value'")
            }
            return jdk
        }
        return DEFAULT_GRAALVM_JDK
    }

    @Internal
    def getConfiguredGraalDownloadUrl() {
        if (downloadBaseUrl.isPresent()) {
            return downloadBaseUrl.get()
        }
        def url = project.property(PROP_DOWNLOAD_BASE_URL)
        if (url != null) {
            return url
        }
        return DEFAULT_DOWNLOAD_BASE_URL
    }

    def buildDownloadUrl() {
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
        def jdk = configuredGraalVMJDK.name
        def version = configuredGraalVMVersion
        def installBaseDir = new File(project.projectDir, "software")
        return new File(installBaseDir, "graalvm-ce-$jdk-$version").absolutePath
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
            throw new GradleException("Failed to install GraalVM")
        }
    }

    @TaskAction
    def downloadAndInstall() {
        final platform = this.configuredGraalVMPlatform
        final version = this.configuredGraalVMVersion
        def installDir = new File(buildInstallationDirName())
        if (installDir.exists() && installDir.isDirectory()) {
            logger.info("GraalVM '$configuredGraalVMVersion' already installed in '$installDir.absolutePath. Skipping download.'")
            return
        }
        if (platform.isWindows()) {
            throw new GradleException("GraalVM download is not supported yet on the Windows platform")
        }

        def url = buildDownloadUrl()
        def distribFile = downloadGraalVM(url)
        def installBaseDir = createInstallationDir()
        installGraalVM(distribFile)
        installDir = new File(installBaseDir, distribFile.name)
        logger.info("Successfully downloaded GraalVM '$version'")
        logger.info("Successfully installed GraalVM '$version' in '$installDir.absolutePath' ...")
        distribFile.delete()
    }
}
