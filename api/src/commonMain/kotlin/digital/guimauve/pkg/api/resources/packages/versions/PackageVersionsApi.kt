package digital.guimauve.pkg.api.resources.packages.versions

import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Resource class representing the versions of a package.
 */
@Serializable
@Resource("/api/v1/organizations/{organizationId}/packages/{packageId}/versions")
class PackageVersionsApi(
    /**
     * The ID of the organization the package belongs to.
     */
    val organizationId: Uuid,
    /**
     * The ID of the package the versions belong to.
     */
    val packageId: Uuid,
) {

    /**
     * Resource class representing a specific version identified by its ID.
     */
    @Resource("{versionId}")
    class Id(
        /**
         * The ID of the version.
         */
        val versionId: Uuid,
        /**
         * The parent PackageVersionsApi resource.
         */
        val parent: PackageVersionsApi,
    ) {

        /**
         * Resource class representing the files published for a version.
         */
        @Resource("files")
        class Files(
            /**
             * The parent version resource.
             */
            val parent: Id,
        )

    }

}
