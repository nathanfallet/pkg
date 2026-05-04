package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

class GetLatestPackageVersionUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetLatestPackageVersionUseCase {
    override suspend fun invoke(versionId: Uuid): PackageVersion? = repository.getLatest(versionId)
}
