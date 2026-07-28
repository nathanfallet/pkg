package digital.guimauve.pkg.presentation.config

import dev.kaccelero.routers.createRoutes
import digital.guimauve.pkg.controllers.packages.maven.MavenRouter
import digital.guimauve.pkg.controllers.packages.npm.NpmRouter
import digital.guimauve.pkg.controllers.packages.pypi.PyPiRouter
import digital.guimauve.pkg.presentation.routes.auth.authRoutes
import digital.guimauve.pkg.presentation.routes.dashboard.dashboardRoutes
import digital.guimauve.pkg.presentation.routes.organizations.organizationsRoutes
import digital.guimauve.pkg.presentation.routes.packages.packagesRoutes
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
        authenticate("auth-basic", optional = true) {
            listOf(
                get<MavenRouter>(),
                get<PyPiRouter>(),
            ).forEach {
                it.createRoutes(this)
            }
        }
        authenticate("auth-bearer", optional = true) {
            listOf(
                get<NpmRouter>(),
            ).forEach {
                it.createRoutes(this)
            }
        }

        staticResources("", "static")
    }
}
