package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface PackageVersionsRepository {

    suspend fun list(packageId: Uuid): List<PackageVersion>
    suspend fun get(id: Uuid, packageId: Uuid): PackageVersion?
    suspend fun getByName(name: String, packageId: Uuid): PackageVersion?
    suspend fun getLatest(packageId: Uuid): PackageVersion?
    suspend fun create(
        payload: CreatePackageVersionPayload,
        packageId: Uuid,
        publishedBy: Uuid,
    ): PackageVersion?

}
