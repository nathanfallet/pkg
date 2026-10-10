package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

/** Reads a version of a package by its identifier, or null if that package has no such version. */
interface GetPackageVersionUseCase {
    suspend operator fun invoke(versionId: Uuid, packageId: Uuid): PackageVersion?
}
