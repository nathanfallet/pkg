package digital.guimauve.pkg.infrastructure.storage

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
) : StorageService {

    private fun getRegion(): Region =
        Region.of(region)

    private fun getCredentials(): AwsCredentialsProvider =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))

    override suspend fun signUrl(path: String): String = withContext(Dispatchers.IO) {
        S3Presigner.builder()
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
                presigner.presignGetObject(presignRequest).url().toExternalForm()
            }
    }

    override suspend fun uploadStream(file: FileFromStream, path: String): String? =
        withContext(Dispatchers.IO) {
            requireSafeStorageKey(path)
            S3Client.builder()
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
                        .contentType(file.contentType)
                        .contentLength(file.contentLength)
                        .build()
                    s3.putObject(request, RequestBody.fromInputStream(file.inputStream, file.contentLength))
                    "https://$name.s3.$region.amazonaws.com/$path"
                }
        }

    override suspend fun downloadStream(path: String): InputStream? = withContext(Dispatchers.IO) {
        S3Client.builder()
            .region(getRegion())
            .credentialsProvider(getCredentials())
            .build()
            .use { s3 ->
                val request = GetObjectRequest.builder()
                    .bucket(name)
                    .key(path)
                    .build()
                // The stream is closed along with the client, so its content has to be copied out.
                s3.getObject(request).use { it.readBytes().inputStream() }
            }
    }

}
