package org.openstreetmap.josm.plugins.scripting.build

import groovy.ant.AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

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
    JDK17("jdk17"),
    JDK20("jdk20")
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

    static final DEFAULT_DOWNLOAD_BASE_URL = "https://download.oracle.com/graalvm"
    static final DEFAULT_GRAALVM_PLATFORM = GraalVMPlatform.LINUX_AMD64
    static final DEFAULT_GRAALVM_JDK = GraalVMJDK.JDK17

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
        def jdk
        if (configuredGraalVMJDK == GraalVMJDK.JDK17) {
            jdk = "jdk-17"
        } else if (configuredGraalVMJDK == GraalVMJDK.JDK20) {
            jdk  = "jdk-20"
        }
        def platform
        if (configuredGraalVMPlatform == GraalVMPlatform.LINUX_AMD64) {
            platform = "linux-x64"
        } else if (configuredGraalVMPlatform == GraalVMPlatform.WINDOWS_AMD64) {
            platform = "windows-x64"
        }
        final suffix = configuredGraalVMPlatform.isWindows() ? "zip" : "tar.gz"
        final fileName = "graalvm-${jdk}_${platform}_bin.${suffix}"
        return fileName
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
        def os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
        if (os.contains("windows")) {
            return GraalVMPlatform.WINDOWS_AMD64
        } else {
            return DEFAULT_GRAALVM_PLATFORM
        }
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
                        "Illegal value for property 'graalVMJDK' in task '$this.name. Got value '$value'")
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
        def jdkSlug
        if (configuredGraalVMJDK == GraalVMJDK.JDK17) {
            jdkSlug = "17"
        } else if (configuredGraalVMJDK == GraalVMJDK.JDK20) {
            jdkSlug = "20"
        }
        def url = "$configuredGraalDownloadUrl/${jdkSlug}/latest/${buildDistributionFileName()}"
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

    File buildInstallationBaseDir() {
        //final jdk = configuredGraalVMJDK.name
        return new File(project.projectDir, "software")
        //return new File(installBaseDir, "graalvm-$jdk").absoluteFile
    }

    def createInstallationBaseDir() {
        final installDir = buildInstallationBaseDir()
        if (!installDir.exists()) {
            installDir.mkdirs()
        }
        return installDir
    }

    def rootZipEntry(final File zipFile) {
        new ZipFile(zipFile).withCloseable {zip ->
            return zip.stream().findFirst().get().getName().split("/")[0]
        }
    }

    def installGraalVM(final File tempFile) {
        final jdk = configuredGraalVMJDK.name
        def installDir
        if (configuredGraalVMPlatform.isWindows()) {
            installDir = buildInstallationBaseDir()
            // windows OS: extract .zip file
            final root = rootZipEntry(tempFile.absoluteFile)
            def ant = new AntBuilder()
            ant.unzip(src: tempFile.absolutePath, dest: installDir.absolutePath, overwrite: false)
            installDir = new File(installDir, root)
            final targetDir = new File(buildInstallationBaseDir(), "graalvm-$jdk")
            installDir.renameTo(targetDir)
            installDir = targetDir
        } else {
            installDir = new File(buildInstallationBaseDir(), "graalvm-$jdk").absoluteFile
            // linux OS: extract .tgz file
            if (!installDir.exists()) {
                installDir.mkdirs()
            }
            def command = "tar xvf ${tempFile.absolutePath} --directory ${installDir.absolutePath} --strip-components=1"
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

        // add JS language to the GraalVM
        logger.info("Installation JS language in GraalVM for JDK '$jdk' in directory '$installDir'")
        final binDir = new File(installDir, "bin")
        final guCommand = configuredGraalVMPlatform.isWindows()
            ? new File(binDir, "gu.cmd")
            : new File(binDir, "gu")
        final command = "${guCommand} install js"
        final process = command.execute()
        process.inputStream.withReader { reader ->
            logger.info(reader.readLines().join(System.lineSeparator()))
        }
        process.waitFor()
        if (process.exitValue()) {
            logger.error("Failed to add js language to GraalVM: ${process.getErrorStream()}")
            throw new GradleException("Failed to add js language to GraalVM")
        }
    }

    @TaskAction
    def downloadAndInstall() {
        final platform = this.configuredGraalVMPlatform
        final jdk = this.configuredGraalVMJDK
        logger.info("Downloading GraalVM for JDK '${jdk}' and platform '$platform' ...")
        def installDir = new File(buildInstallationBaseDir(), "graalvm-${configuredGraalVMJDK.name}").absoluteFile
        if (installDir.exists() && installDir.isDirectory()) {
            logger.info("GraalVM for JDK '$jdk' already installed in '${installDir.absolutePath}. Skipping download.'")
            return
        }

        def url = buildDownloadUrl()
        def distribFile = downloadGraalVM(url)
        def installBaseDir = createInstallationBaseDir()
        installGraalVM(distribFile)
        installDir = new File(installBaseDir, distribFile.name)
        logger.info("Successfully downloaded GraalVM for JDK '$jdk'")
        logger.info("Successfully installed GraalVM for JDK '$jdk' in '${installDir.absolutePath}' ...")
        distribFile.delete()
    }
}
