package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * A maven upload sends each file of a version in its own request: the first one creates the
 * version, every following one must find it instead of creating it again.
 */
class GetOrCreatePackageVersionUseCaseTest {

    private val packageId = Uuid.random()
    private val user = User(Uuid.random(), Uuid.random(), "john@example.com", null)

    private val repository = mockk<PackageVersionsRepository>()
    private val useCase = GetOrCreatePackageVersionUseCaseImpl(repository)

    private val version = PackageVersion(
        Uuid.random(), packageId, "1.0.0", user.id, Clock.System.now(), metadata = "{}", yanked = false,
    )

    @Test
    fun `an existing version is returned without creating one`() = runBlocking {
        coEvery { repository.getByName("1.0.0", packageId) } returns version

        assertEquals(version, useCase("1.0.0", packageId, user, metadata = "{\"new\":true}"))

        coVerify(exactly = 0) { repository.create(any(), any(), any()) }
    }

    @Test
    fun `a new version is created with its metadata, published by the user`() = runBlocking {
        coEvery { repository.getByName("1.0.0", packageId) } returns null
        coEvery { repository.create(any(), any(), any()) } returns version

        assertEquals(version, useCase("1.0.0", packageId, user, metadata = "{}"))

        coVerify(exactly = 1) {
            repository.create(CreatePackageVersionPayload("1.0.0", "{}"), packageId, user.id)
        }
    }

}
