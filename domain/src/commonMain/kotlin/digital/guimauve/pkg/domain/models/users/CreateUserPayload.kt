package digital.guimauve.pkg.domain.models.users

import kotlinx.serialization.Serializable

/** What it takes to register a [User]. [password] is the raw password until the use case encodes it. */
@Serializable
data class CreateUserPayload(
    val email: String,
    val password: String,
)
