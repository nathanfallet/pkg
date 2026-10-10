package digital.guimauve.pkg.infrastructure.database.repositories

import digital.guimauve.pkg.domain.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.infrastructure.database.tables.PackageVersions
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A file name is taken once per version. Without a version, a name resolves to the file of the
 * most recently published version holding it: that is what serves `maven-metadata.xml`.
 */
class PackageVersionFilesDatabaseRepositoryTest {

    private val database = migratedDatabase()
    private val repository = PackageVersionFilesDatabaseRepository(database)
    private val versions = PackageVersionsDatabaseRepository(database)

    private val packageId = Uuid.random()
    private val otherPackageId = Uuid.random()

    private suspend fun version(name: String, at: Instant, packageId: Uuid = this.packageId): PackageVersion {
        val created = assertNotNull(versions.create(CreatePackageVersionPayload(name, null), packageId, Uuid.random()))
        database.suspendTransaction {
            PackageVersions.update({ PackageVersions.id eq created.id }) { it[publishedAt] = at }
        }
        return created
    }

    private suspend fun file(name: String, versionId: Uuid, size: Long = 42) = assertNotNull(
        repository.create(
            CreatePackageVersionFilePayload(name, "$versionId/$name"), versionId, "application/java-archive", size,
        )
    )

    @Test
    fun `a created file is read back by name in its version`() = runBlocking {
        val version = version("1.0.0", Clock.System.now())

        val created = file("pkg-1.0.0.jar", version.id, size = 1234)

        assertEquals(version.id, created.versionId)
        assertEquals("pkg-1.0.0.jar", created.name)
        assertEquals("application/java-archive", created.contentType)
        assertEquals(1234, created.size)
        assertEquals("${version.id}/pkg-1.0.0.jar", created.path)
        assertEquals(created, repository.getByName("pkg-1.0.0.jar", version.id))
    }

    @Test
    fun `a file is not found through another version`() = runBlocking {
        val now = Clock.System.now()
        val first = version("1.0.0", now)
        val second = version("1.0.1", now)
        file("pkg.jar", first.id)

        assertNull(repository.getByName("pkg.jar", second.id))
        assertNull(repository.getByName("other.jar", first.id))
    }

    @Test
    fun `a file name is taken once per version`() = runBlocking {
        val now = Clock.System.now()
        val first = version("1.0.0", now)
        val second = version("1.0.1", now)
        file("maven-metadata.xml", first.id)
        file("maven-metadata.xml", second.id)

        assertFailsWith<ExposedSQLException> { file("maven-metadata.xml", first.id) }
        Unit
    }

    @Test
    fun `the list holds the files of one version, by name`() = runBlocking {
        val now = Clock.System.now()
        val version = version("1.0.0", now)
        val other = version("1.0.1", now)
        val pom = file("pkg-1.0.0.pom", version.id)
        val jar = file("pkg-1.0.0.jar", version.id)
        val metadata = file("maven-metadata.xml", version.id)
        file("pkg-1.0.1.jar", other.id)

        assertEquals(listOf(metadata, jar, pom), repository.list(version.id))
    }

    @Test
    fun `without a version, a name resolves in the most recently published version`() = runBlocking {
        val now = Clock.System.now()
        val old = version("2.0.0", now - 2.hours)
        val recent = version("1.0.1", now - 1.hours)
        val newest = version("3.0.0", now)
        file("maven-metadata.xml", old.id)
        val expected = file("maven-metadata.xml", recent.id)
        // The newest version does not hold that file: it must not hide the one before it.
        file("pkg-3.0.0.jar", newest.id)

        assertEquals(expected, repository.getLatestByName("maven-metadata.xml", packageId))
    }

    @Test
    fun `without a version, the files of other packages are ignored`() = runBlocking {
        val now = Clock.System.now()
        val ours = version("1.0.0", now - 1.hours)
        val theirs = version("1.0.0", now, packageId = otherPackageId)
        val expected = file("maven-metadata.xml", ours.id)
        file("maven-metadata.xml", theirs.id)

        assertEquals(expected, repository.getLatestByName("maven-metadata.xml", packageId))
        assertNull(repository.getLatestByName("maven-metadata.xml", Uuid.random()))
        assertNull(repository.getLatestByName("missing.xml", packageId))
    }

}
