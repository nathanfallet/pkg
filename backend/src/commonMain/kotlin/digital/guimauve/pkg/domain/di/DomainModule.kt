package digital.guimauve.pkg.domain.di

import dev.kaccelero.commons.auth.*
import dev.kaccelero.commons.localization.GetLocaleForCallUseCase
import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.commons.localization.ITranslateUseCase
import dev.kaccelero.commons.localization.TranslateFromPropertiesUseCase
import dev.kaccelero.commons.repositories.*
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import dev.kaccelero.commons.users.RequireUserForCallUseCase
import digital.guimauve.pkg.domain.repositories.*
import digital.guimauve.pkg.domain.usecases.auth.*
import digital.guimauve.pkg.domain.usecases.organizations.IRequireOrganizationForCallUseCase
import digital.guimauve.pkg.domain.usecases.organizations.RequireOrganizationForCallUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.versions.*
import digital.guimauve.pkg.domain.usecases.packages.versions.files.*
import digital.guimauve.pkg.domain.usecases.users.*
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.uuid.Uuid

/**
 * Koin module for domain layer dependencies.
 */
val Application.domainModule
    get() = module {
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
        single<GetPackageByNameUseCase> { GetPackageByNameUseCaseImpl(get()) }
        single<GetOrCreatePackageUseCase> { GetOrCreatePackageUseCaseImpl(get()) }
        single<GetPackageVersionByNameUseCase> { GetPackageVersionByNameUseCaseImpl(get()) }
        single<GetOrCreatePackageVersionUseCase> { GetOrCreatePackageVersionUseCaseImpl(get()) }
        single<GetLatestPackageVersionUseCase> { GetLatestPackageVersionUseCaseImpl(get()) }
        single<GetPackageVersionFileByNameUseCase> { GetPackageVersionFileByNameUseCaseImpl(get()) }
        single<GetLatestPackageVersionFileUseCase> { GetLatestPackageVersionFileUseCaseImpl(get()) }
        single<ICreateChildModelWithContextSuspendUseCase<PackageVersionFile, CreatePackageVersionFilePayload, Uuid>>(
            named<PackageVersionFile>()
        ) {
            CreatePackageVersionFileUseCase(get(), get())
        }
        single<IListChildModelSuspendUseCase<digital.guimauve.pkg.models.packages.Package, Uuid>>(named<digital.guimauve.pkg.models.packages.Package>()) {
            ListChildModelFromRepositorySuspendUseCase(get<PackagesRepository>())
        }
        single<IGetChildModelSuspendUseCase<digital.guimauve.pkg.models.packages.Package, Uuid, Uuid>>(named<Package>()) {
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
        single<DownloadFileUseCase> { DownloadFileUseCaseImpl(get()) }

        // Maven
        single<ParseMavenPathUseCase> { ParseMavenPathUseCaseImpl() }
    }
