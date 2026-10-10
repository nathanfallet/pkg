package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.packages.CreatePackagePayload
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.UpdatePackagePayload
import kotlin.uuid.Uuid

/**
 * Reads and writes the packages. Every method but [getByName] is scoped to an organization: a
 * package of another organization reads as missing and cannot be changed.
 */
interface PackagesRepository {

    /** Lists the packages of an organization, in no particular order. */
    suspend fun list(organizationId: Uuid): List<Package>

    /** Reads a package by its identifier. @return The package, or null if it doesn't exist in that organization. */
    suspend fun get(id: Uuid, organizationId: Uuid): Package?

    /**
     * Reads a package by its name within a format, whatever organization owns it — a name is unique
     * per format across the whole registry. The caller decides who may see it.
     */
    suspend fun getByName(name: String, format: PackageFormat): Package?

    /**
     * Registers a package for an organization. A name already taken in that format violates the
     * unique index and throws.
     *
     * @return The created package, or null if the insert returned no row.
     */
    suspend fun create(payload: CreatePackagePayload, organizationId: Uuid): Package?

    /** Changes a package's visibility. @return True if a package of that organization was updated. */
    suspend fun update(id: Uuid, payload: UpdatePackagePayload, organizationId: Uuid): Boolean

    /**
     * Removes a package. Its versions and files are left in place.
     *
     * @return True if a package of that organization was deleted.
     */
    suspend fun delete(id: Uuid, organizationId: Uuid): Boolean

}
