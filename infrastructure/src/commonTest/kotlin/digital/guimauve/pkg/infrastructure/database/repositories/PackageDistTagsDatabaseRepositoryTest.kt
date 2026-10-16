package digital.guimauve.pkg.infrastructure.database.repositories

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

/** A tag names one version of its package: setting it again moves it, and other packages are not affected. */
class PackageDistTagsDatabaseRepositoryTest {

    private val repository = PackageDistTagsDatabaseRepository(migratedDatabase())

    private val packageId = Uuid.random()
    private val otherPackageId = Uuid.random()

    @Test
    fun `a package without tags lists none`() = runBlocking {
        assertEquals(emptyMap(), repository.list(packageId))
    }

    @Test
    fun `tags are listed with the version they point to`() = runBlocking {
        val stable = Uuid.random()
        val beta = Uuid.random()

        repository.set(packageId, "latest", stable)
        repository.set(packageId, "beta", beta)

        assertEquals(mapOf("latest" to stable, "beta" to beta), repository.list(packageId))
    }

    @Test
    fun `setting a tag again moves it and leaves the others`() = runBlocking {
        val first = Uuid.random()
        val second = Uuid.random()
        val beta = Uuid.random()
        repository.set(packageId, "latest", first)
        repository.set(packageId, "beta", beta)

        repository.set(packageId, "latest", second)

        assertEquals(mapOf("latest" to second, "beta" to beta), repository.list(packageId))
    }

    @Test
    fun `the same tag in another package is another tag`() = runBlocking {
        val mine = Uuid.random()
        val theirs = Uuid.random()

        repository.set(packageId, "latest", mine)
        repository.set(otherPackageId, "latest", theirs)

        assertEquals(mapOf("latest" to mine), repository.list(packageId))
        assertEquals(mapOf("latest" to theirs), repository.list(otherPackageId))
    }

}
