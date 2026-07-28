package digital.guimauve.pkg.services.storage

import io.ktor.server.config.*
import java.io.InputStream

class ProxyStorageService(
    config: ApplicationConfig,
) : IStorageService {

    private val innerStorageService: IStorageService

    init {
        val s3Region = config.propertyOrNull("s3.region")?.getString()
        val s3Name = config.propertyOrNull("s3.name")?.getString()
        val s3Key = config.propertyOrNull("s3.key")?.getString()
        val s3Secret = config.propertyOrNull("s3.secret")?.getString()
        val useS3 = s3Region != null && s3Name != null && s3Key != null && s3Secret != null

        innerStorageService =
            if (useS3) S3StorageService(s3Region, s3Name, s3Key, s3Secret)
            else LocalStorageService()
    }

    override fun signUrl(path: String): String = innerStorageService.signUrl(path)
    override fun uploadStream(file: FileFromStream, path: String): String? =
        innerStorageService.uploadStream(file, path)

    override fun downloadStream(path: String): InputStream? = innerStorageService.downloadStream(path)

}
