package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class GetPackageVersionUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetPackageVersionUseCase {
    override suspend fun invoke(versionId: Uuid, packageId: Uuid): PackageVersion? =
        repository.get(versionId, packageId)
}
