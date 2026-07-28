package digital.guimauve.pkg.infrastructure.storage

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.services.StorageService
import io.ktor.server.config.*
import java.io.InputStream

class ProxyStorageService(
    config: ApplicationConfig,
) : StorageService {

    private val innerStorageService: StorageService

    init {
        val s3Region = config.propertyOrNull("s3.region")?.getString()?.takeIf(String::isNotBlank)
        val s3Name = config.propertyOrNull("s3.name")?.getString()?.takeIf(String::isNotBlank)
        val s3Key = config.propertyOrNull("s3.key")?.getString()?.takeIf(String::isNotBlank)
        val s3Secret = config.propertyOrNull("s3.secret")?.getString()?.takeIf(String::isNotBlank)
        val useS3 = s3Region != null && s3Name != null && s3Key != null && s3Secret != null

        innerStorageService =
            if (useS3) S3StorageService(s3Region, s3Name, s3Key, s3Secret)
            else LocalStorageService(
                config.propertyOrNull("storage.path")?.getString() ?: LocalStorageService.DEFAULT_FOLDER
            )
    }

    override suspend fun signUrl(path: String): String = innerStorageService.signUrl(path)
    override suspend fun uploadStream(file: FileFromStream, path: String): String? =
        innerStorageService.uploadStream(file, path)

    override suspend fun downloadStream(path: String): InputStream? = innerStorageService.downloadStream(path)

}
