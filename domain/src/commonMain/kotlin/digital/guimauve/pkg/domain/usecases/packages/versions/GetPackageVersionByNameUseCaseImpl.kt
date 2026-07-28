package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class GetPackageVersionByNameUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetPackageVersionByNameUseCase {
    override suspend fun invoke(name: String, packageId: Uuid): PackageVersion? =
        repository.getByName(name, packageId)
}
