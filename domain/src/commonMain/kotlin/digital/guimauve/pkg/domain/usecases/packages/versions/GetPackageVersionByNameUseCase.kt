package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

/** Reads a version of a package by its version string (e.g. `1.0.0`), or null if the package has no such version. */
interface GetPackageVersionByNameUseCase {
    suspend operator fun invoke(name: String, packageId: Uuid): PackageVersion?
}
