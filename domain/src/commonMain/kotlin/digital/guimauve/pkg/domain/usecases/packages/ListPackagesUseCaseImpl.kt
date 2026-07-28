package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.Package
import kotlin.uuid.Uuid

class ListPackagesUseCaseImpl(
    private val repository: PackagesRepository,
) : ListPackagesUseCase {
    override suspend fun invoke(organizationId: Uuid): List<Package> = repository.list(organizationId)
}
