package digital.guimauve.pkg.infrastructure.database.repositories

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** A session is written again on every change: the second write must replace the first, not collide. */
class SessionsDatabaseRepositoryTest {

    private val repository = SessionsDatabaseRepository(migratedDatabase())

    @Test
    fun `an unknown session is null`() = runBlocking {
        assertNull(repository.get("unknown"))
    }

    @Test
    fun `a written session is read back`() = runBlocking {
        repository.set("session", "{\"userId\":\"1\"}")

        assertEquals("{\"userId\":\"1\"}", repository.get("session"))
    }

    @Test
    fun `writing a session again replaces its value`() = runBlocking {
        repository.set("session", "first")
        repository.set("session", "second")

        assertEquals("second", repository.get("session"))
    }

    @Test
    fun `sessions are kept apart by id`() = runBlocking {
        repository.set("one", "first")
        repository.set("two", "second")

        assertEquals("first", repository.get("one"))
        assertEquals("second", repository.get("two"))
    }

    @Test
    fun `a deleted session is gone, and the others stay`() = runBlocking {
        repository.set("one", "first")
        repository.set("two", "second")

        repository.delete("one")
        // Deleting what is already gone is not an error: a logout may arrive twice.
        repository.delete("one")

        assertNull(repository.get("one"))
        assertEquals("second", repository.get("two"))
    }

}
