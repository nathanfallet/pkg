package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.routers.APIModelRouter
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import io.ktor.util.reflect.*
import kotlin.uuid.Uuid

class OrganizationsRouter(
    controller: IOrganizationsController,
) : APIModelRouter<Organization, Uuid, CreateOrganizationPayload, Unit>(
    typeInfo<Organization>(),
    typeInfo<CreateOrganizationPayload>(),
    typeInfo<Unit>(),
    controller,
    IOrganizationsController::class,
    prefix = "/api/v1"
)
