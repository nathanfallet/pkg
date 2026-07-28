package digital.guimauve.pkg.domain.models.organizations

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrganizationPayload(
    val name: String,
)
