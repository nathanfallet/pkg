package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

/**
 * Reads a file by name in the most recently published version of a package that has it, or null if
 * none does. It serves the maven paths that carry no version.
 */
interface GetLatestPackageVersionFileUseCase {
    suspend operator fun invoke(name: String, packageId: Uuid): PackageVersionFile?
}
