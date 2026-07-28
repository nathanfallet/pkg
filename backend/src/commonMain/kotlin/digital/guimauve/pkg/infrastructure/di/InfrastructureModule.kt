package digital.guimauve.pkg.infrastructure.di

import dev.kaccelero.commons.sessions.ISessionsRepository
import dev.kaccelero.commons.sessions.SessionsDatabaseRepository
import dev.kaccelero.database.IDatabase
import digital.guimauve.pkg.domain.repositories.*
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.infrastructure.bcrypt.BCryptPasswordEncoderService
import digital.guimauve.pkg.infrastructure.database.Database
import digital.guimauve.pkg.infrastructure.database.repositories.*
import digital.guimauve.pkg.services.storage.IStorageService
import digital.guimauve.pkg.services.storage.ProxyStorageService
import digital.guimauve.pkg.services.tokens.IJWTService
import digital.guimauve.pkg.services.tokens.ITokensService
import digital.guimauve.pkg.services.tokens.JWTService
import digital.guimauve.pkg.services.tokens.TokensService
import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for infrastructure-related dependencies.
 */
val Application.infrastructureModule: Module
    get() = module {
        // Database
        single<IDatabase> {
            Database(
                environment.config.property("database.protocol").getString(),
                environment.config.property("database.host").getString(),
                environment.config.property("database.name").getString(),
                environment.config.property("database.user").getString(),
                environment.config.property("database.password").getString()
            )
        }

        // Services
        single<PasswordEncoderService> { BCryptPasswordEncoderService() }
        single<IJWTService> {
            JWTService(
                environment.config.property("jwt.secret").getString(),
                environment.config.property("jwt.issuer").getString(),
                environment.config.property("jwt.audience").getString()
            )
        }
        single<ITokensService> {
            TokensService(get())
        }
        single<IStorageService> {
            ProxyStorageService(environment.config)
        }

        // Repositories
        single<ISessionsRepository> { SessionsDatabaseRepository(get()) }
        single<OrganizationsRepository> { OrganizationDatabaseRepository(get()) }
        single<UsersRepository> { UsersDatabaseRepository(get()) }
        single<PackagesRepository> { PackagesDatabaseRepository(get()) }
        single<PackageVersionsRepository> { PackageVersionsDatabaseRepository(get()) }
        single<PackageVersionFilesRepository> { PackageVersionFilesDatabaseRepository(get()) }
    }
