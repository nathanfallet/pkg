package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.services.StorageService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * A replaced file keeps its record and its storage path: the object is overwritten, and the record
 * follows the new size and type. Nothing is recorded if the storage did not keep the new content.
 */
class ReplacePackageVersionFileUseCaseTest {

    private val file = PackageVersionFile(
        Uuid.random(), Uuid.random(), "maven-metadata.xml", "application/xml", 10, "org/pkg/version/maven-metadata.xml"
    )
    private val content = "<metadata>new</metadata>".toByteArray()

    private val repository = mockk<PackageVersionFilesRepository> {
        coEvery { update(any(), any(), any()) } answers {
            file.copy(contentType = secondArg(), size = thirdArg())
        }
    }

    /** What the storage received, read while the stream is still open. */
    private var uploaded: ByteArray? = null
    private val storage = mockk<StorageService> {
        coEvery { uploadStream(any(), any()) } answers {
            uploaded = firstArg<FileFromStream>().inputStream.readBytes()
            "stored/${secondArg<String>()}"
        }
    }

    private val useCase = ReplacePackageVersionFileUseCaseImpl(repository, storage)

    @Test
    fun `the content is written over the same path and the record follows it`() = runBlocking {
        val replaced = useCase(file, FileFromStream(content.inputStream(), "text/xml", content.size.toLong()))

        assertContentEquals(content, uploaded)
        coVerify(exactly = 1) { storage.uploadStream(any(), file.path) }
        coVerify(exactly = 1) { repository.update(file.id, "text/xml", content.size.toLong()) }
        assertEquals(file.copy(contentType = "text/xml", size = content.size.toLong()), replaced)
    }

    @Test
    fun `a content the storage did not keep is not recorded`() = runBlocking {
        coEvery { storage.uploadStream(any(), any()) } returns null

        assertNull(useCase(file, FileFromStream(content.inputStream(), "text/xml", content.size.toLong())))

        coVerify(exactly = 0) { repository.update(any(), any(), any()) }
    }

}
