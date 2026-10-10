package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.organizations.CreateOrganizationPayload
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class OrganizationsDatabaseRepositoryTest {

    private val repository = OrganizationsDatabaseRepository(migratedDatabase())

    @Test
    fun `a created organization is read back by its id`() = runBlocking {
        val created = assertNotNull(repository.create(CreateOrganizationPayload("Guimauve")))

        assertEquals("Guimauve", created.name)
        assertEquals(created, repository.get(created.id))
    }

    @Test
    fun `every organization gets its own id`() = runBlocking {
        val first = assertNotNull(repository.create(CreateOrganizationPayload("Guimauve")))
        val second = assertNotNull(repository.create(CreateOrganizationPayload("Guimauve")))

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `an unknown id is null`() = runBlocking {
        assertNull(repository.get(Uuid.random()))
    }

}
