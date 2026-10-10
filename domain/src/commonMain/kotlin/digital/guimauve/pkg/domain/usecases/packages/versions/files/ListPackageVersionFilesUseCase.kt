package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

/** Lists the files of a version, by name. */
interface ListPackageVersionFilesUseCase {
    suspend operator fun invoke(versionId: Uuid): List<PackageVersionFile>
}
