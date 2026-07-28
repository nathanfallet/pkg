package digital.guimauve.pkg.domain.models.users

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserPayload(
    val email: String,
    val password: String,
)
