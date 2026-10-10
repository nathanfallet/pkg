package digital.guimauve.pkg.presentation.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level
import kotlin.uuid.Uuid

/**
 * Gives every request an id (the caller's `X-Request-Id`, or a fresh one), sends it back in the
 * response, and puts it in the MDC so every log of the request prints it (`%X{call-id}` in logback).
 */
fun Application.configureMonitoring() {
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { Uuid.random().toString() }
        // A caller-supplied id lands in the logs: keep it short and plain ASCII ([A-Za-z0-9-_.:]).
        // `isLetterOrDigit` would also let through any Unicode letter, lookalikes included.
        verify { it.isNotBlank() && it.length <= 128 && it.all(::isRequestIdChar) }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("call-id")
        // The kubernetes probes hit these every few seconds: logging them would drown everything else.
        filter { call ->
            call.request.path() != "/healthz" && call.request.path() != "/readyz"
        }
    }
}

private fun isRequestIdChar(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-_.:"
