package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.annotations.*
import dev.kaccelero.controllers.IModelController
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import io.ktor.server.application.*
import kotlin.uuid.Uuid

interface IOrganizationsController : IModelController<Organization, Uuid, CreateOrganizationPayload, Unit> {

    @APIMapping
    @ListModelPath
    suspend fun list(call: ApplicationCall): List<Organization>

    @APIMapping
    @GetModelPath
    @DocumentedError(404, "organizations_not_found")
    suspend fun get(call: ApplicationCall, @Id id: Uuid): Organization

}
