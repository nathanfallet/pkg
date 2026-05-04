package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface GetPackageVersionByNameUseCase {
    suspend operator fun invoke(name: String, packageId: Uuid): PackageVersion?
}
