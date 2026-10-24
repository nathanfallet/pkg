package digital.guimauve.pkg.presentation.config

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*

/**
 * Reads the `X-Forwarded-*` headers of the reverse proxy in front of the service (Cloudflare, then the ingress, which
 * terminates TLS and forwards plain http), so `call.request.origin` is the address the client used.
 *
 * Without it the registry served `http://` tarball urls in its npm manifests (`dist.tarball`), and pnpm wrote them in
 * the lockfiles of the consumers. Safe only because the service accepts connections from that proxy alone.
 */
fun Application.configureForwardedHeaders() {
    install(XForwardedHeaders)
}
