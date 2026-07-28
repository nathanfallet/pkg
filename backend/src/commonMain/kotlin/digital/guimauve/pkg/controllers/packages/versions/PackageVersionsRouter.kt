package digital.guimauve.pkg.controllers.packages.versions

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.routers.APIChildModelRouter
import dev.kaccelero.routers.ConcatChildModelRouter
import digital.guimauve.pkg.controllers.models.PublicChildModelRouter
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.controllers.packages.PackagesRouter
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import io.ktor.util.reflect.*
import kotlin.uuid.Uuid

class PackageVersionsRouter(
    controller: IPackageVersionsController,
    getUserUseCase: GetUserUseCase,
    getLocaleForCallUseCase: IGetLocaleForCallUseCase,
    packagesRouter: PackagesRouter,
) : ConcatChildModelRouter<PackageVersion, Uuid, CreatePackageVersionPayload, Unit, Package, Uuid>(
    APIChildModelRouter(
        typeInfo<PackageVersion>(),
        typeInfo<CreatePackageVersionPayload>(),
        typeInfo<Unit>(),
        controller,
        IPackageVersionsController::class,
        packagesRouter,
        route = "versions",
        prefix = "/api/v1",
    ),
    PublicChildModelRouter(
        typeInfo<PackageVersion>(),
        typeInfo<CreatePackageVersionPayload>(),
        typeInfo<Unit>(),
        controller,
        IPackageVersionsController::class,
        packagesRouter,
        getUserUseCase,
        getLocaleForCallUseCase,
        route = "versions",
    )
)
