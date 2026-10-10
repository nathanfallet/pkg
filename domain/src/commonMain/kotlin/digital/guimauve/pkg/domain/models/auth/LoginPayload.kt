package digital.guimauve.pkg.domain.models.auth

import kotlinx.serialization.Serializable

/** The credentials a user logs in with. */
@Serializable
data class LoginPayload(
    val email: String,
    val password: String,
)
