package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface ListPackageVersionFilesUseCase {
    suspend operator fun invoke(versionId: Uuid): List<PackageVersionFile>
}
