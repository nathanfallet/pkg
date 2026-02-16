package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.routers.AbstractModelRouter
import dev.kaccelero.routers.ControllerRoute
import dev.kaccelero.routers.ICall
import dev.kaccelero.routers.KtorCall
import digital.guimauve.pkg.domain.usecases.organizations.IRequireOrganizationForCallUseCase
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.parameters.Parameter
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

class OrganizationForCallRouter(
    private val requireOrganizationForCallUseCase: IRequireOrganizationForCallUseCase,
    controller: IOrganizationsController,
) : AbstractModelRouter<Organization, Uuid, CreateOrganizationPayload, Unit>(
    typeInfo<Organization>(),
    typeInfo<CreateOrganizationPayload>(),
    typeInfo<Unit>(),
    controller,
    IOrganizationsController::class,
    "",
    "",
    ""
), IOrganizationForCallRouter {

    override suspend fun get(call: ICall): Organization = requireOrganizationForCallUseCase((call as KtorCall).call)

    override suspend fun <Payload : Any> decodePayload(call: ApplicationCall, type: KClass<Payload>): Payload =
        throw UnsupportedOperationException()

    override fun createControllerRoute(root: Route, controllerRoute: ControllerRoute, openAPI: OpenAPI?) = Unit
    override fun getOpenAPIParameters(self: Boolean): List<Parameter> = emptyList()

}
