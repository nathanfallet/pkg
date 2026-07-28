package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class ListPackageVersionsUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : ListPackageVersionsUseCase {
    override suspend fun invoke(packageId: Uuid): List<PackageVersion> = repository.list(packageId)
}
