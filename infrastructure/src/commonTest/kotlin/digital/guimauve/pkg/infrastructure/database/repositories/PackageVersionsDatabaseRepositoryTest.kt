package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A version is read within its package only, a version string is taken once per package, and
 * "latest" means the most recently published, not the highest version number.
 */
class PackageVersionsDatabaseRepositoryTest {

    private val database = migratedDatabase()
    private val repository = PackageVersionsDatabaseRepository(database)

    private val packageId = Uuid.random()
    private val otherPackageId = Uuid.random()
    private val publisher = Uuid.random()

    private suspend fun create(version: String, packageId: Uuid = this.packageId, metadata: String? = null) =
        assertNotNull(repository.create(CreatePackageVersionPayload(version, metadata), packageId, publisher))

    /** Creates a version published at [at], so the order does not hang on the clock resolution. */
    private suspend fun publish(version: String, at: Instant, packageId: Uuid = this.packageId): PackageVersion {
        val created = create(version, packageId)
        database.suspendTransaction {
            PackageVersions.update({ PackageVersions.id eq created.id }) { it[publishedAt] = at }
        }
        return created.copy(publishedAt = at)
    }

    @Test
    fun `a created version is read back in its package`() = runBlocking {
        val created = create("1.0.0", metadata = "{\"main\":\"index.js\"}")

        assertEquals("1.0.0", created.version)
        assertEquals(packageId, created.packageId)
        assertEquals(publisher, created.publishedBy)
        assertEquals("{\"main\":\"index.js\"}", created.metadata)
        assertFalse(created.yanked, "a new version is not yanked")
        assertEquals(created, repository.get(created.id, packageId))
        assertEquals(created, repository.getByName("1.0.0", packageId))
    }

    @Test
    fun `a version is not found through another package`() = runBlocking {
        val created = create("1.0.0")

        assertNull(repository.get(created.id, otherPackageId))
        assertNull(repository.getByName("1.0.0", otherPackageId))
        assertNull(repository.getByName("2.0.0", packageId))
    }

    @Test
    fun `a version string is taken once per package`() = runBlocking {
        create("1.0.0")
        create("1.0.0", packageId = otherPackageId)

        assertFailsWith<ExposedSQLException> { create("1.0.0") }
        Unit
    }

    @Test
    fun `the list holds the versions of one package, most recently published first`() = runBlocking {
        val now = Clock.System.now()
        val old = publish("1.0.0", now - 2.hours)
        val recent = publish("1.1.0", now)
        val middle = publish("1.0.1", now - 1.hours)
        publish("9.9.9", now, packageId = otherPackageId)

        assertEquals(listOf(recent, middle, old).map { it.id }, repository.list(packageId).map { it.id })
    }

    @Test
    fun `the latest version is the most recently published, not the highest`() = runBlocking {
        val now = Clock.System.now()
        publish("2.0.0", now - 1.hours)
        val backport = publish("1.0.1", now)

        assertEquals(backport.id, repository.getLatest(packageId)?.id)
    }

    @Test
    fun `the latest version ignores the other packages`() = runBlocking {
        val now = Clock.System.now()
        val ours = publish("1.0.0", now - 1.hours)
        publish("2.0.0", now, packageId = otherPackageId)

        assertEquals(ours.id, repository.getLatest(packageId)?.id)
    }

    @Test
    fun `a package without versions has no latest`() = runBlocking {
        assertNull(repository.getLatest(packageId))
        assertEquals(emptyList(), repository.list(packageId))
    }

}
