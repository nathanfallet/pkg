package digital.guimauve.pkg.presentation.extensions

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * Serves the content of a published file.
 *
 * The declared content type of an artifact is whatever its publisher sent, so replaying it would
 * let anyone who can publish serve `text/html` from this origin — where the session cookie of every
 * other user lives. Artifacts therefore always go out as an opaque attachment.
 */
suspend fun ApplicationCall.respondArtifact(name: String, bytes: ByteArray) {
    response.header("X-Content-Type-Options", "nosniff")
    response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, name).toString()
    )
    respondBytes(bytes, ContentType.Application.OctetStream)
}
