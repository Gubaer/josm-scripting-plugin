package org.openstreetmap.josm.plugins.scripting.release


import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectRequest
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile
import java.util.stream.Collectors

/**
 * Publishes the current scripting*.jar plugin from <code>build/libs</code>
 * into an Amazon S3 bucket.
 */
@SuppressWarnings('unused')
abstract class PublishToAwsS3Task extends DefaultTask {

    static public final String DEFAULT_BUCKET_NAME = "josm-scripting-plugin"
    static public final String DEFAULT_S3_OBJECT_KEY = "scripting.jar"

    /**
     * the name of the S3 bucket into which the scripting.jar is uploaded
     */
    @Input
    @Optional
    abstract Property<String> getBucketName()

    /**
     * the object key name in the S3 bucket, i.e. the remote file name
     */
    @Input
    @Optional
    abstract Property<String> getS3ObjectKey()

    PublishToAwsS3Task() {
        bucketName.convention(DEFAULT_BUCKET_NAME)
        getS3ObjectKey().convention(DEFAULT_S3_OBJECT_KEY)
    }

    def ensureCredentialsSet(final AmazonS3ClientBuilder builder) {
        final credentials = builder.credentials.credentials
        if (credentials.AWSAccessKeyId == null || credentials.AWSSecretKey == null) {
            String message = "Missing credentials to access AWS S3. " +
                "Set the environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY " +
                "or use another supported configuration options. " +
                "See https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html"
            logger.error(message)
            throw new GradleException(message)
        }
    }

    def lookupDistributionJarFile() {
        final jarFile = new File(project.buildDir, "dist/scripting.jar")
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            logger.error("No scripting.jar file found. Path: '$jarFile.absolutePath'")
        }
        return new JarFile(jarFile)
    }

    def publishToS3(final AmazonS3 client, final jarFile) {
        final request = new PutObjectRequest(bucketName.get(), s3ObjectKey.get(), jarFile)
        try {
            client.putObject(request)
            logger.info(
                "Successfully uploaded file '${jarFile.absolutePath}' " +
                "to S3 bucket '${bucketName.get()}' " +
                "into key '${s3ObjectKey.get()}'")
        } catch(Throwable e) {
            final message = "Failed to upload '${jarFile.absolutePath}' " +
                "to S3 bucket '${bucketName.get()}' "
            logger.log(LogLevel.ERROR, message, e)
            throw new GradleException(message, e)
        }
    }

    @TaskAction
    def publish() {
        final builder = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.EU_CENTRAL_1)
        ensureCredentialsSet(builder)
        final jarFile = lookupDistributionJarFile()
        publishToS3(builder.build(), jarFile)
    }
}
