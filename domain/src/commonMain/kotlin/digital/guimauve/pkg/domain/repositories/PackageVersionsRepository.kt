package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

/** Reads and writes the versions of the packages. Every method is scoped to a package. */
interface PackageVersionsRepository {

    /** Lists the versions of a package, most recently published first. */
    suspend fun list(packageId: Uuid): List<PackageVersion>

    /** Reads a version by its identifier. @return The version, or null if it doesn't exist in that package. */
    suspend fun get(id: Uuid, packageId: Uuid): PackageVersion?

    /** Reads a version by its version string, e.g. `1.0.0` (unique per package). */
    suspend fun getByName(name: String, packageId: Uuid): PackageVersion?

    /**
     * Reads the most recently published version of a package — by publication date, not by version
     * number, and yanked versions included.
     *
     * @return The version, or null if the package has none.
     */
    suspend fun getLatest(packageId: Uuid): PackageVersion?

    /**
     * Records a version, published now by [publishedBy]. A version string the package already has
     * violates the unique index and throws.
     *
     * @return The created version, or null if the insert returned no row.
     */
    suspend fun create(
        payload: CreatePackageVersionPayload,
        packageId: Uuid,
        publishedBy: Uuid,
    ): PackageVersion?

}
