package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * Locks the two things a user read must never do: leak the password hash unless the caller asks
 * for it (only the login does), and find a user through another organization.
 */
class UsersDatabaseRepositoryTest {

    private val repository = UsersDatabaseRepository(migratedDatabase())

    private val organizationId = Uuid.random()
    private val otherOrganizationId = Uuid.random()

    @Test
    fun `a created user is read back, without its password`() = runBlocking {
        val created = assertNotNull(repository.create(CreateUserPayload("john@example.com", "hash"), organizationId))

        assertEquals("john@example.com", created.email)
        assertEquals(organizationId, created.organizationId)
        assertNull(created.password, "create must not hand the hash back")
        assertEquals(created, repository.get(created.id))
    }

    @Test
    fun `an unknown id is null`() = runBlocking {
        assertNull(repository.get(Uuid.random()))
    }

    @Test
    fun `a user is found in its organization only`() = runBlocking {
        val user = assertNotNull(repository.create(CreateUserPayload("john@example.com", "hash"), organizationId))

        assertEquals(user, repository.get(user.id, organizationId))
        assertNull(repository.get(user.id, otherOrganizationId))
    }

    @Test
    fun `a read by email carries the password only when asked`() = runBlocking {
        repository.create(CreateUserPayload("john@example.com", "hash"), organizationId)

        assertEquals("hash", repository.getForEmail("john@example.com", includePassword = true)?.password)
        val withoutPassword = assertNotNull(repository.getForEmail("john@example.com", includePassword = false))
        assertNull(withoutPassword.password)
    }

    @Test
    fun `an unknown email is null`() = runBlocking {
        repository.create(CreateUserPayload("john@example.com", "hash"), organizationId)

        assertNull(repository.getForEmail("jane@example.com", includePassword = true))
    }

    @Test
    fun `the list holds the users of one organization, without passwords`() = runBlocking {
        val john = assertNotNull(repository.create(CreateUserPayload("john@example.com", "hash"), organizationId))
        val jane = assertNotNull(repository.create(CreateUserPayload("jane@example.com", "hash"), organizationId))
        repository.create(CreateUserPayload("other@example.com", "hash"), otherOrganizationId)

        val users = repository.list(organizationId)

        assertEquals(setOf(john, jane), users.toSet())
        assertEquals(listOf(null, null), users.map { it.password })
    }

}
