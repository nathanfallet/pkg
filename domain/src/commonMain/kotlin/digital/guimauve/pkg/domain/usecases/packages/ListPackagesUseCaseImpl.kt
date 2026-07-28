package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import kotlin.uuid.Uuid

class ListPackagesUseCaseImpl(
    private val repository: PackagesRepository,
) : ListPackagesUseCase {
    override suspend fun invoke(organizationId: Uuid): List<Package> = repository.list(organizationId)
}
