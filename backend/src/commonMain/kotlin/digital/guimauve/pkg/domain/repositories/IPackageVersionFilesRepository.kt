package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.models.UUID
import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

interface IPackageVersionFilesRepository :
    IChildModelSuspendRepository<PackageVersionFile, UUID, CreatePackageVersionFilePayload, Unit, UUID> {

    suspend fun getByName(name: String, parentId: UUID): PackageVersionFile?
    suspend fun getLatestByName(name: String, packageId: UUID): PackageVersionFile?

}
