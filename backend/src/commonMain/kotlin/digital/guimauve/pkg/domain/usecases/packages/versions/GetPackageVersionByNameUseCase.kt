package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class GetPackageVersionByNameUseCase(
    private val repository: PackageVersionsRepository,
) : IGetPackageVersionByNameUseCase {

    override suspend fun invoke(input1: String, input2: Uuid): PackageVersion? =
        repository.getByName(input1, input2)

}
