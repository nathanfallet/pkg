package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import kotlin.uuid.Uuid

class GetPackageVersionUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetPackageVersionUseCase {
    override suspend fun invoke(versionId: Uuid, packageId: Uuid): PackageVersion? =
        repository.get(versionId, packageId)
}
