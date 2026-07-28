package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import kotlin.uuid.Uuid

class GetLatestPackageVersionUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetLatestPackageVersionUseCase {
    override suspend fun invoke(versionId: Uuid): PackageVersion? = repository.getLatest(versionId)
}
