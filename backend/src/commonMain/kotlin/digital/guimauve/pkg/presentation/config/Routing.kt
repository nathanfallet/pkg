package digital.guimauve.pkg.presentation.config

import digital.guimauve.pkg.presentation.routes.auth.authRoutes
import digital.guimauve.pkg.presentation.routes.dashboard.dashboardRoutes
import digital.guimauve.pkg.presentation.routes.organizations.organizationsRoutes
import digital.guimauve.pkg.presentation.routes.packages.maven.mavenRoutes
import digital.guimauve.pkg.presentation.routes.packages.npm.npmRoutes
import digital.guimauve.pkg.presentation.routes.packages.packagesRoutes
import digital.guimauve.pkg.presentation.routes.packages.pypi.pypiRoutes
import digital.guimauve.pkg.presentation.routes.packages.versions.packageVersionsRoutes
import digital.guimauve.pkg.presentation.routes.users.usersRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    install(Resources)
    install(IgnoreTrailingSlash)
    install(AutoHeadResponse)
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    routing {
        authenticate("api-jwt", optional = true) {
            // API
            organizationsRoutes(get())
            usersRoutes(get())
            packagesRoutes(get())
            packageVersionsRoutes(get())

            // Dashboard
            authRoutes(get())
            dashboardRoutes(get())
        }

        // Package registries, each authenticated the way its client does it
        authenticate("auth-basic", optional = true) {
            mavenRoutes(get())
            pypiRoutes(get())
        }
        authenticate("auth-bearer", optional = true) {
            npmRoutes(get())
        }

        staticResources("", "static")
    }
}
