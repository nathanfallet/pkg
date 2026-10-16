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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Publishes and installs an npm package through the whole stack — real routing, real auth, real
 * database, real storage — with the request shapes `npm publish` and `npm install` send.
 *
 * Each test gets its own in-memory database and storage folder, as in [MavenRegistryEndToEndTest].
 */
class NpmRegistryEndToEndTest {

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

    @Test
    fun testPublishThenInstallWithAToken() = withRegistry("npmBearer") {
        val token = token()

        val published = publish("1.0.0") { bearerAuth(token) }
        assertEquals(HttpStatusCode.Created, published.status, published.bodyAsText())

        assertEquals("1.0.0", packument { bearerAuth(token) }["dist-tags"]!!.jsonObject["latest"]!!.jsonPrimitive.content)
        val archive = client.get("/npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz") { bearerAuth(token) }
        assertContentEquals(tarball, archive.readRawBytes())
    }

    /**
     * The `_auth` of a `.npmrc` is `base64(username:password)`, sent as basic auth: the same
     * credentials as maven, with no login and nothing that expires.
     */
    @Test
    fun testPublishThenInstallWithBasicAuth() = withRegistry("npmBasic") {
        val published = publish("1.0.0") { basicAuth(email, password) }
        assertEquals(HttpStatusCode.Created, published.status, published.bodyAsText())

        assertEquals(
            "1.0.0",
            packument { basicAuth(email, password) }["dist-tags"]!!.jsonObject["latest"]!!.jsonPrimitive.content
        )
        val archive = client.get("/npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz") { basicAuth(email, password) }
        assertEquals(HttpStatusCode.OK, archive.status)
        assertContentEquals(tarball, archive.readRawBytes())
    }

    @Test
    fun testBasicAuthWithAWrongPasswordIsRefused() = withRegistry("npmBasicWrong") {
        val published = publish("1.0.0") { basicAuth(email, "wrong") }

        assertEquals(HttpStatusCode.Unauthorized, published.status)
    }

    /** Accepting basic next to bearer must not let a bad token through. */
    @Test
    fun testAnInvalidTokenIsRefused() = withRegistry("npmBearerWrong") {
        val published = publish("1.0.0") { bearerAuth("not-a-token") }

        assertEquals(HttpStatusCode.Unauthorized, published.status)
    }

    @Test
    fun testInstallAnonymouslyIsRefused() = withRegistry("npmAnonymous") {
        publish("1.0.0") { basicAuth(email, password) }

        val response = client.get("/npm/@guimauve%2ffake-pkg")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("""{"error":"packages_private"}""", response.bodyAsText())
    }

}
