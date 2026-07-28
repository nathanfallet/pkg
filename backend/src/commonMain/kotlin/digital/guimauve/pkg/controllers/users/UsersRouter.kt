package digital.guimauve.pkg.controllers.users

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import digital.guimauve.pkg.controllers.models.PublicChildModelRouter
import digital.guimauve.pkg.controllers.organizations.IOrganizationForCallRouter
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.CreateUserPayload
import digital.guimauve.pkg.models.users.User
import io.ktor.util.reflect.*
import kotlin.uuid.Uuid

class UsersRouter(
    controller: IUsersController,
    getUserUseCase: GetUserUseCase,
    getLocaleForCallUseCase: IGetLocaleForCallUseCase,
    organizationForCallRouter: IOrganizationForCallRouter,
) : PublicChildModelRouter<User, Uuid, CreateUserPayload, Unit, Organization, Uuid>(
    typeInfo<User>(),
    typeInfo<CreateUserPayload>(),
    typeInfo<Unit>(),
    controller,
    IUsersController::class,
    organizationForCallRouter,
    getUserUseCase,
    getLocaleForCallUseCase,
)
