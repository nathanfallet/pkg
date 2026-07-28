package digital.guimauve.pkg.presentation.di

import digital.guimauve.pkg.controllers.auth.AuthController
import digital.guimauve.pkg.controllers.auth.AuthRouter
import digital.guimauve.pkg.controllers.auth.IAuthController
import digital.guimauve.pkg.controllers.organizations.IOrganizationForCallRouter
import digital.guimauve.pkg.controllers.organizations.OrganizationForCallRouter
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
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.presentation.routes.organizations.OrganizationsRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.PackagesRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.versions.PackageVersionsRoutesDependencies
import digital.guimauve.pkg.presentation.routes.users.UsersRoutesDependencies
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for presentation layer dependencies.
 */
val presentationModule = module {
    // API routes
    single { OrganizationsRoutesDependencies(get(), get(), get()) }
    single { UsersRoutesDependencies(get(), get(), get(), get()) }
    single { PackagesRoutesDependencies(get(), get(), get(), get()) }
    single { PackageVersionsRoutesDependencies(get(), get(), get(), get(), get(), get()) }

    // Dashboard and package registries
    single<IAuthController> { AuthController(get()) }
    single<IUsersController> { UsersController(get(), get()) }
    single<IPackagesController> { PackagesController(get(), get(), get()) }
    single<IPackageVersionsController> { PackageVersionsController(get(), get()) }
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

    single<IOrganizationForCallRouter> { OrganizationForCallRouter(get(), get()) }
    single { AuthRouter(get(), get()) }
    single { UsersRouter(get(), get(), get(), get()) }
    single { PackagesRouter(get(), get(), get(), get()) }
    single { PackageVersionsRouter(get(), get(), get(), get()) }
    single { MavenRouter(get()) }
    single { NpmRouter(get()) }
    single { PyPiRouter(get()) }
}
