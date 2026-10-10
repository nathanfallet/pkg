package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.CreatePackagePayload
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.UpdatePackagePayload
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * A package belongs to one organization, which alone may read, change or delete it by id. Its name
 * is global to its format: that is what lets a registry client resolve it without an organization.
 */
class PackagesDatabaseRepositoryTest {

    private val repository = PackagesDatabaseRepository(migratedDatabase())

    private val organizationId = Uuid.random()
    private val otherOrganizationId = Uuid.random()

    private suspend fun create(
        name: String = "digital.guimauve:pkg",
        format: PackageFormat = PackageFormat.MAVEN,
        isPublic: Boolean = false,
        organizationId: Uuid = this.organizationId,
    ) = assertNotNull(repository.create(CreatePackagePayload(name, format, isPublic), organizationId))

    @Test
    fun `a created package is read back in its organization`() = runBlocking {
        val created = create(isPublic = true)

        assertEquals("digital.guimauve:pkg", created.name)
        assertEquals(PackageFormat.MAVEN, created.format)
        assertEquals(organizationId, created.organizationId)
        assertTrue(created.isPublic)
        assertEquals(created, repository.get(created.id, organizationId))
    }

    @Test
    fun `a package is not found through another organization`() = runBlocking {
        val created = create()

        assertNull(repository.get(created.id, otherOrganizationId))
    }

    @Test
    fun `a name is resolved within its format, whatever the organization`() = runBlocking {
        val maven = create(name = "pkg", format = PackageFormat.MAVEN)
        val npm = create(name = "pkg", format = PackageFormat.NPM, organizationId = otherOrganizationId)

        assertEquals(maven, repository.getByName("pkg", PackageFormat.MAVEN))
        assertEquals(npm, repository.getByName("pkg", PackageFormat.NPM))
        assertNull(repository.getByName("pkg", PackageFormat.PYPI))
        assertNull(repository.getByName("other", PackageFormat.MAVEN))
    }

    @Test
    fun `a name is taken once per format, even across organizations`() = runBlocking {
        create(name = "pkg", format = PackageFormat.NPM)

        assertFailsWith<ExposedSQLException> {
            create(name = "pkg", format = PackageFormat.NPM, organizationId = otherOrganizationId)
        }
        Unit
    }

    @Test
    fun `the list holds the packages of one organization`() = runBlocking {
        val first = create(name = "first")
        val second = create(name = "second", format = PackageFormat.NPM)
        create(name = "theirs", organizationId = otherOrganizationId)

        assertEquals(setOf(first, second), repository.list(organizationId).toSet())
        assertEquals(emptyList(), repository.list(Uuid.random()))
    }

    @Test
    fun `an update changes the visibility of a package of the organization`() = runBlocking {
        val created = create(isPublic = false)

        assertTrue(repository.update(created.id, UpdatePackagePayload(isPublic = true), organizationId))

        assertEquals(created.copy(isPublic = true), repository.get(created.id, organizationId))
    }

    @Test
    fun `an update through another organization changes nothing`() = runBlocking {
        val created = create(isPublic = false)

        assertFalse(repository.update(created.id, UpdatePackagePayload(isPublic = true), otherOrganizationId))

        assertEquals(created, repository.get(created.id, organizationId))
    }

    @Test
    fun `a package of the organization is deleted`() = runBlocking {
        val created = create()

        assertTrue(repository.delete(created.id, organizationId))

        assertNull(repository.get(created.id, organizationId))
        assertFalse(repository.delete(created.id, organizationId), "nothing is left to delete")
    }

    @Test
    fun `a delete through another organization keeps the package`() = runBlocking {
        val created = create()

        assertFalse(repository.delete(created.id, otherOrganizationId))

        assertEquals(created, repository.get(created.id, organizationId))
    }

}
