package digital.guimauve.pkg.domain.usecases.packages.tags

import digital.guimauve.pkg.domain.repositories.PackageDistTagsRepository
import kotlin.uuid.Uuid

class ListPackageDistTagsUseCaseImpl(
    private val repository: PackageDistTagsRepository,
) : ListPackageDistTagsUseCase {
    override suspend fun invoke(packageId: Uuid): Map<String, Uuid> = repository.list(packageId)
}
