package digital.guimauve.pkg.controllers.models

import dev.kaccelero.commons.localization.IGetLocaleForCallUseCase
import dev.kaccelero.controllers.IChildModelController
import dev.kaccelero.models.IChildModel
import dev.kaccelero.routers.IChildModelRouter
import dev.kaccelero.routers.LocalizedTemplateChildModelRouter
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.userOrNull
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.util.reflect.*
import kotlin.reflect.KClass

open class PublicChildModelRouter<Model : IChildModel<Id, CreatePayload, UpdatePayload, ParentId>, Id, CreatePayload : Any, UpdatePayload : Any, ParentModel : IChildModel<ParentId, *, *, *>, ParentId>(
    modelTypeInfo: TypeInfo,
    createPayloadTypeInfo: TypeInfo,
    updatePayloadTypeInfo: TypeInfo,
    controller: IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>,
    controllerClass: KClass<out IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>>,
    parentRouter: IChildModelRouter<ParentModel, ParentId, *, *, *, *>?,
    getUserUseCase: GetUserUseCase,
    getLocaleForCallUseCase: IGetLocaleForCallUseCase,
    respondTemplate: (suspend ApplicationCall.(String, Map<String, Any?>) -> Unit)? = null,
    route: String? = null,
    id: String? = null,
    prefix: String? = null,
) : LocalizedTemplateChildModelRouter<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>(
    modelTypeInfo,
    createPayloadTypeInfo,
    updatePayloadTypeInfo,
    controller,
    controllerClass,
    parentRouter,
    { template, model ->
        if (template == "root/error.ftl") respondTemplate(template, model)
        else (model + mapOf(
            "user" to userOrNull(getUserUseCase),
        )).let { newModel ->
            respondTemplate?.invoke(this, template, newModel) ?: respondTemplate(template, newModel)
        }
    },
    getLocaleForCallUseCase,
    "root/error.ftl",
    "/auth/login?redirect={path}",
    route,
    id,
    prefix
)
