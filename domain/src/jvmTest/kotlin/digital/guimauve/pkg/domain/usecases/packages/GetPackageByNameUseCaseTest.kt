package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.exceptions.packages.PackagePrivateException
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.uuid.Uuid

/** Locks who may read a package: anyone when it is public, only its organization when it is not. */
class GetPackageByNameUseCaseTest {

    private val organizationId = Uuid.random()
    private val member = User(Uuid.random(), organizationId, "john@example.com", null)
    private val outsider = User(Uuid.random(), Uuid.random(), "jane@example.com", null)

    private val repository = mockk<PackagesRepository>()
    private val useCase = GetPackageByNameUseCaseImpl(repository)

    private fun stored(isPublic: Boolean) =
        Package(Uuid.random(), "pkg", PackageFormat.NPM, organizationId, isPublic, Clock.System.now()).also {
            coEvery { repository.getByName("pkg", PackageFormat.NPM) } returns it
        }

    @Test
    fun `an unknown package is null`() = runBlocking {
        coEvery { repository.getByName("pkg", PackageFormat.NPM) } returns null

        assertNull(useCase("pkg", PackageFormat.NPM, member))
    }

    @Test
    fun `a public package is readable by anyone, signed in or not`() = runBlocking {
        val pkg = stored(isPublic = true)

        assertEquals(pkg, useCase("pkg", PackageFormat.NPM, null))
        assertEquals(pkg, useCase("pkg", PackageFormat.NPM, outsider))
        assertEquals(pkg, useCase("pkg", PackageFormat.NPM, member))
    }

    @Test
    fun `a private package is readable by its organization`() = runBlocking {
        val pkg = stored(isPublic = false)

        assertEquals(pkg, useCase("pkg", PackageFormat.NPM, member))
    }

    @Test
    fun `a private package is refused to an anonymous client`() = runBlocking {
        stored(isPublic = false)

        assertFailsWith<PackagePrivateException> { useCase("pkg", PackageFormat.NPM, null) }
        Unit
    }

    @Test
    fun `a private package is refused to another organization`() = runBlocking {
        stored(isPublic = false)

        assertFailsWith<PackagePrivateException> { useCase("pkg", PackageFormat.NPM, outsider) }
        Unit
    }

}
