package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

/** Reads a file of a version by its name, or null if the version has no such file. */
interface GetPackageVersionFileByNameUseCase {
    suspend operator fun invoke(name: String, versionId: Uuid): PackageVersionFile?
}
