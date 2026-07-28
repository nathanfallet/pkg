package digital.guimauve.pkg.infrastructure.storage

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.services.StorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class LocalStorageService(
    private val localFolder: String = DEFAULT_FOLDER,
) : StorageService {

    companion object {

        const val DEFAULT_FOLDER = "pkg_data"

    }

    override suspend fun signUrl(path: String): String =
        File(File(localFolder), path).absolutePath

    override suspend fun uploadStream(file: FileFromStream, path: String): String? =
        withContext(Dispatchers.IO) {
            requireSafeStorageKey(path)
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
