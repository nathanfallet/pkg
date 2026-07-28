package digital.guimauve.pkg.domain.di

import dev.kaccelero.commons.repositories.ICreateChildModelWithContextSuspendUseCase
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCase
import digital.guimauve.pkg.domain.usecases.auth.LoginUseCaseImpl
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCaseImpl
import digital.guimauve.pkg.domain.usecases.organizations.ListOrganizationsUseCase
import digital.guimauve.pkg.domain.usecases.organizations.ListOrganizationsUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.*
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCaseImpl
import digital.guimauve.pkg.domain.usecases.packages.versions.*
import digital.guimauve.pkg.domain.usecases.packages.versions.files.*
import digital.guimauve.pkg.domain.usecases.users.*
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.uuid.Uuid

/**
 * Koin module for domain layer dependencies.
 */
val Application.domainModule
    get() = module {
        // Auth
        single<LoginUseCase> { LoginUseCaseImpl(get(), get()) }

        // Organizations
        single<ListOrganizationsUseCase> { ListOrganizationsUseCaseImpl(get()) }
        single<GetOrganizationUseCase> { GetOrganizationUseCaseImpl(get()) }

        // Users
        single<CreateUserUseCase> { CreateUserUseCaseImpl(get(), get()) }
        single<ListUsersUseCase> { ListUsersUseCaseImpl(get()) }
        single<GetUserUseCase> { GetUserUseCaseImpl(get()) }
        single<GetUserInOrganizationUseCase> { GetUserInOrganizationUseCaseImpl(get()) }
        single<GetUserForEmailUseCase> { GetUserForEmailUseCaseImpl(get()) }
        single<GetUserForRefreshTokenUseCase> { GetUserForRefreshTokenUseCaseImpl(get(), get()) }

        // Packages
        single<ListPackagesUseCase> { ListPackagesUseCaseImpl(get()) }
        single<GetPackageUseCase> { GetPackageUseCaseImpl(get()) }
        single<GetPackageByNameUseCase> { GetPackageByNameUseCaseImpl(get()) }
        single<GetOrCreatePackageUseCase> { GetOrCreatePackageUseCaseImpl(get()) }

        // Package versions
        single<ListPackageVersionsUseCase> { ListPackageVersionsUseCaseImpl(get()) }
        single<GetPackageVersionUseCase> { GetPackageVersionUseCaseImpl(get()) }
        single<GetPackageVersionByNameUseCase> { GetPackageVersionByNameUseCaseImpl(get()) }
        single<GetOrCreatePackageVersionUseCase> { GetOrCreatePackageVersionUseCaseImpl(get()) }
        single<GetLatestPackageVersionUseCase> { GetLatestPackageVersionUseCaseImpl(get()) }

        // Package version files
        single<ListPackageVersionFilesUseCase> { ListPackageVersionFilesUseCaseImpl(get()) }
        single<GetPackageVersionFileByNameUseCase> { GetPackageVersionFileByNameUseCaseImpl(get()) }
        single<GetLatestPackageVersionFileUseCase> { GetLatestPackageVersionFileUseCaseImpl(get()) }
        single<ICreateChildModelWithContextSuspendUseCase<PackageVersionFile, CreatePackageVersionFilePayload, Uuid>>(
            named<PackageVersionFile>()
        ) {
            CreatePackageVersionFileUseCase(get(), get())
        }
        single<DownloadFileUseCase> { DownloadFileUseCaseImpl(get()) }

        // Maven
        single<ParseMavenPathUseCase> { ParseMavenPathUseCaseImpl() }
    }
