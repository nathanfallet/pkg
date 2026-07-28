package digital.guimauve.pkg.domain.models.storage

import java.io.InputStream
import java.net.URL

/**
 * Where the content of a file being published comes from.
 */
sealed interface FileSource {

    val contentType: String

}

/**
 * A file being streamed straight from the request.
 */
class FileFromStream(
    val inputStream: InputStream,
    override val contentType: String,
    val contentLength: Long,
) : FileSource

/**
 * A file already held in memory.
 */
data class FileFromBytes(
    val bytes: ByteArray,
    override val contentType: String,
) : FileSource

/**
 * A file to be fetched from a URL.
 */
data class FileFromUrl(
    val url: URL,
    override val contentType: String,
) : FileSource
