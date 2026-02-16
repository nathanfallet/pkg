package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class GetLatestPackageVersionUseCase(
    private val repository: PackageVersionsRepository,
) : IGetLatestPackageVersionUseCase {

    override suspend fun invoke(input: Uuid): PackageVersion? = repository.getLatest(input)

}
