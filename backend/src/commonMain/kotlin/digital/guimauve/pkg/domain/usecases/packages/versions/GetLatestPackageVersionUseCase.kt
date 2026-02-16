package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion

class GetLatestPackageVersionUseCase(
    private val repository: IPackageVersionsRepository,
) : IGetLatestPackageVersionUseCase {

    override suspend fun invoke(input: UUID): PackageVersion? = repository.getLatest(input)

}
