package digital.guimauve.pkg.domain.repositories

import dev.kaccelero.models.UUID
import dev.kaccelero.repositories.IChildModelSuspendRepository
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.UpdatePackagePayload

interface IPackagesRepository :
    IChildModelSuspendRepository<Package, UUID, CreatePackagePayload, UpdatePackagePayload, UUID> {

    suspend fun getByName(name: String, format: PackageFormat): Package?

}
