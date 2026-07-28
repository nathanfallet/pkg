package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

class GetPackageVersionFileByNameUseCaseImpl(
    private val repository: PackageVersionFilesRepository,
) : GetPackageVersionFileByNameUseCase {
    override suspend fun invoke(name: String, packageId: Uuid): PackageVersionFile? =
        repository.getByName(name, packageId)
}
