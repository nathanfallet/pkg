package digital.guimauve.pkg.controllers.users

import dev.kaccelero.annotations.*
import dev.kaccelero.controllers.IChildModelController
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import io.ktor.server.application.*
import kotlin.uuid.Uuid

interface IUsersController : IChildModelController<User, Uuid, CreateUserPayload, Unit, Organization, Uuid> {

    @APIMapping
    @TemplateMapping("public/users/list.ftl")
    @ListModelPath
    suspend fun list(call: ApplicationCall, @ParentModel parent: Organization): List<User>

    @APIMapping
    @TemplateMapping("public/users/detail.ftl")
    @GetModelPath
    @DocumentedError(404, "users_not_found")
    suspend fun get(call: ApplicationCall, @ParentModel parent: Organization, @Id id: Uuid): Map<String, Any>

}
