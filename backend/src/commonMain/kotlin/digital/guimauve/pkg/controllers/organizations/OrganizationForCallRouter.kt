package digital.guimauve.pkg.controllers.organizations

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.controllers.IModelController
import dev.kaccelero.routers.AbstractModelRouter
import dev.kaccelero.routers.ControllerRoute
import dev.kaccelero.routers.ICall
import dev.kaccelero.routers.KtorCall
import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.presentation.extensions.requireOrganization
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.parameters.Parameter
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

/**
 * The kaccelero routers reflect on a controller to build their routes. This one exists only to
 * satisfy that signature: the organizations logic lives in `presentation/routes/organizations`.
 */
private interface StubOrganizationsController : IModelController<Organization, Uuid, CreateOrganizationPayload, Unit>

private object StubOrganizationsControllerImpl : StubOrganizationsController

/**
 * Serves as the parent router of the dashboard routers, so that `/packages` and `/users` resolve
 * the organization from the session instead of from the path.
 */
class OrganizationForCallRouter(
    private val getUserUseCase: GetUserUseCase,
    private val getOrganizationUseCase: GetOrganizationUseCase,
) : AbstractModelRouter<Organization, Uuid, CreateOrganizationPayload, Unit>(
    typeInfo<Organization>(),
    typeInfo<CreateOrganizationPayload>(),
    typeInfo<Unit>(),
    StubOrganizationsControllerImpl,
    StubOrganizationsController::class,
    "",
    "",
    ""
), IOrganizationForCallRouter {

    override suspend fun get(call: ICall): Organization = try {
        (call as KtorCall).call.requireOrganization(getUserUseCase, getOrganizationUseCase)
    } catch (exception: InvalidCredentialsException) {
        // The kaccelero template routers only redirect to the login page on a `ControllerException`.
        throw ControllerException(HttpStatusCode.Unauthorized, "auth_invalid_credentials")
    } catch (exception: OrganizationNotFoundException) {
        throw ControllerException(HttpStatusCode.NotFound, "organizations_not_found")
    }

    override suspend fun <Payload : Any> decodePayload(call: ApplicationCall, type: KClass<Payload>): Payload =
        throw UnsupportedOperationException()

    override fun createControllerRoute(root: Route, controllerRoute: ControllerRoute, openAPI: OpenAPI?) = Unit
    override fun getOpenAPIParameters(self: Boolean): List<Parameter> = emptyList()

}
