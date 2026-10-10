package digital.guimauve.pkg.infrastructure.storage

import digital.guimauve.pkg.domain.models.storage.FileFromStream
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** The local storage keeps every file under its root folder, and refuses a key that would leave it. */
class LocalStorageServiceTest {

    private val parent = Files.createTempDirectory("pkg-storage").toFile()
    private val root = File(parent, "root")
    private val storage = LocalStorageService(root.path)

    private val content = "jar content".toByteArray()
    private fun file() = FileFromStream(content.inputStream(), "application/java-archive", content.size.toLong())

    @AfterTest
    fun cleanUp() {
        parent.deleteRecursively()
    }

    @Test
    fun `an uploaded file is downloaded back, its folders created on the way`() = runBlocking {
        val location = storage.uploadStream(file(), "org/pkg/version/pkg-1.0.0.jar")

        assertEquals(File(root, "org/pkg/version/pkg-1.0.0.jar").path, location)
        val downloaded = assertNotNull(storage.downloadStream("org/pkg/version/pkg-1.0.0.jar"))
        assertContentEquals(content, downloaded.use { it.readBytes() })
    }

    @Test
    fun `uploading to the same key replaces the file`() = runBlocking {
        storage.uploadStream(file(), "a/file")
        val replacement = "new".toByteArray()
        storage.uploadStream(FileFromStream(replacement.inputStream(), "text/plain", 3), "a/file")

        assertContentEquals(replacement, storage.downloadStream("a/file")?.use { it.readBytes() })
    }

    @Test
    fun `a file never uploaded is null`() = runBlocking {
        assertNull(storage.downloadStream("org/pkg/version/missing.jar"))
    }

    @Test
    fun `a key leaving the root is refused, and nothing is written`() = runBlocking {
        listOf("../escaped", "a/../../escaped", "a/./b", "/etc/escaped", "\\escaped", "a\\..\\..\\escaped", "")
            .forEach { key ->
                assertFailsWith<IllegalArgumentException>("accepted $key") { storage.uploadStream(file(), key) }
            }

        assertFalse(File(parent, "escaped").exists())
    }

    @Test
    fun `a signed url is the absolute path of the file under the root`() = runBlocking {
        assertEquals(File(root, "org/pkg/file.jar").absolutePath, storage.signUrl("org/pkg/file.jar"))
    }

}
