package digital.guimauve.pkg.presentation.di

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
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.models.users.User
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for presentation layer dependencies.
 */
val presentationModule = module {
    single<IAuthController> { AuthController(get()) }
    single<IOrganizationsController> {
        OrganizationsController(
            get(),
            get(named<Organization>()),
            get()
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
            get(named<digital.guimauve.pkg.models.packages.Package>()),
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
        )
    }
    single<IPyPiController> {
        PyPiController(
            get(),
            get(),
            get(),
        )
    }

    single<IOrganizationForCallRouter> { OrganizationForCallRouter(get(), get(), get()) }
    single { AuthRouter(get(), get()) }
    single { OrganizationsRouter(get()) }
    single { UsersRouter(get(), get(), get(), get(), get()) }
    single { PackagesRouter(get(), get(), get(), get(), get()) }
    single { PackageVersionsRouter(get(), get(), get(), get()) }
    single { MavenRouter(get()) }
    single { NpmRouter(get()) }
    single { PyPiRouter(get()) }
}
