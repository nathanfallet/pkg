package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.UpdatePackagePayload
import kotlin.uuid.Uuid

interface PackagesRepository {

    suspend fun list(organizationId: Uuid): List<Package>
    suspend fun get(id: Uuid, organizationId: Uuid): Package?
    suspend fun getByName(name: String, format: PackageFormat): Package?
    suspend fun create(payload: CreatePackagePayload, organizationId: Uuid): Package?
    suspend fun update(id: Uuid, payload: UpdatePackagePayload, organizationId: Uuid): Boolean
    suspend fun delete(id: Uuid, organizationId: Uuid): Boolean

}
