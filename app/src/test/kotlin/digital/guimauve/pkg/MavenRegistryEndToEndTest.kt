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

    @Test
    fun testPublishTwiceIsRefused() = withRegistry("publishTwice") {
        client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        val again = client.put(artifact) {
            publisher()
            setBody(bytes)
        }

        assertEquals(HttpStatusCode.BadRequest, again.status)
        assertEquals("""{"error":"files_already_exists"}""", again.bodyAsText())
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
