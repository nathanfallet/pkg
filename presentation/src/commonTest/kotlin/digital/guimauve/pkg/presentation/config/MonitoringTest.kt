package digital.guimauve.pkg.presentation.config

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
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

}
