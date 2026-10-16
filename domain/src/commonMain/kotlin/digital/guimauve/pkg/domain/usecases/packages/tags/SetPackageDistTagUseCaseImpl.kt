package digital.guimauve.pkg.domain.usecases.packages.tags

import digital.guimauve.pkg.domain.repositories.PackageDistTagsRepository
import kotlin.uuid.Uuid

class SetPackageDistTagUseCaseImpl(
    private val repository: PackageDistTagsRepository,
) : SetPackageDistTagUseCase {
    override suspend fun invoke(packageId: Uuid, tag: String, versionId: Uuid) =
        repository.set(packageId, tag, versionId)
}
