package digital.guimauve.pkg.infrastructure.storage

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class LocalStorageService : StorageService {

    private val localFolder = "pkg_data"

    override suspend fun signUrl(path: String): String =
        File(File(localFolder), path).absolutePath

    override suspend fun uploadStream(file: FileFromStream, path: String): String? =
        withContext(Dispatchers.IO) {
            val localFile = File(File(localFolder), path)
            localFile.parentFile.mkdirs()
            localFile.outputStream().use { output ->
                file.inputStream.copyTo(output)
            }
            localFile.path
        }

    override suspend fun downloadStream(path: String): InputStream? =
        withContext(Dispatchers.IO) {
            val localFile = File(File(localFolder), path)
            if (localFile.exists()) localFile.inputStream() else null
        }

}
