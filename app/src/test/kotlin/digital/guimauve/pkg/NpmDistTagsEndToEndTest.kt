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
import kotlinx.serialization.json.*
import org.koin.ktor.ext.get
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The dist-tags `npm publish --tag` sends, through the whole stack: a pre-release published under
 * its own tag must not become the version every consumer installs by default.
 *
 * Each test gets its own in-memory database and storage folder, as in [MavenRegistryEndToEndTest].
 */
class NpmDistTagsEndToEndTest {

    private val email = "publisher@guimauve.digital"
    private val password = "correct horse battery staple"
    private val name = "@guimauve/fake-pkg"
    private val tarball = "not really a tarball, but the bytes must survive".toByteArray()

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

    /** The body of `npm publish` for one version, with the tags npm sends along. */
    private fun publication(version: String, distTags: Map<String, String> = mapOf("latest" to version)) =
        buildJsonObject {
            put("_id", name)
            put("name", name)
            putJsonObject("dist-tags") { distTags.forEach { (tag, value) -> put(tag, value) } }
            putJsonObject("versions") {
                putJsonObject(version) {
                    put("name", name)
                    put("version", version)
                    putJsonObject("dist") { put("tarball", "http://elsewhere.example.com/whatever.tgz") }
                }
            }
            putJsonObject("_attachments") {
                putJsonObject("$name-$version.tgz") {
                    put("content_type", "application/octet-stream")
                    put("data", Base64.encode(tarball))
                    put("length", tarball.size)
                }
            }
        }.toString()

    private suspend fun ApplicationTestBuilder.publish(
        version: String,
        distTags: Map<String, String> = mapOf("latest" to version),
        auth: HttpRequestBuilder.() -> Unit,
    ) = client.put("/npm/@guimauve%2ffake-pkg") {
        auth()
        contentType(ContentType.Application.Json)
        setBody(publication(version, distTags))
    }

    private suspend fun ApplicationTestBuilder.packument(auth: HttpRequestBuilder.() -> Unit): JsonObject {
        val response = client.get("/npm/@guimauve%2ffake-pkg") { auth() }
        assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        return Json.parseToJsonElement(response.bodyAsText()).jsonObject
    }

    /** What `npm login --auth-type=legacy` does, to get the token of a `_authToken` line. */
    private suspend fun ApplicationTestBuilder.token(): String {
        val response = client.put("/npm/-/user/org.couchdb.user:$email") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("name", email); put("password", password) }.toString())
        }
        return Json.parseToJsonElement(response.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content
    }

    private suspend fun ApplicationTestBuilder.distTags(token: String): Map<String, String> =
        packument { bearerAuth(token) }["dist-tags"]!!.jsonObject.mapValues { it.value.jsonPrimitive.content }

    @Test
    fun testATaggedPublicationDoesNotMoveLatest() = withRegistry("npmTagged") {
        val token = token()
        publish("1.0.0") { bearerAuth(token) }

        // `npm publish --tag beta`
        val beta = publish("1.1.0-beta.1", mapOf("beta" to "1.1.0-beta.1")) { bearerAuth(token) }

        assertEquals(HttpStatusCode.Created, beta.status, beta.bodyAsText())
        assertEquals(mapOf("latest" to "1.0.0", "beta" to "1.1.0-beta.1"), distTags(token))
    }

    @Test
    fun testAPublicationMovesItsTagOnly() = withRegistry("npmMoves") {
        val token = token()
        publish("1.0.0") { bearerAuth(token) }
        publish("1.1.0-beta.1", mapOf("beta" to "1.1.0-beta.1")) { bearerAuth(token) }

        publish("1.0.1") { bearerAuth(token) }
        assertEquals(mapOf("latest" to "1.0.1", "beta" to "1.1.0-beta.1"), distTags(token))

        publish("1.1.0-beta.2", mapOf("beta" to "1.1.0-beta.2")) { bearerAuth(token) }
        assertEquals(mapOf("latest" to "1.0.1", "beta" to "1.1.0-beta.2"), distTags(token))
    }

    /**
     * A package published only under another tag has no `latest`: `npm install pkg` must not get a
     * pre-release by default (npm then looks for the highest stable version, and finds none).
     */
    @Test
    fun testAPackagePublishedOnlyUnderAnotherTagHasNoLatest() = withRegistry("npmNoLatest") {
        val token = token()

        publish("1.1.0-beta.1", mapOf("beta" to "1.1.0-beta.1")) { bearerAuth(token) }

        assertEquals(mapOf("beta" to "1.1.0-beta.1"), distTags(token))
    }

    @Test
    fun testATagNamingAnotherVersionIsRefused() = withRegistry("npmTagMismatch") {
        val token = token()

        val published = publish("1.0.0", mapOf("latest" to "9.9.9")) { bearerAuth(token) }

        assertEquals(HttpStatusCode.BadRequest, published.status)
        assertEquals(HttpStatusCode.NotFound, client.get("/npm/@guimauve%2ffake-pkg") { bearerAuth(token) }.status)
    }

}
