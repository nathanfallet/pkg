package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.exceptions.packages.PackageWriteForbiddenException
import digital.guimauve.pkg.domain.models.packages.CreatePackagePayload
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * A package name is global to its format: publishing under a name another organization owns must
 * be refused, never answered with that organization's package.
 */
class GetOrCreatePackageUseCaseTest {

    private val user = User(Uuid.random(), Uuid.random(), "john@example.com", null)

    private val repository = mockk<PackagesRepository>()
    private val useCase = GetOrCreatePackageUseCaseImpl(repository)

    private fun pkg(organizationId: Uuid) =
        Package(Uuid.random(), "digital.guimauve:pkg", PackageFormat.MAVEN, organizationId, false, Clock.System.now())

    @Test
    fun `a package of the user's organization is returned as is`() = runBlocking {
        val existing = pkg(user.organizationId)
        coEvery { repository.getByName("digital.guimauve:pkg", PackageFormat.MAVEN) } returns existing

        assertEquals(existing, useCase("digital.guimauve:pkg", PackageFormat.MAVEN, user))

        coVerify(exactly = 0) { repository.create(any(), any()) }
    }

    @Test
    fun `a package of another organization is refused`() = runBlocking {
        coEvery { repository.getByName("digital.guimauve:pkg", PackageFormat.MAVEN) } returns pkg(Uuid.random())

        assertFailsWith<PackageWriteForbiddenException> {
            useCase("digital.guimauve:pkg", PackageFormat.MAVEN, user)
        }
        coVerify(exactly = 0) { repository.create(any(), any()) }
    }

    @Test
    fun `a new name is created private, in the user's organization`() = runBlocking {
        val created = pkg(user.organizationId)
        coEvery { repository.getByName("digital.guimauve:pkg", PackageFormat.MAVEN) } returns null
        coEvery { repository.create(any(), any()) } returns created

        assertEquals(created, useCase("digital.guimauve:pkg", PackageFormat.MAVEN, user))

        coVerify(exactly = 1) {
            repository.create(
                CreatePackagePayload("digital.guimauve:pkg", PackageFormat.MAVEN, isPublic = false),
                user.organizationId,
            )
        }
    }

}
