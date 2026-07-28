package digital.guimauve.pkg.models.organizations

import dev.kaccelero.models.IModel
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class Organization(
    override val id: Uuid,
    val name: String,
) : IModel<Uuid, CreateOrganizationPayload, Unit>
