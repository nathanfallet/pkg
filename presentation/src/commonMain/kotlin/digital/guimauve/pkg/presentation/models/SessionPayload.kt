package digital.guimauve.pkg.presentation.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SessionPayload(
    val userId: Uuid,
)
