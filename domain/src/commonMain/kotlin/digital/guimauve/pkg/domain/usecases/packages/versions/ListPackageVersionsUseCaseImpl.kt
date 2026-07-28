package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import kotlin.uuid.Uuid

class ListPackageVersionsUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : ListPackageVersionsUseCase {
    override suspend fun invoke(packageId: Uuid): List<PackageVersion> = repository.list(packageId)
}
