package digital.guimauve.pkg.services.storage

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.InputStream
import java.time.Duration

class S3StorageService(
    private val region: String,
    private val name: String,
    private val accessKey: String,
    private val secretKey: String,
) : IStorageService {

    private fun getRegion(): Region =
        Region.of(region)

    private fun getCredentials(): AwsCredentialsProvider =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))

    override fun signUrl(path: String): String = S3Presigner.builder()
        .region(getRegion())
        .credentialsProvider(getCredentials())
        .build()
        .use { presigner ->
            val objectRequest = GetObjectRequest.builder()
                .bucket(name)
                .key(path)
                .build()
            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(1))
                .getObjectRequest(objectRequest)
                .build()
            return presigner.presignGetObject(presignRequest).url().toExternalForm()
        }

    override fun uploadStream(file: FileFromStream, path: String): String? = S3Client.builder()
        .region(getRegion())
        .credentialsProvider(getCredentials())
        .overrideConfiguration { config ->
            config.apiCallTimeout(Duration.ofMinutes(30))
            config.apiCallAttemptTimeout(Duration.ofMinutes(30))
        }
        .build()
        .use { s3 ->
            val request = PutObjectRequest.builder()
                .bucket(name)
                .key(path)
                .contentType(file.contentType.toString())
                .contentLength(file.contentLength)
                .build()
            s3.putObject(request, RequestBody.fromInputStream(file.inputStream, file.contentLength))
            return "https://$name.s3.$region.amazonaws.com/$path"
        }

    override fun downloadStream(path: String): InputStream? = S3Client.builder()
        .region(getRegion())
        .credentialsProvider(getCredentials())
        .build()
        .use { s3 ->
            val request = GetObjectRequest.builder()
                .bucket(name)
                .key(path)
                .build()
            val response = s3.getObject(request)
            // The returned InputStream will be closed when the S3Client is closed, so we need to copy it
            return response.use { it.readBytes().inputStream() }
        }

}
