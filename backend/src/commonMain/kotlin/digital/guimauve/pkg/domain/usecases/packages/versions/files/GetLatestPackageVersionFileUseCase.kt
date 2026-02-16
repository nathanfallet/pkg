package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackageVersionFilesRepository
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

class GetLatestPackageVersionFileUseCase(
    private val repository: IPackageVersionFilesRepository,
) : IGetLatestPackageVersionFileUseCase {

    override suspend fun invoke(input1: String, input2: UUID): PackageVersionFile? =
        repository.getLatestByName(input1, input2)

}
