package digital.guimauve.pkg.models.organizations

import dev.kaccelero.annotations.ModelProperty
import dev.kaccelero.annotations.Schema
import dev.kaccelero.models.IModel
import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@Zodable
@JsExport
@Serializable
data class Organization(
    @ModelProperty("id")
    @Schema("Id of the organization", "123abc")
    override val id: Uuid,
    @ModelProperty("string")
    @Schema("Name of the organization", "Guimauve Digital")
    val name: String,
) : IModel<Uuid, CreateOrganizationPayload, Unit>
