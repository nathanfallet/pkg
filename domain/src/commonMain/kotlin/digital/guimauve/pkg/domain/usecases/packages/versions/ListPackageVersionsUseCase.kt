package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

/** Lists the versions of a package, most recently published first. */
interface ListPackageVersionsUseCase {
    suspend operator fun invoke(packageId: Uuid): List<PackageVersion>
}
