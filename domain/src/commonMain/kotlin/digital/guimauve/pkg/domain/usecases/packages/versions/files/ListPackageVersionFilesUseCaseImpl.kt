package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

class ListPackageVersionFilesUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
) : ListPackageVersionFilesUseCase {
    override suspend fun invoke(versionId: Uuid): List<PackageVersionFile> = repository.list(versionId)
}
