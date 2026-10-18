package digital.guimauve.pkg

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.usecases.users.CreateUserUseCase
import digital.guimauve.pkg.infrastructure.database.TransactionManager
import digital.guimauve.pkg.infrastructure.database.tables.Users
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.core.eq
import io.ktor.server.sessions.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.koin.ktor.ext.get
import kotlin.test.*
import kotlin.uuid.Uuid

class UserAdministrationEndToEndTest {
    private fun ApplicationTestBuilder.configuration(name: String) {
        environment {
            config = ApplicationConfig("application.e2e.conf").mergeWith(MapApplicationConfig("database.name" to name))
        }
    }

    private fun bootstrapMessages(events: List<ILoggingEvent>) = events.map { it.formattedMessage }
        .filter { it.startsWith("Bootstrap administrator created:") }

    @Test
    fun bootstrapPasswordIsLoggedOnceAndNeverOnRestart() {
        val name = "bootstrap-log-${Uuid.random()}"
        var firstHash: String? = null
        repeat(2) { boot ->
            testApplication {
                configuration(name)
                application {
                    val logger = log as Logger
                    val logs = ListAppender<ILoggingEvent>().apply { start() }
                    logger.addAppender(logs)
                    try {
                        module()
                        assertEquals(if (boot == 0) 1 else 0, bootstrapMessages(logs.list).size)
                        val user = runBlocking { get<UsersRepository>().getForEmail("admin", true)!! }
                        if (boot == 0) firstHash = user.password else assertEquals(firstHash, user.password)
                    } finally {
                        logger.detachAppender(logs)
                        logs.stop()
                    }
                }
                assertEquals(HttpStatusCode.OK, client.get("/healthz").status)
            }
        }
    }

    private suspend fun HttpClient.form(path: String, cookie: String?, vararg fields: Pair<String, String>) =
        submitForm(path, parameters { fields.forEach { (key, value) -> append(key, value) } }) {
            cookie?.let { header(HttpHeaders.Cookie, it) }
        }

    private fun HttpResponse.cookie(): String = headers.getAll(HttpHeaders.SetCookie)!!
        .first { it.startsWith("session=") }.substringBefore(';')

    private suspend fun HttpClient.page(path: String, cookie: String) = get(path) {
        header(HttpHeaders.Cookie, cookie)
    }

    private suspend fun HttpResponse.csrf() = Regex("name=\"csrf\" value=\"([^\"]+)\"")
        .find(bodyAsText())!!.groupValues[1]

    private suspend fun HttpResponse.temporaryPassword(): String {
        assertEquals(HttpStatusCode.OK, status, bodyAsText())
        assertEquals("no-store", headers[HttpHeaders.CacheControl])
        assertEquals("frame-ancestors 'none'", headers["Content-Security-Policy"])
        return Regex("id=\"temporary-password\">([^<]+)</code>").find(bodyAsText())!!.groupValues[1]
    }

    @Test
    fun legacySessionSurvivesAndCanSubmitAdministrationForms() = testApplication {
        configuration("legacy-session-${Uuid.random()}")
        lateinit var storage: SessionStorage
        lateinit var legacyUser: User
        application {
            module()
            storage = get()
            runBlocking {
                val organization = get<UsersRepository>().getForEmail("admin", false)!!.organizationId
                legacyUser = get<CreateUserUseCase>()(CreateUserPayload("-legacy@example.com", "old password"), organization)!!
                get<TransactionManager>().transaction {
                    Users.update({ Users.id eq legacyUser.id }) { it[admin] = true }
                }
            }
        }
        startApplication()
        val browser = client.config { followRedirects = false }
        val cookie = browser.form("/auth/login", null, "email" to legacyUser.email, "password" to "old password").cookie()
        val sessionId = cookie.substringAfter('=')
        // Ktor's pre-upgrade representation contains just the userId field.
        val old = buildJsonObject { put("userId", legacyUser.id.toString()) }.toString()
        storage.write(sessionId, old)
        assertEquals(HttpStatusCode.OK, browser.page("/packages", cookie).status)
        val page = browser.page("/admin/users", cookie)
        assertEquals(HttpStatusCode.OK, page.status, page.bodyAsText())
        val created = browser.form("/admin/users", cookie, "csrf" to page.csrf(),
            "username" to "legacy-created", "organization" to legacyUser.organizationId.toString())
        created.temporaryPassword()
    }

    @Test
    fun dashboardAccountLifecycleAndRegistryAccess() = testApplication {
        configuration("lifecycle-${Uuid.random()}")
        lateinit var temporary: String
        lateinit var admin: User
        lateinit var users: UsersRepository
        application {
            val logger = log as Logger
            val logs = ListAppender<ILoggingEvent>().apply { start() }
            logger.addAppender(logs)
            try {
                module()
                temporary = bootstrapMessages(logs.list).single().substringAfter("temporaryPassword=").substringBefore('.')
                users = get()
                admin = runBlocking { users.getForEmail("admin", false)!! }
            } finally {
                logger.detachAppender(logs)
                logs.stop()
            }
        }
        startApplication()
        val browser = client.config { followRedirects = false }
        assertEquals(HttpStatusCode.NotFound, browser.get("/auth/register").status)
        val initialLogin = browser.form("/auth/login", null, "email" to "admin", "password" to temporary)
        assertEquals("/auth/password", initialLogin.headers[HttpHeaders.Location])
        val temporarySession = initialLogin.cookie()
        assertEquals("/auth/password", browser.page("/packages", temporarySession).headers[HttpHeaders.Location])
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/maven2/test/package/1/package-1.jar") {
            basicAuth("admin", temporary)
        }.status)
        suspend fun npmLogin(username: String, password: String) = browser.put("/npm/-/user/org.couchdb.user:$username") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("name", username); put("password", password) }.toString())
        }
        assertEquals(HttpStatusCode.Unauthorized, npmLogin("admin", temporary).status)
        assertEquals(HttpStatusCode.Forbidden, browser.page("/api/v1/organizations", temporarySession).status)
        val passwordCsrf = browser.page("/auth/password", temporarySession).csrf()
        assertEquals(HttpStatusCode.Forbidden, browser.form("/auth/password", temporarySession,
            "csrf" to "invalid", "current" to temporary, "replacement" to "admin permanent password").status)
        assertEquals(HttpStatusCode.Found, browser.form("/auth/password", temporarySession,
            "csrf" to passwordCsrf, "current" to temporary, "replacement" to "admin permanent password").status)
        assertTrue(browser.page("/packages", temporarySession).headers[HttpHeaders.Location]!!.startsWith("/auth/login"))
        val adminSession = browser.form("/auth/login", null, "email" to "admin", "password" to "admin permanent password").cookie()
        val management = browser.page("/admin/users", adminSession)
        assertEquals(HttpStatusCode.OK, management.status, management.bodyAsText())
        val csrf = management.csrf()
        assertEquals(HttpStatusCode.Forbidden, browser.form("/admin/users", adminSession,
            "username" to "new-user", "organization" to admin.organizationId.toString()).status)
        val created = browser.form("/admin/users", adminSession, "csrf" to csrf,
            "username" to "new-user", "organization" to admin.organizationId.toString())
        val password = created.temporaryPassword()
        val member = users.getForEmail("new-user", false)!!
        assertFalse(member.admin)
        assertTrue(member.passwordChangeRequired)
        assertFalse(browser.page("/admin/users", adminSession).bodyAsText().contains(password))
        assertFalse(browser.page("/users/${member.id}", adminSession).bodyAsText().contains(password))
        val memberTemporarySession = browser.form("/auth/login", null, "email" to "new-user", "password" to password).cookie()
        assertEquals(HttpStatusCode.Found, browser.form("/auth/password", memberTemporarySession,
            "csrf" to browser.page("/auth/password", memberTemporarySession).csrf(),
            "current" to password, "replacement" to "member permanent password").status)
        val memberSession = browser.form("/auth/login", null, "email" to "new-user", "password" to "member permanent password").cookie()
        assertEquals(HttpStatusCode.OK, browser.page("/packages", memberSession).status)
        assertEquals(HttpStatusCode.Forbidden, browser.page("/admin/users", memberSession).status)
        val memberCsrf = browser.page("/auth/password", memberSession).csrf()
        assertEquals(HttpStatusCode.Forbidden, browser.form("/admin/users/${admin.id}/reset-password", memberSession, "csrf" to memberCsrf).status)
        val tokenResponse = npmLogin("new-user", "member permanent password")
        assertEquals(HttpStatusCode.Created, tokenResponse.status)
        val token = Json.parseToJsonElement(tokenResponse.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content
        assertEquals(HttpStatusCode.OK, browser.get("/api/v1/organizations") { bearerAuth(token) }.status)
        // Disabling must reject every authentication mechanism, including a previously issued JWT.
        assertEquals(HttpStatusCode.Found, browser.form("/admin/users/${member.id}/update", adminSession,
            "csrf" to csrf, "disabled" to "on").status)
        assertEquals(HttpStatusCode.Unauthorized, npmLogin("new-user", "member permanent password").status)
        assertEquals(HttpStatusCode.Unauthorized, browser.form("/auth/login", null,
            "email" to "new-user", "password" to "member permanent password").status)
        assertTrue(browser.page("/packages", memberSession).headers[HttpHeaders.Location]!!.startsWith("/auth/login"))
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/api/v1/organizations") { bearerAuth(token) }.status)
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/npm/@test%2fpackage") { bearerAuth(token) }.status)
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/maven2/test/package/1/package-1.jar") {
            basicAuth("new-user", "member permanent password")
        }.status)
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/pypi/simple/test/") {
            basicAuth("new-user", "member permanent password")
        }.status)
        assertEquals(HttpStatusCode.Found, browser.form("/admin/users/${member.id}/update", adminSession, "csrf" to csrf).status)
        assertEquals(HttpStatusCode.Unauthorized, browser.get("/api/v1/organizations") { bearerAuth(token) }.status)
        val resetPassword = browser.form("/admin/users/${member.id}/reset-password", adminSession, "csrf" to csrf).temporaryPassword()
        assertNotEquals(password, resetPassword)
        val resetLogin = browser.form("/auth/login", null, "email" to "new-user", "password" to resetPassword)
        assertEquals("/auth/password", resetLogin.headers[HttpHeaders.Location])
        assertEquals(HttpStatusCode.Conflict, browser.form("/admin/users/${admin.id}/update", adminSession, "csrf" to csrf).status)
    }
}
