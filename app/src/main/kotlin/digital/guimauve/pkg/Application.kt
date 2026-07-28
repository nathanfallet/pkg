package digital.guimauve.pkg

import digital.guimauve.pkg.domain.di.domainModule
import digital.guimauve.pkg.infrastructure.config.configureSecurity
import digital.guimauve.pkg.infrastructure.di.infrastructureModule
import digital.guimauve.pkg.presentation.config.*
import digital.guimauve.pkg.presentation.di.presentationModule
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Initialize plugins
    install(Koin) {
        modules(domainModule, presentationModule, infrastructureModule)
    }
    configureSerialization()
    configureTemplating()
    configureErrorHandling()
    configureSecurity()
    configureSessions()
    configureRouting()
    configureMonitoring()
}
