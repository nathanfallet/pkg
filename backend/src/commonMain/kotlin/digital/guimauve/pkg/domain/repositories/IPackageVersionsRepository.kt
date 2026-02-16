package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.models.UUID
import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion

interface IPackageVersionsRepository :
    IChildModelSuspendRepository<PackageVersion, UUID, CreatePackageVersionPayload, Unit, UUID> {

    suspend fun getByName(name: String, packageId: UUID): PackageVersion?
    suspend fun getLatest(packageId: UUID): PackageVersion?

}
