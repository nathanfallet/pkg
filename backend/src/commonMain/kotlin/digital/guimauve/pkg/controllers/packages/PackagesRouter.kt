package digital.guimauve.pkg.controllers.packages

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.routers.APIChildModelRouter
import dev.kaccelero.routers.ConcatChildModelRouter
import digital.guimauve.pkg.controllers.models.PublicChildModelRouter
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.controllers.organizations.IOrganizationForCallRouter
import digital.guimauve.pkg.controllers.organizations.OrganizationsRouter
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.UpdatePackagePayload
import io.ktor.util.reflect.*
import kotlin.uuid.Uuid

class PackagesRouter(
    controller: IPackagesController,
    getUserUseCase: GetUserUseCase,
    getLocaleForCallUseCase: IGetLocaleForCallUseCase,
    organizationForCallRouter: IOrganizationForCallRouter,
    organizationsRouter: OrganizationsRouter,
) : ConcatChildModelRouter<Package, Uuid, CreatePackagePayload, UpdatePackagePayload, Organization, Uuid>(
    APIChildModelRouter(
        typeInfo<Package>(),
        typeInfo<CreatePackagePayload>(),
        typeInfo<UpdatePackagePayload>(),
        controller,
        IPackagesController::class,
        organizationsRouter,
        prefix = "/api/v1"
    ),
    PublicChildModelRouter(
        typeInfo<Package>(),
        typeInfo<CreatePackagePayload>(),
        typeInfo<UpdatePackagePayload>(),
        controller,
        IPackagesController::class,
        organizationForCallRouter,
        getUserUseCase,
        getLocaleForCallUseCase,
    )
)
