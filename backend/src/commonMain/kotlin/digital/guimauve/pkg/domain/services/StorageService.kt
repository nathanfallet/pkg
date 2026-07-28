package digital.guimauve.pkg.domain.services

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import java.io.InputStream

/**
 * Interface for storing and reading the published files.
 *
 * Every method suspends: the implementations talk to a disk or to S3, and confine that blocking
 * work to [kotlinx.coroutines.Dispatchers.IO] themselves.
 */
interface StorageService {

    /**
     * Builds a URL a client can download the file from directly.
     */
    suspend fun signUrl(path: String): String

    /**
     * Stores a file.
     *
     * @return The location it was stored at, or null if it could not be stored.
     */
    suspend fun uploadStream(file: FileFromStream, path: String): String?

    /**
     * Reads a stored file.
     *
     * @return Its content, or null if the storage has no such file.
     */
    suspend fun downloadStream(path: String): InputStream?

}
