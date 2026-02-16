package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.routers.IModelRouter
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import kotlin.uuid.Uuid

interface IOrganizationForCallRouter : IModelRouter<Organization, Uuid, CreateOrganizationPayload, Unit>
