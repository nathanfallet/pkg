package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.packages.versions.files.PackageVersionFileAlreadyExistsException
import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.models.storage.FileFromBytes
import digital.guimauve.pkg.domain.models.storage.FileFromStream
import digital.guimauve.pkg.domain.models.storage.FileFromUrl
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.domain.services.StorageService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * Whatever a file comes from, it reaches the storage as one stream and is recorded with the size
 * and type it was uploaded with. A name already taken in the version is refused before anything is
 * written, and a file the storage did not keep is not recorded.
 */
class CreatePackageVersionFileUseCaseTest {

    private val versionId = Uuid.random()
    private val payload = CreatePackageVersionFilePayload("pkg-1.0.0.jar", "org/pkg/version/pkg-1.0.0.jar")
    private val content = "jar content".toByteArray()

    private val repository = mockk<PackageVersionFilesRepository> {
        coEvery { getByName(any(), any()) } returns null
        coEvery { create(any(), any(), any(), any()) } answers {
            PackageVersionFile(Uuid.random(), secondArg(), payload.name, thirdArg(), arg(3), payload.path)
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

    private val useCase = CreatePackageVersionFileUseCaseImpl(repository, storage)

    @Test
    fun `a stream is uploaded to the payload path and recorded with its type and length`() = runBlocking {
        val file = useCase(
            payload, versionId, FileFromStream(content.inputStream(), "application/java-archive", content.size.toLong())
        )

        assertContentEquals(content, uploaded)
        coVerify(exactly = 1) { storage.uploadStream(any(), payload.path) }
        coVerify(exactly = 1) {
            repository.create(payload, versionId, "application/java-archive", content.size.toLong())
        }
        assertEquals(versionId, file?.versionId)
    }

    @Test
    fun `bytes in memory are uploaded whole, sized by their length`() = runBlocking {
        useCase(payload, versionId, FileFromBytes(content, "application/octet-stream"))

        assertContentEquals(content, uploaded)
        coVerify(exactly = 1) {
            repository.create(payload, versionId, "application/octet-stream", content.size.toLong())
        }
    }

    @Test
    fun `a url is fetched and uploaded, sized by what the connection reports`() = runBlocking {
        val source = File.createTempFile("pkg", ".tgz").apply {
            writeBytes(content)
            deleteOnExit()
        }

        useCase(payload, versionId, FileFromUrl(source.toURI().toURL(), "application/gzip"))

        assertContentEquals(content, uploaded)
        coVerify(exactly = 1) {
            repository.create(payload, versionId, "application/gzip", content.size.toLong())
        }
    }

    @Test
    fun `a name already taken in the version is refused before anything is uploaded`() = runBlocking {
        coEvery { repository.getByName(payload.name, versionId) } returns
                PackageVersionFile(Uuid.random(), versionId, payload.name, "application/java-archive", 1, payload.path)

        assertFailsWith<PackageVersionFileAlreadyExistsException> {
            useCase(payload, versionId, FileFromBytes(content, "application/java-archive"))
        }
        coVerify(exactly = 0) { storage.uploadStream(any(), any()) }
        coVerify(exactly = 0) { repository.create(any(), any(), any(), any()) }
    }

    @Test
    fun `a file the storage did not keep is not recorded`() = runBlocking {
        coEvery { storage.uploadStream(any(), any()) } returns null

        assertNull(useCase(payload, versionId, FileFromBytes(content, "application/java-archive")))

        coVerify(exactly = 0) { repository.create(any(), any(), any(), any()) }
    }

}
