package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface PackageVersionsRepository :
    IChildModelSuspendRepository<PackageVersion, Uuid, CreatePackageVersionPayload, Unit, Uuid> {

    suspend fun getByName(name: String, packageId: Uuid): PackageVersion?
    suspend fun getLatest(packageId: Uuid): PackageVersion?

}
