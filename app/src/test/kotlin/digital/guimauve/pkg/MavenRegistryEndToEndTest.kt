package digital.guimauve.pkg

import digital.guimauve.pkg.domain.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.repositories.OrganizationsRepository
import digital.guimauve.pkg.domain.usecases.users.CreateUserUseCase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.get
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Publishes and resolves an artifact through the whole stack — real routing, real basic auth, real
 * database, real storage — because a break anywhere along that chain means nobody can build.
 *
 * Each test gets its own in-memory database and its own storage folder: an H2 memory database is
 * kept alive for the whole JVM by `DB_CLOSE_DELAY=-1`, so sharing one would make the results depend
 * on the order the tests run in.
 */
class MavenRegistryEndToEndTest {

    private val email = "publisher@guimauve.digital"
    private val password = "correct horse battery staple"
    private val artifact = "/maven2/com/example/library/1.0.0/library-1.0.0.jar"
    private val bytes = "not really a jar, but the bytes must survive".toByteArray()

    private fun withRegistry(name: String, block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        val storagePath = "build/test-storage/$name"
        environment {
            config = ApplicationConfig("application.e2e.conf").mergeWith(
                MapApplicationConfig(
                    "database.name" to name,
                    "storage.path" to storagePath,
                )
            )
        }
        application {
            module()
            runBlocking {
                val organization = get<OrganizationsRepository>().create(CreateOrganizationPayload("Guimauve"))
                    ?: return@runBlocking
                get<CreateUserUseCase>()(CreateUserPayload(email, password), organization.id)
            }
        }
        try {
            block()
        } finally {
            File(storagePath).deleteRecursively()
        }
    }

    private fun HttpRequestBuilder.publisher() = basicAuth(email, password)

    @Test
    fun testPublishThenResolve() = withRegistry("publishThenResolve") {
        val published = client.put(artifact) {
            publisher()
            setBody(bytes)
        }
        assertEquals(HttpStatusCode.NoContent, published.status, published.bodyAsText())

        val resolved = client.get(artifact) { publisher() }
        assertEquals(HttpStatusCode.OK, resolved.status)
        assertContentEquals(bytes, resolved.readRawBytes())
    }

    /**
     * A package is private by default, so resolving it without credentials must be refused rather
     * than serving the artifact.
     */
    @Test
    fun testResolvePrivateArtifactAnonymously() = withRegistry("resolvePrivate") {
        client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        val resolved = client.get(artifact)

        assertEquals(HttpStatusCode.Unauthorized, resolved.status)
        assertEquals("""{"error":"packages_private"}""", resolved.bodyAsText())
    }

    /**
     * A publication that failed half way is relaunched from its first file: sending again exactly
     * what is already there must succeed, or the relaunch can never get past it.
     */
    @Test
    fun testPublishTheSameBytesTwiceIsAccepted() = withRegistry("publishSameTwice") {
        client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        val again = client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        assertEquals(HttpStatusCode.NoContent, again.status, again.bodyAsText())
        assertContentEquals(bytes, client.get(artifact) { publisher() }.readRawBytes())
    }

    /** A published version is immutable: other bytes under the same name are refused. */
    @Test
    fun testPublishOtherBytesTwiceIsRefused() = withRegistry("publishOtherTwice") {
        client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        val again = client.put(artifact) {
            publisher()
            setBody("other bytes".toByteArray())
        }

        assertEquals(HttpStatusCode.BadRequest, again.status)
        assertEquals("""{"error":"files_already_exists"}""", again.bodyAsText())
        assertContentEquals(bytes, client.get(artifact) { publisher() }.readRawBytes())
    }

    /**
     * Two publications of the same module overlapping, a release and a pre-release, as a CI does when
     * a release is cut right after a merge. Each reads `maven-metadata.xml`, adds its version and
     * writes it back: the module level metadata has no version in its path, and is rewritten on
     * every publication. The second write must replace the first one, not be refused because the
     * newest version already holds one.
     */
    @Test
    fun testOverlappingPublicationsRewriteTheModuleMetadata() = withRegistry("overlappingPublications") {
        val module = "/maven2/com/example/library"
        val releaseJar = "$module/1.0.1/library-1.0.1.jar"
        val metadata = "$module/maven-metadata.xml"
        val stagingMetadata = "<metadata><versioning><latest>1.0.2-staging.1</latest></versioning></metadata>"
        val releaseMetadata = "<metadata><versioning><latest>1.0.1</latest></versioning></metadata>"

        client.put(releaseJar) { publisher(); setBody(bytes) }
        client.put("$module/1.0.2-staging.1/library-1.0.2-staging.1.jar") { publisher(); setBody(bytes) }
        val staging = client.put(metadata) { publisher(); setBody(stagingMetadata) }
        val stagingSha1 = client.put("$metadata.sha1") { publisher(); setBody("staging-sha1") }
        val release = client.put(metadata) { publisher(); setBody(releaseMetadata) }
        val releaseSha1 = client.put("$metadata.sha1") { publisher(); setBody("release-sha1") }

        assertEquals(HttpStatusCode.NoContent, staging.status, staging.bodyAsText())
        assertEquals(HttpStatusCode.NoContent, stagingSha1.status, stagingSha1.bodyAsText())
        assertEquals(HttpStatusCode.NoContent, release.status, release.bodyAsText())
        assertEquals(HttpStatusCode.NoContent, releaseSha1.status, releaseSha1.bodyAsText())
        assertEquals(releaseMetadata, client.get(metadata) { publisher() }.bodyAsText())
        assertEquals("release-sha1", client.get("$metadata.sha1") { publisher() }.bodyAsText())

        // The relaunch of a release that failed after its first file.
        val relaunched = client.put(releaseJar) { publisher(); setBody(bytes) }
        assertEquals(HttpStatusCode.NoContent, relaunched.status, relaunched.bodyAsText())
    }

    @Test
    fun testPublishAnonymouslyIsRefused() = withRegistry("publishAnonymously") {
        val published = client.put(artifact) { setBody(bytes) }

        assertEquals(HttpStatusCode.Unauthorized, published.status)
        assertEquals("""{"error":"auth_invalid_credentials"}""", published.bodyAsText())
    }

    /**
     * An encoded `../` survives inside a single path segment, so without the check in
     * `ParseMavenPathUseCase` this writes outside the storage folder. The key is
     * `{organizationId}/{packageId}/{versionId}/{filename}`, so escaping the root takes four of them.
     */
    @Test
    fun testPublishCannotEscapeTheStorageFolder() = withRegistry("escapeStorage") {
        val escape = "%2e%2e%2f".repeat(4) + "escaped.jar"

        val published = client.put("/maven2/com/example/library/1.0.0/$escape") {
            publisher()
            setBody(bytes)
        }

        assertEquals(HttpStatusCode.NotFound, published.status)
        assertEquals("""{"error":"invalid_path"}""", published.bodyAsText())
        assertFalse(File("build/test-storage/escaped.jar").exists(), "the artifact escaped the storage")
    }

}
