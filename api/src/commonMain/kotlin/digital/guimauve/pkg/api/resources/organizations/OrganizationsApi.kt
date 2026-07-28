package digital.guimauve.pkg.api.resources.organizations

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Resource class representing organization-related API endpoints.
 */
@Serializable
@Resource("/api/v1/organizations")
class OrganizationsApi {

    /**
     * Resource class representing a specific organization identified by its ID.
     */
    @Resource("{organizationId}")
    class Id(
        /**
         * The ID of the organization.
         */
        val organizationId: Uuid,
        /**
         * The parent OrganizationsApi resource.
         */
        val parent: OrganizationsApi = OrganizationsApi(),
    )

}
