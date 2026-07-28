package digital.guimauve.pkg.api.resources.packages

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Resource class representing the packages of an organization.
 */
@Serializable
@Resource("/api/v1/organizations/{organizationId}/packages")
class PackagesApi(
    /**
     * The ID of the organization the packages belong to.
     */
    val organizationId: Uuid,
) {

    /**
     * Resource class representing a specific package identified by its ID.
     */
    @Resource("{packageId}")
    class Id(
        /**
         * The ID of the package.
         */
        val packageId: Uuid,
        /**
         * The parent PackagesApi resource.
         */
        val parent: PackagesApi,
    )

}
