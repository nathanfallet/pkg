package digital.guimauve.pkg.plugins

import dev.kaccelero.commons.auth.*
import dev.kaccelero.commons.localization.GetLocaleForCallUseCase
import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.commons.localization.ITranslateUseCase
import dev.kaccelero.commons.localization.TranslateFromPropertiesUseCase
import dev.kaccelero.commons.repositories.*
import dev.kaccelero.commons.sessions.ISessionsRepository
import dev.kaccelero.commons.sessions.SessionsDatabaseRepository
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import dev.kaccelero.commons.users.RequireUserForCallUseCase
import dev.kaccelero.database.IDatabase
import digital.guimauve.pkg.controllers.auth.AuthController
import digital.guimauve.pkg.controllers.auth.AuthRouter
import digital.guimauve.pkg.controllers.auth.IAuthController
import digital.guimauve.pkg.controllers.organizations.*
import digital.guimauve.pkg.controllers.packages.IPackagesController
import digital.guimauve.pkg.controllers.packages.PackagesController
import digital.guimauve.pkg.controllers.packages.PackagesRouter
import digital.guimauve.pkg.controllers.packages.maven.IMavenController
import digital.guimauve.pkg.controllers.packages.maven.MavenController
import digital.guimauve.pkg.controllers.packages.maven.MavenRouter
import digital.guimauve.pkg.controllers.packages.npm.INpmController
import digital.guimauve.pkg.controllers.packages.npm.NpmController
import digital.guimauve.pkg.controllers.packages.npm.NpmRouter
import digital.guimauve.pkg.controllers.packages.pypi.IPyPiController
import digital.guimauve.pkg.controllers.packages.pypi.PyPiController
import digital.guimauve.pkg.controllers.packages.pypi.PyPiRouter
import digital.guimauve.pkg.controllers.packages.versions.IPackageVersionsController
import digital.guimauve.pkg.controllers.packages.versions.PackageVersionsController
import digital.guimauve.pkg.controllers.packages.versions.PackageVersionsRouter
import digital.guimauve.pkg.controllers.users.IUsersController
import digital.guimauve.pkg.controllers.users.UsersController
import digital.guimauve.pkg.controllers.users.UsersRouter
import digital.guimauve.pkg.domain.repositories.*
import digital.guimauve.pkg.domain.usecases.auth.*
import digital.guimauve.pkg.domain.usecases.organizations.IRequireOrganizationForCallUseCase
import digital.guimauve.pkg.domain.usecases.organizations.RequireOrganizationForCallUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.versions.*
import digital.guimauve.pkg.domain.usecases.packages.versions.files.*
import digital.guimauve.pkg.domain.usecases.users.*
import digital.guimauve.pkg.infrastructure.database.Database
import digital.guimauve.pkg.infrastructure.database.repositories.*
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.services.storage.IStorageService
import digital.guimauve.pkg.services.storage.ProxyStorageService
import digital.guimauve.pkg.services.tokens.IJWTService
import digital.guimauve.pkg.services.tokens.ITokensService
import digital.guimauve.pkg.services.tokens.JWTService
import digital.guimauve.pkg.services.tokens.TokensService
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.uuid.Uuid

fun Application.configureKoin() {
    install(Koin) {
        val databaseModule = module {
            single<IDatabase> {
                Database(
                    environment.config.property("database.protocol").getString(),
                    environment.config.property("database.host").getString(),
                    environment.config.property("database.name").getString(),
                    environment.config.property("database.user").getString(),
                    environment.config.property("database.password").getString()
                )
            }
        }
        val serviceModule = module {
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
        }
        val repositoryModule = module {
            single<ISessionsRepository> { SessionsDatabaseRepository(get()) }
            single<OrganizationsRepository> { OrganizationDatabaseRepository(get()) }
            single<UsersRepository> { UsersDatabaseRepository(get()) }
            single<PackagesRepository> { PackagesDatabaseRepository(get()) }
            single<PackageVersionsRepository> { PackageVersionsDatabaseRepository(get()) }
            single<PackageVersionFilesRepository> { PackageVersionFilesDatabaseRepository(get()) }
        }
        val useCaseModule = module {
            // Application
            single<ITranslateUseCase> { TranslateFromPropertiesUseCase() }
            single<IGetLocaleForCallUseCase> { GetLocaleForCallUseCase() }
            single<IGetJWTPrincipalForCallUseCase> { GetJWTPrincipalForCallUseCase() }
            single<IGetUserIdPrincipalUseCase> { GetUserIdPrincipalUseCase() }

            // Auth
            single<IHashPasswordUseCase> { HashPasswordUseCase() }
            single<IVerifyPasswordUseCase> { VerifyPasswordUseCase() }
            single<IGetSessionForCallUseCase> { GetSessionForCallUseCase() }
            single<ISetSessionForCallUseCase> { SetSessionForCallUseCase() }
            single<IClearSessionForCallUseCase> { ClearSessionForCallUseCase() }
            single<LoginUseCase> { LoginUseCaseImpl(get(), get()) }

            // Organizations
            single<IRequireOrganizationForCallUseCase> { RequireOrganizationForCallUseCase(get(), get()) }
            single<IListModelSuspendUseCase<Organization>>(named<Organization>()) {
                ListModelFromRepositorySuspendUseCase(get<OrganizationsRepository>())
            }
            single<IGetModelSuspendUseCase<Organization, Uuid>>(named<Organization>()) {
                GetModelFromRepositorySuspendUseCase(get<OrganizationsRepository>())
            }
            single<ICreateModelSuspendUseCase<Organization, CreateOrganizationPayload>>(named<Organization>()) {
                CreateModelFromRepositorySuspendUseCase(get<OrganizationsRepository>())
            }

            // Users
            single<IListChildModelSuspendUseCase<User, Uuid>>(named<User>()) {
                ListChildModelFromRepositorySuspendUseCase(get<UsersRepository>())
            }
            single<IGetChildModelSuspendUseCase<User, Uuid, Uuid>>(named<User>()) {
                GetChildModelFromRepositorySuspendUseCase(get<UsersRepository>())
            }
            single<ICreateChildModelSuspendUseCase<User, CreateUserPayload, Uuid>>(named<User>()) {
                CreateUserUseCase(get(), get())
            }
            single<GetUserUseCase> { GetUserUseCaseImpl(get()) }
            single<GetUserForEmailUseCase> { GetUserForEmailUseCaseImpl(get()) }
            single<IGetUserForCallUseCase> { GetUserForCallUseCase(get(), get(), get(), get()) }
            single<GetUserForRefreshTokenUseCase> { GetUserForRefreshTokenUseCaseImpl(get(), get()) }
            single<IRequireUserForCallUseCase> { RequireUserForCallUseCase(get()) }

            // Packages
            single<IGetPackageByNameUseCase> { GetPackageByNameUseCase(get()) }
            single<IGetOrCreatePackageUseCase> { GetOrCreatePackageUseCase(get()) }
            single<IGetPackageVersionByNameUseCase> { GetPackageVersionByNameUseCase(get()) }
            single<IGetOrCreatePackageVersionUseCase> { GetOrCreatePackageVersionUseCase(get()) }
            single<IGetLatestPackageVersionUseCase> { GetLatestPackageVersionUseCase(get()) }
            single<IGetPackageVersionFileByNameUseCase> { GetPackageVersionFileByNameUseCase(get()) }
            single<IGetLatestPackageVersionFileUseCase> { GetLatestPackageVersionFileUseCase(get()) }
            single<ICreateChildModelWithContextSuspendUseCase<PackageVersionFile, CreatePackageVersionFilePayload, Uuid>>(
                named<PackageVersionFile>()
            ) {
                CreatePackageVersionFileUseCase(get(), get())
            }
            single<IListChildModelSuspendUseCase<Package, Uuid>>(named<Package>()) {
                ListChildModelFromRepositorySuspendUseCase(get<PackagesRepository>())
            }
            single<IGetChildModelSuspendUseCase<Package, Uuid, Uuid>>(named<Package>()) {
                GetChildModelFromRepositorySuspendUseCase(get<PackagesRepository>())
            }
            single<IListChildModelSuspendUseCase<PackageVersion, Uuid>>(named<PackageVersion>()) {
                ListChildModelFromRepositorySuspendUseCase(get<PackageVersionsRepository>())
            }
            single<IGetChildModelSuspendUseCase<PackageVersion, Uuid, Uuid>>(named<PackageVersion>()) {
                GetChildModelFromRepositorySuspendUseCase(get<PackageVersionsRepository>())
            }
            single<IListChildModelSuspendUseCase<PackageVersionFile, Uuid>>(named<PackageVersionFile>()) {
                ListChildModelFromRepositorySuspendUseCase(get<PackageVersionFilesRepository>())
            }
            single<IDownloadFileUseCase> { DownloadFileUseCase(get()) }

            // Maven
            single<ParseMavenPathUseCase> { ParseMavenPathUseCaseImpl() }
        }
        val controllerModule = module {
            single<IAuthController> { AuthController(get(), get(), get()) }
            single<IOrganizationsController> {
                OrganizationsController(
                    get(),
                    get(named<Organization>()),
                    get(named<Organization>())
                )
            }
            single<IUsersController> {
                UsersController(
                    get(named<User>()),
                    get(named<User>())
                )
            }
            single<IPackagesController> {
                PackagesController(
                    get(named<Package>()),
                    get(named<Package>()),
                    get(named<PackageVersion>()),
                )
            }
            single<IPackageVersionsController> {
                PackageVersionsController(
                    get(named<PackageVersion>()),
                    get(named<PackageVersionFile>()),
                )
            }
            single<IMavenController> {
                MavenController(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(named<PackageVersionFile>()),
                    get(),
                )
            }
            single<INpmController> {
                NpmController(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                )
            }
            single<IPyPiController> {
                PyPiController(
                    get(),
                    get(),
                    get(),
                    get(),
                )
            }
        }
        val routerModule = module {
            single<IOrganizationForCallRouter> { OrganizationForCallRouter(get(), get()) }
            single { AuthRouter(get(), get()) }
            single { OrganizationsRouter(get()) }
            single { UsersRouter(get(), get(), get(), get(), get()) }
            single { PackagesRouter(get(), get(), get(), get(), get()) }
            single { PackageVersionsRouter(get(), get(), get(), get()) }
            single { MavenRouter(get()) }
            single { NpmRouter(get()) }
            single { PyPiRouter(get()) }
        }

        modules(
            databaseModule,
            serviceModule,
            repositoryModule,
            useCaseModule,
            controllerModule,
            routerModule
        )
    }
}
