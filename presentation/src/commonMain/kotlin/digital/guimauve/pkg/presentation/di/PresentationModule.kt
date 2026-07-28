package digital.guimauve.pkg.presentation.di

import digital.guimauve.pkg.presentation.routes.auth.AuthRoutesDependencies
import digital.guimauve.pkg.presentation.routes.dashboard.DashboardRoutesDependencies
import digital.guimauve.pkg.presentation.routes.health.HealthRoutesDependencies
import digital.guimauve.pkg.presentation.routes.organizations.OrganizationsRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.PackagesRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.maven.MavenRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.npm.NpmRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.pypi.PyPiRoutesDependencies
import digital.guimauve.pkg.presentation.routes.packages.versions.PackageVersionsRoutesDependencies
import digital.guimauve.pkg.presentation.routes.users.UsersRoutesDependencies
import org.koin.dsl.module

/**
 * Koin module for presentation layer dependencies.
 */
val presentationModule = module {
    // Probes
    single { HealthRoutesDependencies(get()) }

    // API routes
    single { OrganizationsRoutesDependencies(get(), get()) }
    single { UsersRoutesDependencies(get(), get(), get(), get()) }
    single { PackagesRoutesDependencies(get(), get(), get(), get()) }
    single { PackageVersionsRoutesDependencies(get(), get(), get(), get(), get(), get()) }

    // Dashboard routes
    single { AuthRoutesDependencies(get(), get()) }
    single {
        DashboardRoutesDependencies(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }

    // Package registry routes
    single {
        MavenRoutesDependencies(
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
            get(),
        )
    }
    single { NpmRoutesDependencies(get(), get(), get()) }
    single { PyPiRoutesDependencies(get(), get()) }
}
