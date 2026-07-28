package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import kotlin.uuid.Uuid

class GetPackageVersionByNameUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetPackageVersionByNameUseCase {
    override suspend fun invoke(name: String, packageId: Uuid): PackageVersion? =
        repository.getByName(name, packageId)
}
