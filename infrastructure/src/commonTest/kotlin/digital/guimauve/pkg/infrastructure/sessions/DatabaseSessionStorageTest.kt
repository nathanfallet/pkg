package digital.guimauve.pkg.infrastructure.sessions

import digital.guimauve.pkg.domain.repositories.SessionsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Ktor reads a missing session as a [NoSuchElementException] and starts a new one: anything else
 * would turn an expired cookie into an error page.
 */
class DatabaseSessionStorageTest {

    private val repository = mockk<SessionsRepository>()
    private val storage = DatabaseSessionStorage(repository)

    @Test
    fun `a stored session is read`() = runBlocking {
        coEvery { repository.get("session") } returns "value"

        assertEquals("value", storage.read("session"))
    }

    @Test
    fun `a missing session is reported the way Ktor expects`() = runBlocking {
        coEvery { repository.get("session") } returns null

        assertFailsWith<NoSuchElementException> { storage.read("session") }
        Unit
    }

}
