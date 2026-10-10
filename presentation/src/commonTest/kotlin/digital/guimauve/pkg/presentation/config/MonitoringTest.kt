package digital.guimauve.pkg.presentation.config

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import digital.guimauve.pkg.domain.services.TranslateService
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MonitoringTest {

    /** Answers the id the logs of the request print, as seen from inside a route. */
    private fun ApplicationTestBuilder.serve() = application {
        configureMonitoring()
        routing {
            get("/probe") {
                call.respondText(MDC.get("call-id").orEmpty())
            }
        }
    }

    @Test
    fun theCallersRequestIdIsKeptEchoedAndLogged() = testApplication {
        serve()

        val response = client.get("/probe") { header(HttpHeaders.XRequestId, "req-1") }

        assertEquals("req-1", response.headers[HttpHeaders.XRequestId])
        assertEquals("req-1", response.bodyAsText())
    }

    @Test
    fun aRequestWithoutAnIdGetsAFreshOne() = testApplication {
        serve()

        val response = client.get("/probe")

        val id = response.headers[HttpHeaders.XRequestId]
        assertTrue(!id.isNullOrBlank())
        assertEquals(id, response.bodyAsText())
    }

    @Test
    fun anUnprintableCallerIdIsReplacedNeverLoggedAsIs() = testApplication {
        serve()

        val response = client.get("/probe") { header(HttpHeaders.XRequestId, "bad id\twith\tspaces") }

        val id = response.headers[HttpHeaders.XRequestId]
        assertTrue(!id.isNullOrBlank())
        assertNotEquals("bad id\twith\tspaces", id)
        assertEquals(id, response.bodyAsText())
    }

    @Test
    fun anOverlongCallerIdIsReplaced() = testApplication {
        serve()

        val tooLong = "a".repeat(129)
        val response = client.get("/probe") { header(HttpHeaders.XRequestId, tooLong) }

        assertNotEquals(tooLong, response.headers[HttpHeaders.XRequestId])
    }

    @Test
    fun probesStillGetAnId() = testApplication {
        serve()

        // Filtered out of the call log, but still traceable from the response.
        val response = client.get("/healthz")

        assertTrue(!response.headers[HttpHeaders.XRequestId].isNullOrBlank())
    }

    @Test
    fun aNonAsciiCallerIdIsReplaced() = testApplication {
        serve()

        // A letter, but not one of [A-Za-z0-9]: lookalikes would make ids ambiguous in the logs.
        val response = client.get("/probe") { header(HttpHeaders.XRequestId, "café-1") }

        assertNotEquals("café-1", response.headers[HttpHeaders.XRequestId])
    }

    @Test
    fun theIdSurvivesSuspensionAndDispatcherSwitches() = testApplication {
        application {
            configureMonitoring()
            routing {
                get("/suspending") {
                    delay(10)
                    val id = withContext(Dispatchers.IO) { MDC.get("call-id") }
                    call.respondText(id.orEmpty())
                }
            }
        }

        val response = client.get("/suspending") { header(HttpHeaders.XRequestId, "req-2") }

        assertEquals("req-2", response.bodyAsText())
    }

    @Test
    fun anUnhandledFailureIsLoggedUnderItsRequestId() = testApplication {
        application {
            install(Koin) {
                modules(
                    module {
                        single<TranslateService> {
                            object : TranslateService {
                                override fun translate(key: String, language: String): String = key
                            }
                        }
                        single<GetUserUseCase> { mockk() }
                    }
                )
            }
            configureMonitoring()
            configureSerialization()
            configureErrorHandling()
            routing {
                get("/boom") { error("boom") }
            }
        }
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.addAppender(appender)
        try {
            val response = client.get("/boom") { header(HttpHeaders.XRequestId, "req-err") }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("req-err", response.headers[HttpHeaders.XRequestId])
            val failure = appender.list.single { it.message.startsWith("Unhandled failure") }
            assertEquals("req-err", failure.mdcPropertyMap["call-id"])
        } finally {
            root.detachAppender(appender)
        }
    }

}
