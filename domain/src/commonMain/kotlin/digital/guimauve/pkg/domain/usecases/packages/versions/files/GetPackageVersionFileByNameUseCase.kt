package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface GetPackageVersionFileByNameUseCase {
    suspend operator fun invoke(name: String, versionId: Uuid): PackageVersionFile?
}
