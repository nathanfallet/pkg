package digital.guimauve.pkg.services.storage

import java.io.File
import java.io.InputStream

class LocalStorageService : IStorageService {

    private val localFolder = "pkg_data"

    override fun signUrl(path: String): String {
        return File(File(localFolder), path).absolutePath
    }

    override fun uploadStream(file: FileFromStream, path: String): String? {
        val localFile = File(File(localFolder), path)
        localFile.parentFile.mkdirs()
        localFile.outputStream().use { output ->
            file.inputStream.copyTo(output)
        }
        return localFile.path
    }

    override fun downloadStream(path: String): InputStream? {
        val localFile = File(File(localFolder), path)
        return if (localFile.exists()) localFile.inputStream()
        else null
    }

}
