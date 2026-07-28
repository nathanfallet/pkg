package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import kotlin.uuid.Uuid

class GetLatestPackageVersionFileUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
) : GetLatestPackageVersionFileUseCase {
    override suspend fun invoke(name: String, packageId: Uuid): PackageVersionFile? =
        repository.getLatestByName(name, packageId)
}
