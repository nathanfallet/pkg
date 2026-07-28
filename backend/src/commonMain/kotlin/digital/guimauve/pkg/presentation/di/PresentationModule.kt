package digital.guimauve.pkg.presentation.di

import digital.guimauve.pkg.controllers.packages.maven.IMavenController
import digital.guimauve.pkg.controllers.packages.maven.MavenController
import digital.guimauve.pkg.controllers.packages.maven.MavenRouter
import digital.guimauve.pkg.controllers.packages.npm.INpmController
import digital.guimauve.pkg.controllers.packages.npm.NpmController
import digital.guimauve.pkg.controllers.packages.npm.NpmRouter
import digital.guimauve.pkg.controllers.packages.pypi.IPyPiController
import digital.guimauve.pkg.controllers.packages.pypi.PyPiController
import digital.guimauve.pkg.controllers.packages.pypi.PyPiRouter
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.presentation.routes.auth.AuthRoutesDependencies
import digital.guimauve.pkg.presentation.routes.dashboard.DashboardRoutesDependencies
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

    // Dashboard routes
    single { AuthRoutesDependencies(get(), get()) }
    single { DashboardRoutesDependencies(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    // Package registries
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

    single { MavenRouter(get()) }
    single { NpmRouter(get()) }
    single { PyPiRouter(get()) }
}
