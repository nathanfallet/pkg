package digital.guimauve.pkg.controllers.users

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.routers.APIChildModelRouter
import dev.kaccelero.routers.ConcatChildModelRouter
import digital.guimauve.pkg.controllers.models.PublicChildModelRouter
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.controllers.organizations.IOrganizationForCallRouter
import digital.guimauve.pkg.controllers.organizations.OrganizationsRouter
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
    organizationsRouter: OrganizationsRouter,
) : ConcatChildModelRouter<User, Uuid, CreateUserPayload, Unit, Organization, Uuid>(
    APIChildModelRouter(
        typeInfo<User>(),
        typeInfo<CreateUserPayload>(),
        typeInfo<Unit>(),
        controller,
        IUsersController::class,
        organizationsRouter,
        prefix = "/api/v1"
    ),
    PublicChildModelRouter(
        typeInfo<User>(),
        typeInfo<CreateUserPayload>(),
        typeInfo<Unit>(),
        controller,
        IUsersController::class,
        organizationForCallRouter,
        getUserUseCase,
        getLocaleForCallUseCase,
    )
)
