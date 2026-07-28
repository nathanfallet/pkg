package digital.guimauve.pkg.api.models.packages.npm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * What `npm login --auth-type=legacy` sends.
 */
@Serializable
data class NpmLoginRequest(
    val name: String,
    val password: String,
)

/**
 * The token the client writes to the `_authToken` of its `.npmrc`.
 */
@Serializable
data class NpmLoginResponse(
    @SerialName("_id") val id: String,
    val token: String,
    val ok: Boolean = true,
)
