package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.versions.files.CreatePackageVersionFilePayload
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface PackageVersionFilesRepository :
    IChildModelSuspendRepository<PackageVersionFile, Uuid, CreatePackageVersionFilePayload, Unit, Uuid> {

    suspend fun getByName(name: String, packageId: Uuid): PackageVersionFile?
    suspend fun getLatestByName(name: String, packageId: Uuid): PackageVersionFile?

}
