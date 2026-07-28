package digital.guimauve.pkg.domain.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginPayload(
    val email: String,
    val password: String,
)
