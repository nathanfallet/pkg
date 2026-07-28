package digital.guimauve.pkg.api.resources.users

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Resource class representing the users of an organization.
 */
@Serializable
@Resource("/api/v1/organizations/{organizationId}/users")
class UsersApi(
    /**
     * The ID of the organization the users belong to.
     */
    val organizationId: Uuid,
) {

    /**
     * Resource class representing a specific user identified by its ID.
     */
    @Resource("{userId}")
    class Id(
        /**
         * The ID of the user.
         */
        val userId: Uuid,
        /**
         * The parent UsersApi resource.
         */
        val parent: UsersApi,
    )

}
