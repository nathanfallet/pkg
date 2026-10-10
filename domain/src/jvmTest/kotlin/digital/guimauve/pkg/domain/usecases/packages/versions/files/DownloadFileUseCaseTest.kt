package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.exceptions.storage.StorageFileNotFoundException
import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.services.StorageService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * A download reads the stored file whole and closes it: the local storage hands out a live file
 * handle, which would otherwise leak on every download.
 */
class DownloadFileUseCaseTest {

    private val file = PackageVersionFile(
        Uuid.random(), Uuid.random(), "pkg-1.0.0.jar", "application/java-archive", 11, "org/pkg/v/pkg-1.0.0.jar",
    )

    private val storage = mockk<StorageService>()
    private val useCase = DownloadFileUseCaseImpl(storage)

    private class TrackedStream(bytes: ByteArray) : ByteArrayInputStream(bytes) {
        var closed = false
            private set

        override fun close() {
            closed = true
            super.close()
        }
    }

    @Test
    fun `the stored content is returned, and its stream closed`() = runBlocking {
        val stream = TrackedStream("jar content".toByteArray())
        coEvery { storage.downloadStream(file.path) } returns stream

        assertContentEquals("jar content".toByteArray(), useCase(file))
        assertTrue(stream.closed, "the stream handed out by the storage must be closed")
    }

    @Test
    fun `a file missing from the storage is reported as such`() = runBlocking {
        coEvery { storage.downloadStream(file.path) } returns null

        assertFailsWith<StorageFileNotFoundException> { useCase(file) }
        Unit
    }

}
