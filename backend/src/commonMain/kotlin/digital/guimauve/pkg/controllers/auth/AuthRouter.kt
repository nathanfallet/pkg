package digital.guimauve.pkg.controllers.auth

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.routers.LocalizedTemplateUnitRouter
import io.ktor.server.freemarker.*

class AuthRouter(
    controller: IAuthController,
    getLocaleForCallUseCase: IGetLocaleForCallUseCase,
) : LocalizedTemplateUnitRouter(
    controller,
    IAuthController::class,
    { template, model -> respondTemplate(template, model) },
    getLocaleForCallUseCase,
    errorTemplate = null,
    redirectUnauthorizedToUrl = "/auth/login?redirect={path}",
    route = "auth",
)
