package digital.guimauve.pkg.domain.models.organizations

import kotlinx.serialization.Serializable

/** What it takes to register an [Organization]. */
@Serializable
data class CreateOrganizationPayload(
    val name: String,
)
