package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.repositories.PackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

class GetPackageVersionFileByNameUseCase(
    private val repository: PackageVersionFilesRepository,
) : IGetPackageVersionFileByNameUseCase {

    override suspend fun invoke(input1: String, input2: Uuid): PackageVersionFile? =
        repository.getByName(input1, input2)

}
