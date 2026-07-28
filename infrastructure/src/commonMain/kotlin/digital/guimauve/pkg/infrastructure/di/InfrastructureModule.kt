package digital.guimauve.pkg.infrastructure.di

import digital.guimauve.pkg.domain.repositories.*
import digital.guimauve.pkg.domain.services.*
import digital.guimauve.pkg.infrastructure.bcrypt.BCryptPasswordEncoderService
import digital.guimauve.pkg.infrastructure.database.*
import digital.guimauve.pkg.infrastructure.database.repositories.*
import digital.guimauve.pkg.infrastructure.health.DatabaseHealthService
import digital.guimauve.pkg.infrastructure.i18n.PropertiesTranslateService
import digital.guimauve.pkg.infrastructure.jwt.JwtTokenService
import digital.guimauve.pkg.infrastructure.sessions.DatabaseSessionStorage
import digital.guimauve.pkg.infrastructure.storage.ProxyStorageService
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for infrastructure-related dependencies.
 */
val Application.infrastructureModule: Module
    get() = module {
        // Database
        single {
            DatabaseConfig(
                protocol = environment.config.property("database.protocol").getString(),
                host = environment.config.property("database.host").getString(),
                name = environment.config.property("database.name").getString(),
                user = environment.config.property("database.user").getString(),
                password = environment.config.property("database.password").getString(),
            )
        }
        single<DatabaseFactory> {
            val config = get<DatabaseConfig>()
            when (config.protocol) {
                "mysql" -> MySQLDatabaseFactory(config)
                "h2" -> H2DatabaseFactory(config)
                else -> throw IllegalArgumentException("Unsupported database protocol: ${config.protocol}")
            }
        }
        single<TransactionManager> { TransactionManagerImpl(get()) }

        single<HealthService> { DatabaseHealthService(get()) }

        // Services
        single<PasswordEncoderService> { BCryptPasswordEncoderService() }
        single<TranslateService> { PropertiesTranslateService() }
        // The concrete type is bound too: the Ktor `jwt` provider needs its verifier.
        single {
            JwtTokenService(
                environment.config.property("jwt.secret").getString(),
                environment.config.property("jwt.issuer").getString(),
                environment.config.property("jwt.audience").getString()
            )
        }
        single<TokenService> { get<JwtTokenService>() }
        single<StorageService> {
            ProxyStorageService(environment.config)
        }

        // Repositories
        single<SessionsRepository> { SessionsDatabaseRepository(get()) }
        single<SessionStorage> { DatabaseSessionStorage(get()) }
        single<OrganizationsRepository> { OrganizationsDatabaseRepository(get()) }
        single<UsersRepository> { UsersDatabaseRepository(get()) }
        single<PackagesRepository> { PackagesDatabaseRepository(get()) }
        single<PackageVersionsRepository> { PackageVersionsDatabaseRepository(get()) }
        single<PackageVersionFilesRepository> { PackageVersionFilesDatabaseRepository(get()) }
    }
