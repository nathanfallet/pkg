package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.UpdatePackagePayload
import kotlin.uuid.Uuid

interface PackagesRepository :
    IChildModelSuspendRepository<Package, Uuid, CreatePackagePayload, UpdatePackagePayload, Uuid> {

    suspend fun getByName(name: String, format: PackageFormat): Package?

}
