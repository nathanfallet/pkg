package digital.guimauve.pkg.domain.models.organizations

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Organization(
    val id: Uuid,
    val name: String,
)
